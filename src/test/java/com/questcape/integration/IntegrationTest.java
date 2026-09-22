package com.questcape.integration;

import com.questcape.*;
import java.io.*;
import java.util.*;
import net.runelite.client.config.*;
import net.runelite.client.plugins.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IntegrationTest
{
    @Test public void registryTracksEveryDependencyTransitionWithoutChangingConfiguration()
    {
        PluginManager manager = mock(PluginManager.class); Plugin plugin = mock(Plugin.class);
        List<Plugin> plugins = new ArrayList<>(); when(manager.getPlugins()).thenReturn(plugins);
        RuneLitePluginRegistry registry = new RuneLitePluginRegistry(manager, p -> RuneLitePluginRegistry.QUEST_HELPER);
        registry.refresh(); assertEquals(RuneLitePluginRegistry.State.ABSENT, registry.questHelper());
        plugins.add(plugin); registry.refresh(); assertEquals(RuneLitePluginRegistry.State.DISABLED, registry.questHelper());
        when(manager.isPluginEnabled(plugin)).thenReturn(true); registry.refresh(); assertEquals(RuneLitePluginRegistry.State.INACTIVE, registry.questHelper());
        when(manager.isPluginActive(plugin)).thenReturn(true); registry.refresh(); assertEquals(RuneLitePluginRegistry.State.ACTIVE, registry.questHelper());
        plugins.clear(); registry.refresh(); assertEquals(RuneLitePluginRegistry.State.ABSENT, registry.questHelper());
        plugins.add(plugin); registry.refresh(); assertEquals(RuneLitePluginRegistry.State.ACTIVE, registry.questHelper());
        RuneLitePluginRegistry impostor = new RuneLitePluginRegistry(manager, p -> "something.QuestHelperPlugin"); impostor.refresh();
        assertEquals(RuneLitePluginRegistry.State.ABSENT, impostor.questHelper());
        verify(manager, never()).setPluginEnabled(any(), anyBoolean());
    }
    @Test public void noLaunchConfirmationMeansNoRememberedQuest()
    {
        RuneLitePluginRegistry registry = mock(RuneLitePluginRegistry.class); QuestHelperBridge bridge = new QuestHelperBridge(registry);
        when(registry.questHelper()).thenReturn(RuneLitePluginRegistry.State.ACTIVE);
        assertEquals(QuestHelperBridge.State.LOGGED_OUT, bridge.launch("COOKS_ASSISTANT", false).getState());
        assertEquals(QuestHelperBridge.State.INCOMPATIBLE, bridge.launch("COOKS_ASSISTANT", true).getState());
        assertEquals(QuestHelperBridge.State.UNSUPPORTED, bridge.launch(null, true).getState()); assertFalse(bridge.canConfirmLaunch());
        ConfigManager config = mock(ConfigManager.class); when(config.getRSProfileKey()).thenReturn("rsprofile.a");
        ResumeIntentStore store = new ResumeIntentStore(config, new com.google.gson.Gson());
        store.remember("rsprofile.a", "live:rsprofile.a:STANDARD", "COOKS_ASSISTANT", bridge.launch("COOKS_ASSISTANT", true));
        verify(config, never()).setConfiguration(anyString(), anyString(), anyString(), anyString());
        store.remember("rsprofile.a", "live:rsprofile.a:STANDARD", "COOKS_ASSISTANT",
            new QuestHelperBridge.Result(QuestHelperBridge.State.SEARCH_READY, "Search opened"));
        store.remember("rsprofile.a", "live:rsprofile.a:STANDARD", "COOKS_ASSISTANT",
            new QuestHelperBridge.Result(QuestHelperBridge.State.RESULT_SELECTED, "Result arrow clicked"));
        verify(config, never()).setConfiguration(anyString(), anyString(), anyString(), anyString());
        assertTrue(new QuestCapeConfig() { }.resumeQuestOnLogin());
        assertEquals("questcape", QuestCapeConfig.class.getAnnotation(ConfigGroup.class).value());
        store.clear("rsprofile.a"); verify(config).unsetConfiguration("questcape", "rsprofile.a", "confirmedResumeQuest");
    }
    @Test public void trainingAliasesFallbackAndRedirectsAreConstrained() throws Exception
    {
        BoundedHttp http = mock(BoundedHttp.class); TrainingGuideResolver resolver = new TrainingGuideResolver(http);
        assertEquals(resolver.resolve("Runecrafting"), resolver.resolve("Runecraft"));
        assertEquals(resolver.resolve("Strength"), resolver.resolve("Attack")); assertEquals(TrainingGuideResolver.FALLBACK, resolver.resolve("New Skill"));
        for (String unsafe : List.of("http://theoatrix.net/a", "https://theoatrix.net.evil.test/a", "https://evil@theoatrix.net/a", "javascript:alert(1)", "https://theoatrix.net:123/a")) assertFalse(TrainingGuideResolver.safe(unsafe));
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(new BoundedHttp.Result(302, "", null, null, "https://evil.test/guide", null));
        try { resolver.verifyDestination(resolver.resolve("Firemaking")); fail(); } catch (IOException expected) { }
        verify(http, times(1)).get(anyString(), anyMap(), anyInt());
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(new BoundedHttp.Result(302, "", null, null, "/all-guides", null), new BoundedHttp.Result(200, "", null, null, null, null));
        assertEquals(TrainingGuideResolver.FALLBACK, resolver.verifyDestination(resolver.resolve("Firemaking")));
    }
    @Test public void browserFailureOffersRecoveryAndCannotChangeProgress() throws Exception
    {
        com.questcape.progress.AccountProgress account = com.questcape.progress.ProgressTest.observation("live:A", "Alice", "STANDARD", Map.of(), Map.of("FIREMAKING", 1));
        com.questcape.guide.GuideRow row = new com.questcape.guide.GuideParser().parse(com.questcape.guide.GuideParserTest.table(
            com.questcape.guide.GuideParserTest.activity("Train Firemaking from level 1 to level 40"))).get(0);
        BrowserLinks links = new BrowserLinks(url -> { throw new IllegalStateException("No browser"); });
        String url = new TrainingGuideResolver(null).resolve("Firemaking");
        assertTrue(links.open(url).contains("copy this link: " + url));
        assertEquals(com.questcape.progress.Completion.State.INCOMPLETE, com.questcape.progress.Completion.of(row, account).getState());
    }
}
