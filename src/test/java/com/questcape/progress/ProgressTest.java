package com.questcape.progress;

import com.google.gson.Gson;
import com.questcape.guide.*;
import com.questcape.persistence.JsonStore;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.client.config.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProgressTest
{
	public static AccountProgress observation(String scope, String name, String mode,
		Map<String, AccountProgress.QuestStatus> quests, Map<String, Integer> levels)
	{
		return new AccountProgress(scope, name, mode, "Live RuneLite", System.currentTimeMillis(),
			System.currentTimeMillis(), quests, levels, Map.of(), Set.of());
	}

	@Test
	public void thresholdQuestStageAndManualSemantics() throws Exception
	{
		List<GuideRow> rows = new GuideParser().parse(GuideParserTest.table(
			GuideParserTest.activity("Train Firemaking from level 34 to level 40")
				+ GuideParserTest.activity("Train Firemaking from level 40 to level 49")
				+ GuideParserTest.activity("Hand in Herbi Flax diary, from level 25 to level 25")
				+ GuideParserTest.activity("Unlock: balloon route")
				+ GuideParserTest
					.activity("Start miniquest: <a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a>")
				+ GuideParserTest
					.activity("<a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a> (miniquest)")));
		AccountProgress account = observation("live:A", "Alice", "STANDARD",
			Map.of("IN_SEARCH_OF_KNOWLEDGE", AccountProgress.QuestStatus.IN_PROGRESS),
			Map.of("FIREMAKING", 40, "HERBLORE", 99));
		assertEquals(Completion.State.COMPLETE, Completion.of(rows.get(0), account).getState());
		assertEquals(Completion.State.INCOMPLETE, Completion.of(rows.get(1), account).getState());
		assertEquals(Completion.State.UNKNOWN, Completion.of(rows.get(2), account).getState());
		assertEquals(Completion.State.COMPLETE, Completion.of(rows.get(4), account).getState());
		assertEquals(Completion.State.IN_PROGRESS, Completion.of(rows.get(5), account).getState());
		ProgressService service = new ProgressService(
			new JsonStore(new Gson(), Files.createTempDirectory("oqg-progress")));
		service.acceptLive(account);
		service.toggleManual("live:A", rows.get(2), true);
		assertTrue(Completion.of(rows.get(2), service.current()).isManual());
		assertEquals(Completion.State.COMPLETE, Completion.of(rows.get(2), service.current()).getState());
		service.toggleManual("live:A", rows.get(2), false);
		assertEquals(Completion.State.UNKNOWN, Completion.of(rows.get(2), service.current()).getState());
		assertEquals("RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE",
			Identities.quest("Recipe for Disaster/Freeing the Goblin generals"));
	}

	@Test
	public void persistenceRaceAccountAndModeIsolation() throws Exception
	{
		Path root = Files.createTempDirectory("oqg-isolation");
		JsonStore store = new JsonStore(new Gson(), root);
		ProgressService service = new ProgressService(store);
		AccountProgress alice = observation("live:A:STANDARD", "Alice", "STANDARD", Map.of(), Map.of("ATTACK", 20));
		service.acceptLive(alice);
		long oldSession = service.token();
		service.logout();
		service.acceptLive(observation("live:B:STANDARD", "Bob", "STANDARD", Map.of(), Map.of()));
		assertFalse(service.acceptLive(alice, oldSession, true));
		assertEquals("Bob", service.current().getUsername());
		service.logout();
		service.acceptLive(observation("live:A:DEADMAN", "Alice", "DEADMAN", Map.of(), Map.of()));
		assertFalse(service.acceptLive(alice, oldSession, true));
		assertEquals("DEADMAN", service.current().getMode());
		GuideRow activity = new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity("Unlock: boat")))
			.get(0);
		service.logout();
		service.acceptLive(alice);
		service.toggleManual(alice.getScope(), activity, true);
		ProgressService restart = new ProgressService(store);
		restart.acceptLive(alice);
		assertTrue(restart.current().getManual().contains(activity.getKey()));
		GuideRow changed = new GuideParser()
			.parse(GuideParserTest.table(GuideParserTest.activity("Unlock: another boat"))).get(0);
		assertEquals(Completion.State.UNKNOWN, Completion.of(changed, restart.current()).getState());
		assertEquals(Completion.State.UNKNOWN,
			Completion.of(activity, observation("live:B", "Bob", "STANDARD", Map.of(), Map.of())).getState());
	}

	@Test
	public void explicitSyncRefreshesTimestampAndPreservesManualChecks() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		AccountProgress first = new AccountProgress("live:A:STANDARD", "Alice", "STANDARD", "Live RuneLite",
			1000, 1000L, Map.of(), Map.of("ATTACK", 20), Map.of(), Set.of());
		GuideRow activity = new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity("Unlock: boat")))
			.get(0);
		service.acceptLive(first);
		service.toggleManual(first.getScope(), activity, true);
		AccountProgress next = new AccountProgress(first.getScope(), "Alice", "STANDARD", "Live RuneLite",
			2000, 2000L, first.getQuests(), first.getLevels(), Map.of(), Set.of());
		service.acceptLive(next);
		assertEquals(1000, service.current().getRetrievedAt());
		assertTrue(service.acceptLive(next, service.token(), true));
		assertEquals(2000, service.current().getRetrievedAt());
		assertTrue(service.current().getManual().contains(activity.getKey()));
		service.logout();
		service.acceptLive(observation("live:A:DEADMAN", "Alice", "DEADMAN", Map.of(), Map.of()));
		assertTrue(service.current().getManual().isEmpty());
		service.toggleManual(first.getScope(), activity, true);
		assertTrue(service.current().getManual().isEmpty());
	}

	@Test
	public void logoutDoesNotWaitForDiskOrRepublishAnOldCharacter() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		java.util.concurrent.CompletableFuture<Void> entered = new java.util.concurrent.CompletableFuture<>();
		java.util.concurrent.CompletableFuture<Void> release = new java.util.concurrent.CompletableFuture<>();
		doAnswer(i ->
		{
			entered.complete(null);
			release.get(5, java.util.concurrent.TimeUnit.SECONDS);
			return null;
		})
			.when(store).write(anyString(), anyString(), any());
		java.util.concurrent.ExecutorService worker = java.util.concurrent.Executors.newSingleThreadExecutor();
		try
		{
			AccountProgress account = observation("live:A:STANDARD", "Alice", "STANDARD", Map.of(), Map.of());
			long session = service.token();
			java.util.concurrent.Future<Boolean> result = worker
				.submit(() -> service.acceptLive(account, session, true));
			entered.get(5, java.util.concurrent.TimeUnit.SECONDS);
			java.util.concurrent.CompletableFuture.runAsync(service::logout).get(1,
				java.util.concurrent.TimeUnit.SECONDS);
			assertNull(service.current());
			release.complete(null);
			assertFalse(result.get(5, java.util.concurrent.TimeUnit.SECONDS));
			assertNull(service.current());
		}
		finally
		{
			release.complete(null);
			worker.shutdownNow();
		}
	}

	@Test
	public void liveReaderUsesRealLevelsAndRequiresEstablishedProfile()
	{
		Client client = mock(Client.class);
		ConfigManager config = mock(ConfigManager.class);
		Player player = mock(Player.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Alice");
		when(client.getAccountHash()).thenReturn(42L);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		when(config.getRSProfileKey()).thenReturn("rsprofile.a");
		when(config.getRSProfiles())
			.thenReturn(List.of(new RuneScapeProfile("Alice", RuneScapeProfileType.STANDARD, 42L, "rsprofile.a")));
		when(client.getIntStack()).thenReturn(new int[]
		{ 2 });
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(40);
		when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(99);
		AccountProgress read = new LiveProgressReader(client, config).read();
		assertNotNull(read);
		assertEquals(Integer.valueOf(40), read.getLevels().get("FIREMAKING"));
		assertEquals(AccountProgress.QuestStatus.COMPLETE, read.getQuests().get("COOKS_ASSISTANT"));
		assertEquals(Quest.values().length, read.getQuests().size());
		Set<String> expectedSkills = new HashSet<>();
		for (Skill skill : Skill.values())
		{
			expectedSkills.add(skill.name());
		}
		assertEquals(expectedSkills, read.getLevels().keySet());
		assertFalse(read.getLevels().containsKey("OVERALL"));
		verify(client, never()).getBoostedSkillLevel(any(Skill.class));
		when(client.getIntStack()).thenReturn(new int[]
		{ 0 });
		when(client.getRealSkillLevel(Skill.FIREMAKING)).thenReturn(0);
		AccountProgress partial = new LiveProgressReader(client, config).read();
		assertEquals(AccountProgress.QuestStatus.IN_PROGRESS, partial.getQuests().get("COOKS_ASSISTANT"));
		assertFalse(partial.getLevels().containsKey("FIREMAKING"));
		when(client.getIntStack()).thenReturn(new int[]
		{ 1 });
		assertEquals(AccountProgress.QuestStatus.INCOMPLETE,
			new LiveProgressReader(client, config).read().getQuests().get("COOKS_ASSISTANT"));
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.DEADMAN));
		assertNull(new LiveProgressReader(client, config).read());
		when(config.getRSProfiles())
			.thenReturn(List.of(new RuneScapeProfile("Alice", RuneScapeProfileType.DEADMAN, 42L, "rsprofile.a")));
		assertEquals("live:rsprofile.a:DEADMAN", new LiveProgressReader(client, config).read().getScope());
		when(config.getRSProfileKey()).thenReturn(null);
		assertNull(new LiveProgressReader(client, config).read());
		when(config.getRSProfileKey()).thenReturn("rsprofile.a");
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		assertNull(new LiveProgressReader(client, config).read());
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getAccountHash()).thenReturn(99L);
		assertNull(new LiveProgressReader(client, config).read());
	}
}
