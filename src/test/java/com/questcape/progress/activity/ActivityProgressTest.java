package com.questcape.progress.activity;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.persistence.JsonStore;
import com.questcape.progress.compatibility.ActivityProgress;
import com.questcape.progress.evaluation.Completion;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressService;
import java.io.IOException;
import java.util.*;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ActivityProgressTest
{
	private AccountProgress account(Map<String, Boolean> facts, Map<String, AccountProgress.QuestStatus> quests)
	{
		return new AccountProgress("live:test:STANDARD", "Synthetic", "STANDARD", "Live RuneLite",
			1000, 1000L, quests, Map.of(), facts, Set.of());
	}

	private GuideRow row(String title) throws Exception
	{
		return new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity(title))).get(0);
	}

	@Test
	public void everyCapturedDiaryAndMiniquestHasAnAutomaticIdentity() throws Exception
	{
		for (GuideRow row : new GuideParser().parse(GuideParserTest.fixture()))
		{
			if (row.getKind() == GuideRow.Kind.DIARY || row.getKind() == GuideRow.Kind.MINIQUEST)
			{
				assertFalse(row.getTitle(), row.isManual());
				assertNotNull(row.getTitle(), row.getKind() == GuideRow.Kind.DIARY
					? row.getActivityIdentity() : row.getQuestIdentity());
			}
		}
		assertNull(ActivityProgress.diary("Hand in the diary of Herbi Flax"));
		assertNull(ActivityProgress.diary("Complete an easy task in the Morytania Diary"));
		assertNull(ActivityProgress.diary("Complete easy tasks in the Morytania Diary"));
		assertNull(ActivityProgress.diary("Complete Easy and Medium Ardougne Diary"));
	}

	@Test
	public void everyRegionAndTierKeepsCompletionSeparateFromRewards() throws Exception
	{
		Client client = mock(Client.class);
		String[] regions = {"Ardougne", "Desert", "Falador", "Fremennik", "Kandarin", "Karamja",
			"Kourend & Kebos", "Lumbridge & Draynor", "Morytania", "Varrock", "Western Provinces", "Wilderness"};
		for (String region : regions)
		{
			for (String tier : List.of("Easy", "Medium", "Hard", "Elite"))
			{
				GuideRow complete = row("Complete the " + tier + " " + region + " Diary");
				GuideRow reward = row("Claim the " + tier + " " + region + " Diary reward");
				assertEquals(GuideRow.Kind.DIARY, reward.getKind());
				assertNotNull(complete.getActivityIdentity());
				assertNotEquals(complete.getActivityIdentity(), reward.getActivityIdentity());
				Map<String, Boolean> facts = ActivityProgress.readDiaries(client);
				assertEquals(Boolean.FALSE, facts.get(complete.getActivityIdentity()));
				facts.put(complete.getActivityIdentity(), true);
				AccountProgress observation = account(facts, Map.of());
				assertEquals(Completion.State.COMPLETE, Completion.of(complete, observation).getState());
				assertEquals(Completion.State.INCOMPLETE, Completion.of(reward, observation).getState());
			}
		}
		when(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE)).thenReturn(1);
		Map<String, Boolean> facts = ActivityProgress.readDiaries(client);
		assertEquals(1, facts.values().stream().filter(Boolean::booleanValue).count());
		assertEquals(Boolean.TRUE, facts.get("DIARY:ARDOUGNE_EASY:complete"));
		when(client.getVarbitValue(VarbitID.ATJUN_EASY_DONE)).thenReturn(2);
		assertTrue(ActivityProgress.readDiaries(client).get("DIARY:KARAMJA_EASY:complete"));
		assertFalse(ActivityProgress.readDiaries(client).get("DIARY:KARAMJA_EASY:reward"));
	}

	@Test
	public void karamjaFirstThreeTiersCompleteAtTwoWhileEliteAndRewardsStaySeparate() throws Exception
	{
		Client client = mock(Client.class);
		int[] flags = {VarbitID.ATJUN_EASY_DONE, VarbitID.ATJUN_MED_DONE, VarbitID.ATJUN_HARD_DONE};
		String[] tiers = {"EASY", "MEDIUM", "HARD"};
		for (int tier = 0; tier < flags.length; tier++)
		{
			for (int value : new int[] {0, 1, 2, 3})
			{
				when(client.getVarbitValue(flags[tier])).thenReturn(value);
				Map<String, Boolean> facts = ActivityProgress.readDiaries(client);
				assertEquals("Karamja " + tiers[tier] + " state " + value,
					value == 2, facts.get("DIARY:KARAMJA_" + tiers[tier] + ":complete"));
				assertFalse(facts.get("DIARY:KARAMJA_" + tiers[tier] + ":reward"));
			}
			when(client.getVarbitValue(flags[tier])).thenReturn(0);
		}
		when(client.getVarbitValue(VarbitID.ATJUN_EASY_DONE)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.ATJUN_EASY_REWARD)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.KARAMJA_DIARY_ELITE_COMPLETE)).thenReturn(1);
		Map<String, Boolean> facts = ActivityProgress.readDiaries(client);
		assertTrue(facts.get("DIARY:KARAMJA_EASY:reward"));
		assertFalse(facts.get("DIARY:KARAMJA_MEDIUM:complete"));
		assertFalse(facts.get("DIARY:KARAMJA_HARD:complete"));
		assertTrue(facts.get("DIARY:KARAMJA_ELITE:complete"));
		assertEquals(Completion.State.COMPLETE,
			Completion.of(row("Easy Karamja Diary"), account(facts, Map.of())).getState());
		when(client.getVarbitValue(VarbitID.KARAMJA_DIARY_ELITE_COMPLETE)).thenReturn(2);
		assertFalse(ActivityProgress.readDiaries(client).get("DIARY:KARAMJA_ELITE:complete"));
	}

	@Test
	public void miniquestPredicatesRespectPartialProgressAndRewardCollection() throws Exception
	{
		Client client = mock(Client.class);
		Map<String, AccountProgress.QuestStatus> quests = new HashMap<>();
		ActivityProgress.readMiniquests(client, quests);
		assertEquals(AccountProgress.QuestStatus.INCOMPLETE, quests.get("NATURAL_HISTORY_QUIZ"));
		when(client.getVarbitValue(VarbitID.VM_NATHIS_AGREED)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.VM_NATHIS_DISPLAY_ALLDONE)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.KR_KNIGHTWAVES_STATE)).thenReturn(7);
		ActivityProgress.readMiniquests(client, quests);
		assertEquals(AccountProgress.QuestStatus.IN_PROGRESS, quests.get("NATURAL_HISTORY_QUIZ"));
		assertEquals(AccountProgress.QuestStatus.IN_PROGRESS, quests.get("KNIGHT_WAVES_TRAINING_GROUNDS"));
		when(client.getVarbitValue(VarbitID.VM_NATHIS_REWARD_GIVEN)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.KR_KNIGHTWAVES_STATE)).thenReturn(8);
		ActivityProgress.readMiniquests(client, quests);
		assertEquals(AccountProgress.QuestStatus.COMPLETE, quests.get("NATURAL_HISTORY_QUIZ"));
		assertEquals(AccountProgress.QuestStatus.COMPLETE, quests.get("KNIGHT_WAVES_TRAINING_GROUNDS"));
		GuideRow start = row("Start miniquest: Natural history quiz");
		GuideRow finish = row("Natural history quiz (miniquest)");
		AccountProgress partial = account(Map.of(), Map.of("NATURAL_HISTORY_QUIZ", AccountProgress.QuestStatus.IN_PROGRESS));
		assertEquals(Completion.State.COMPLETE, Completion.of(start, partial).getState());
		assertEquals(Completion.State.IN_PROGRESS, Completion.of(finish, partial).getState());
	}

	@Test
	public void retiredChecksCannotOverrideFactsOrBeToggled() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		GuideRow diary = row("Easy Ardougne Diary"), miniquest = row("Natural history quiz (miniquest)");
		GuideRow unlock = row("Unlock: Varrock Museum Kudos");
		AccountProgress old = account(Map.of(), Map.of()).withManual(Set.of(diary.getKey(), miniquest.getKey(), unlock.getKey()));
		when(store.read("progress", old.getScope(), AccountProgress.class)).thenReturn(old);
		ProgressService service = new ProgressService(store);
		service.acceptLive(account(Map.of(diary.getActivityIdentity(), false), Map.of("NATURAL_HISTORY_QUIZ", AccountProgress.QuestStatus.INCOMPLETE)));
		assertEquals(Completion.State.INCOMPLETE, Completion.of(diary, service.current()).getState());
		assertEquals(Completion.State.INCOMPLETE, Completion.of(miniquest, service.current()).getState());
		assertEquals(Completion.State.COMPLETE, Completion.of(unlock, service.current()).getState());
		clearInvocations(store);
		service.toggleManual(old.getScope(), diary, true);
		service.toggleManual(old.getScope(), miniquest, false);
		verifyNoInteractions(store);
		GuideRow unknown = row("Future miniquest (miniquest)");
		assertEquals(Completion.State.UNKNOWN, Completion.of(unknown, service.current()).getState());
		assertFalse(unknown.isManual());
		for (String title : List.of("Claim the Easy Future Diary reward", "Claim Future reward (miniquest)"))
		{
			GuideRow future = row(title);
			assertFalse(future.isManual());
			assertEquals(Completion.State.UNKNOWN, Completion.of(future, service.current()).getState());
		}
	}

	@Test
	public void storageFailureKeepsLiveStateAndExplicitSyncRecovers() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		doThrow(new IOException("denied")).when(store).write(anyString(), anyString(), any());
		AccountProgress observation = account(Map.of("DIARY:ARDOUGNE_EASY:complete", true), Map.of());
		assertTrue(service.acceptLive(observation, service.token(), false));
		assertEquals(observation.getActivities(), service.current().getActivities());
		assertFalse(service.persistenceStatus().isEmpty());
		service.acceptLive(observation);
		verify(store, times(1)).write(anyString(), anyString(), any());
		doNothing().when(store).write(anyString(), anyString(), any());
		service.acceptLive(observation, service.token(), true);
		assertEquals("", service.persistenceStatus());
		verify(store, times(2)).write(anyString(), anyString(), any());
	}

	@Test
	public void unreadableCacheIsRetainedUntilExplicitRecovery() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		AccountProgress observation = account(Map.of(), Map.of());
		when(store.read("progress", observation.getScope(), AccountProgress.class))
			.thenThrow(new IOException("unknown schema"));
		assertTrue(service.acceptLive(observation, service.token(), false));
		assertNotNull(service.current());
		assertFalse(service.persistenceStatus().isEmpty());
		verify(store, never()).write(anyString(), anyString(), any());
		GuideRow unlock = row("Unlock: Varrock Museum Kudos"), savedUnlock = row("Unlock: More Varrock Museum Kudos");
		service.toggleManual(observation.getScope(), unlock, true);
		verify(store, never()).write(anyString(), anyString(), any());
		doReturn(observation.withManual(Set.of(savedUnlock.getKey()))).when(store)
			.read("progress", observation.getScope(), AccountProgress.class);
		service.acceptLive(observation, service.token(), true);
		assertEquals(Set.of(unlock.getKey(), savedUnlock.getKey()), service.current().getManual());
		assertEquals("", service.persistenceStatus());
		verify(store).write(eq("progress"), eq(observation.getScope()), any());
	}
}
