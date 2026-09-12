package com.questcape.progress;

import com.google.gson.Gson;
import com.questcape.guide.*;
import com.questcape.persistence.JsonStore;
import com.questcape.integration.BoundedHttp;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.client.config.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProgressTest
{
    public static AccountProgress observation(String scope, String name, String mode, Map<String, AccountProgress.QuestStatus> quests, Map<String, Integer> levels)
    { return new AccountProgress(scope, name, mode, "Live RuneLite", System.currentTimeMillis(), System.currentTimeMillis(), quests, levels, Map.of(), Set.of()); }
    @Test public void thresholdQuestStageAndManualSemantics() throws Exception
    {
        List<GuideRow> rows = new GuideParser().parse(GuideParserTest.table(
            GuideParserTest.activity("Train Firemaking from level 34 to level 40") + GuideParserTest.activity("Train Firemaking from level 40 to level 49")
            + GuideParserTest.activity("Hand in Herbi Flax diary, from level 25 to level 25") + GuideParserTest.activity("Unlock: balloon route")
            + GuideParserTest.activity("Start miniquest: <a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a>")
            + GuideParserTest.activity("<a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a> (miniquest)")));
        AccountProgress account = observation("live:A", "Alice", "STANDARD", Map.of("IN_SEARCH_OF_KNOWLEDGE", AccountProgress.QuestStatus.IN_PROGRESS), Map.of("FIREMAKING", 40, "HERBLORE", 99));
        assertEquals(Completion.State.COMPLETE, Completion.of(rows.get(0), account).getState());
        assertEquals(Completion.State.INCOMPLETE, Completion.of(rows.get(1), account).getState());
        assertEquals(Completion.State.UNKNOWN, Completion.of(rows.get(2), account).getState());
        assertEquals(Completion.State.COMPLETE, Completion.of(rows.get(4), account).getState());
        assertEquals(Completion.State.IN_PROGRESS, Completion.of(rows.get(5), account).getState());
        ProgressService service = new ProgressService(new JsonStore(new Gson(), Files.createTempDirectory("oqg-progress")));
        service.acceptLive(account); service.toggleManual("live:A", rows.get(2), true);
        assertTrue(Completion.of(rows.get(2), service.current()).isManual());
        assertEquals(Completion.State.COMPLETE, Completion.of(rows.get(2), service.current()).getState());
        service.toggleManual("live:A", rows.get(2), false); assertEquals(Completion.State.UNKNOWN, Completion.of(rows.get(2), service.current()).getState());
        assertEquals("RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE", Identities.quest("Recipe for Disaster/Freeing the Goblin generals"));
    }
    @Test public void persistenceRaceAccountAndModeIsolation() throws Exception
    {
        Path root = Files.createTempDirectory("oqg-isolation"); JsonStore store = new JsonStore(new Gson(), root);
        ProgressService service = new ProgressService(store);
        AccountProgress alice = observation("live:A:STANDARD", "Alice", "STANDARD", Map.of(), Map.of("ATTACK", 20));
        service.acceptLive(alice); long oldRequest = service.useCurrent(), latestRequest = service.useCurrent();
        AccountProgress remote = new AccountProgress(WikiSyncProgressProvider.scope("Alice"), "Alice", "STANDARD", "WikiSync", 10, 5L, Map.of(), Map.of("ATTACK", 99), Map.of(), Set.of());
        assertFalse(service.acceptCurrentRemote(oldRequest, alice.getScope(), remote));
        assertTrue(service.acceptCurrentRemote(latestRequest, alice.getScope(), remote));
        assertEquals(Integer.valueOf(20), service.viewed().getLevels().get("ATTACK"));
        service.logout(); service.acceptLive(observation("live:B:STANDARD", "Bob", "STANDARD", Map.of(), Map.of()));
        assertFalse(service.acceptCurrentRemote(latestRequest, alice.getScope(), remote)); assertNull(service.remoteViewed());
        service.logout(); service.acceptLive(observation("live:A:DEADMAN", "Alice", "DEADMAN", Map.of(), Map.of()));
        assertFalse(service.acceptCurrentRemote(service.useCurrent(), "live:A:DEADMAN", remote)); assertNull(service.remoteViewed());
        GuideRow activity = new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity("Unlock: boat"))).get(0);
        service.logout(); service.acceptLive(alice); service.toggleManual(alice.getScope(), activity, true);
        ProgressService restart = new ProgressService(store); restart.acceptLive(alice);
        assertTrue(restart.current().getManual().contains(activity.getKey()));
        GuideRow changed = new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity("Unlock: another boat"))).get(0);
        assertEquals(Completion.State.UNKNOWN, Completion.of(changed, restart.current()).getState());
        assertEquals(Completion.State.UNKNOWN, Completion.of(activity, observation("live:B", "Bob", "STANDARD", Map.of(), Map.of())).getState());
    }
    @Test public void wikiSyncFieldsTimestampsNamesFreshRequestsAndFailures() throws Exception
    {
        String json;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/wikisync-snooze-meist.json")) { json = new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8); }
        BoundedHttp http = mock(BoundedHttp.class);
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(new BoundedHttp.Result(200, json, null, null, null, null));
        WikiSyncProgressProvider provider = new WikiSyncProgressProvider(http);
        AccountProgress first = provider.lookup("  snooze meist  "); provider.lookup("snooze_meist");
        assertEquals("https://sync.runescape.wiki/runelite/player/snooze_meist/STANDARD", WikiSyncProgressProvider.url(" snooze meist "));
        assertEquals(WikiSyncProgressProvider.identity("snooze meist"), WikiSyncProgressProvider.identity("snooze_meist"));
        assertEquals(AccountProgress.QuestStatus.COMPLETE, first.getQuests().get("COOKS_ASSISTANT"));
        assertEquals(AccountProgress.QuestStatus.IN_PROGRESS, first.getQuests().get("CONTACT"));
        assertEquals(Integer.valueOf(49), first.getLevels().get("FIREMAKING")); assertNotEquals(Long.valueOf(first.getRetrievedAt()), first.getObservedAt());
        verify(http, times(2)).get(eq(WikiSyncProgressProvider.url("snooze meist")), eq(Map.of("Cache-Control", "no-cache")), eq(1_000_000));
        for (String invalid : Arrays.asList("", "    ", "a/b", "a%2fb", "x?y", "x#y", "longplayername123"))
            try { WikiSyncProgressProvider.url(invalid); fail(); } catch (IllegalArgumentException expected) { }
        AccountProgress partial = provider.parse("Alice", "{\"username\":\"Alice\",\"levels\":{\"Attack\":99.5},\"quests\":{\"Cook's Assistant\":null}}", 123);
        assertTrue(partial.getLevels().isEmpty()); assertTrue(partial.getQuests().isEmpty()); assertNull(partial.getObservedAt());
        try { provider.parse("Alice", json, 123); fail(); } catch (IOException expected) { }
        for (int status : new int[]{400, 403, 429, 500})
        {
            when(http.get(anyString(), anyMap(), anyInt())).thenReturn(new BoundedHttp.Result(status, "{\"code\":\"NO_USER_DATA\"}", null, null, null, "60"));
            try { provider.lookup("Alice"); fail(); } catch (IOException expected) { assertFalse(expected.getMessage().isEmpty()); }
        }
    }
    @Test public void liveReaderUsesRealLevelsAndRequiresEstablishedProfile()
    {
        Client client = mock(Client.class); ConfigManager config = mock(ConfigManager.class); Player player = mock(Player.class);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN); when(client.getLocalPlayer()).thenReturn(player); when(player.getName()).thenReturn("Alice");
        when(client.getAccountHash()).thenReturn(42L); when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
        when(config.getRSProfileKey()).thenReturn("rsprofile.a"); when(config.getRSProfiles()).thenReturn(List.of(new RuneScapeProfile("Alice", RuneScapeProfileType.STANDARD, 42L, "rsprofile.a")));
        when(client.getIntStack()).thenReturn(new int[]{2}); when(client.getRealSkillLevel(any(Skill.class))).thenReturn(40);
        when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(99);
        AccountProgress read = new LiveProgressReader(client, config).read();
        assertNotNull(read); assertEquals(Integer.valueOf(40), read.getLevels().get("FIREMAKING"));
        assertEquals(AccountProgress.QuestStatus.COMPLETE, read.getQuests().get("COOKS_ASSISTANT"));
        verify(client, never()).getBoostedSkillLevel(any(Skill.class));
        when(client.getAccountHash()).thenReturn(99L); assertNull(new LiveProgressReader(client, config).read());
    }
}
