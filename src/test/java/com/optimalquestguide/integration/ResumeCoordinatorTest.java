package com.optimalquestguide.integration;

import com.google.gson.Gson;
import com.optimalquestguide.OptimalQuestGuideConfig;
import com.optimalquestguide.progress.*;
import java.util.*;
import net.runelite.client.config.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Adapter simulations prove local lifecycle policy, not distributed Quest Helper compatibility. */
public class ResumeCoordinatorTest
{
    private static final String A = "rsprofile.a", B = "rsprofile.b", QUEST = "COOKS_ASSISTANT", OTHER = "IN_SEARCH_OF_KNOWLEDGE";
    private static final QuestHelperBridge.Result CONFIRMED = new QuestHelperBridge.Result(QuestHelperBridge.State.CONFIRMED, "Quest started.");
    private final Map<String, String> persisted = new HashMap<>();
    private final ConfigManager manager = mock(ConfigManager.class);
    private final OptimalQuestGuideConfig config = mock(OptimalQuestGuideConfig.class);
    private final QuestHelperBridge bridge = mock(QuestHelperBridge.class);
    private String profile = A;
    private boolean enabled = true;
    private ResumeIntentStore store;
    private ResumeCoordinator coordinator;

    @Before public void setup()
    {
        when(manager.getRSProfileKey()).thenAnswer(i -> profile);
        when(manager.getConfiguration(eq(OptimalQuestGuideConfig.GROUP), anyString(), eq(ResumeIntentStore.KEY)))
            .thenAnswer(i -> persisted.get(i.getArgument(1)));
        doAnswer(i -> { persisted.put(i.getArgument(1), i.getArgument(3)); return null; })
            .when(manager).setConfiguration(eq(OptimalQuestGuideConfig.GROUP), anyString(), eq(ResumeIntentStore.KEY), anyString());
        doAnswer(i -> { persisted.remove(i.getArgument(1)); return null; })
            .when(manager).unsetConfiguration(eq(OptimalQuestGuideConfig.GROUP), anyString(), eq(ResumeIntentStore.KEY));
        when(config.resumeQuestOnLogin()).thenAnswer(i -> enabled);
        when(bridge.canConfirmLaunch()).thenReturn(true);
        when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, null));
        when(bridge.launch(anyString(), eq(true))).thenReturn(CONFIRMED);
        when(bridge.launch(anyString(), eq(false))).thenReturn(new QuestHelperBridge.Result(QuestHelperBridge.State.LOGGED_OUT, "Log in first."));
        when(bridge.availability()).thenReturn("Quest Helper unavailable");
        store = new ResumeIntentStore(manager, new Gson());
        coordinator = new ResumeCoordinator(config, store, bridge);
    }
    private AccountProgress live(String id)
    { return ProgressTest.observation("live:" + id + ":STANDARD", "Player", "STANDARD", Map.of(QUEST, AccountProgress.QuestStatus.IN_PROGRESS, OTHER, AccountProgress.QuestStatus.IN_PROGRESS), Map.of()); }
    private void remember(String quest) { store.remember(profile, live(profile).getScope(), quest, CONFIRMED); }
    private List<String> ticks(int count, AccountProgress live)
    {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) { String message = coordinator.tick(profile, live); if (message != null) messages.add(message); }
        return messages;
    }

    @Test public void defaultResumeLaunchesOnceAcrossDuplicateEventsAndHopsThenSurvivesRestart()
    {
        remember(QUEST); coordinator.login(); assertEquals("Quest started.", coordinator.tick(A, live(A)));
        for (int i = 0; i < 100; i++) { coordinator.login(); assertNull(coordinator.tick(A, live(A))); }
        verify(bridge, times(1)).launch(QUEST, true);
        coordinator = new ResumeCoordinator(config, new ResumeIntentStore(manager, new Gson()), bridge);
        coordinator.tick(A, live(A)); verify(bridge, times(2)).launch(QUEST, true);
    }

    @Test public void existingSameHelperIsANoopAndConflictingHelperWinsForTheWholeSession()
    {
        remember(QUEST); when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, QUEST));
        assertTrue(coordinator.tick(A, live(A)).contains("already active")); verify(bridge, never()).launch(anyString(), anyBoolean());
        coordinator.logout(); when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, OTHER));
        assertTrue(coordinator.tick(A, live(A)).contains("Another quest helper"));
        when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, null));
        assertTrue(ticks(100, live(A)).isEmpty()); verify(bridge, never()).launch(anyString(), anyBoolean());
    }

    @Test public void preferenceCancellationAndEnablingApplyOnlyOnNextLoginIncludingCoordinatorRestart()
    {
        remember(QUEST); when(bridge.canConfirmLaunch()).thenReturn(false);
        coordinator.tick(A, live(A)); enabled = false; coordinator.preferenceChanged();
        when(bridge.canConfirmLaunch()).thenReturn(true); ticks(100, live(A));
        coordinator = new ResumeCoordinator(config, new ResumeIntentStore(manager, new Gson()), bridge);
        ticks(100, live(A)); enabled = true; coordinator.preferenceChanged(); ticks(100, live(A));
        verify(bridge, never()).launch(anyString(), anyBoolean()); assertNotNull(store.current(A, live(A).getScope()));
        coordinator.logout(); coordinator.login(); coordinator.tick(A, live(A)); verify(bridge).launch(QUEST, true);
    }

    @Test public void manualSelectionStillConfirmsWhenResumeIsOffAndPendingIsNeverPersisted()
    {
        enabled = false; remember(QUEST);
        when(bridge.launch(OTHER, true)).thenReturn(new QuestHelperBridge.Result(QuestHelperBridge.State.PENDING, "Waiting"));
        coordinator.manual(A, live(A), OTHER);
        assertEquals(QUEST, store.current(A, live(A).getScope()).getQuest()); coordinator.preferenceChanged();
        when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, OTHER));
        assertTrue(coordinator.tick(A, live(A)).contains("confirmed"));
        assertEquals(OTHER, store.current(A, live(A).getScope()).getQuest()); assertFalse(enabled);
    }

    @Test public void completionAndExplicitClearSuppressResumeAndCancelPendingConfirmation()
    {
        remember(QUEST);
        AccountProgress complete = ProgressTest.observation(live(A).getScope(), "Player", "STANDARD", Map.of(QUEST, AccountProgress.QuestStatus.COMPLETE), Map.of());
        assertTrue(coordinator.tick(A, complete).contains("complete")); assertNull(store.current(A, live(A).getScope()));
        verify(bridge, never()).launch(anyString(), anyBoolean());
        remember(QUEST); coordinator.logout(); when(bridge.canConfirmLaunch()).thenReturn(false);
        coordinator.tick(A, live(A)); coordinator.clear(A); when(bridge.canConfirmLaunch()).thenReturn(true);
        when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, QUEST));
        assertTrue(ticks(100, live(A)).isEmpty()); assertNull(store.current(A, live(A).getScope()));
    }

    @Test public void lateConfirmationFromPriorAccountOrModeCannotCrossScopes()
    {
        remember(QUEST); when(bridge.launch(OTHER, true)).thenReturn(new QuestHelperBridge.Result(QuestHelperBridge.State.PENDING, "Waiting"));
        coordinator.manual(A, live(A), OTHER); profile = B;
        when(bridge.selectedHelper()).thenReturn(new QuestHelperBridge.Selection(true, OTHER));
        coordinator.tick(B, live(B)); assertNull(store.current(B, live(B).getScope()));
        store.remember(A, live(A).getScope(), OTHER, CONFIRMED); // A stale callback after the profile switch.
        profile = A; assertEquals(QUEST, store.current(A, live(A).getScope()).getQuest());
        coordinator.manual(A, live(A), OTHER);
        AccountProgress mode = ProgressTest.observation("live:" + A + ":DEADMAN", "Player", "DEADMAN", live(A).getQuests(), Map.of());
        coordinator.tick(A, mode); assertNull(store.current(A, mode.getScope()));
        assertEquals(QUEST, store.current(A, live(A).getScope()).getQuest());
    }

    @Test public void missingIdentityUnknownQuestAndUnavailableDependencyHaveABoundedWindowAndOneMessage()
    {
        assertEquals(1, ticks(200, null).size()); assertFalse(coordinator.needsTick());
        coordinator.logout(); remember(QUEST); when(bridge.canConfirmLaunch()).thenReturn(false);
        List<String> messages = ticks(200, live(A)); assertEquals(1, messages.size()); assertTrue(messages.get(0).contains("unavailable"));
        assertNotNull(store.current(A, live(A).getScope())); verify(bridge, never()).launch(anyString(), anyBoolean());
        coordinator.logout(); when(bridge.canConfirmLaunch()).thenReturn(true);
        AccountProgress unknown = ProgressTest.observation(live(A).getScope(), "Player", "STANDARD", Map.of(), Map.of());
        assertEquals(1, ticks(200, unknown).size()); verify(bridge, never()).launch(anyString(), anyBoolean());
    }

    @Test public void unconfirmedLaunchTimesOutWithoutReplacingLastConfirmedIntentOrRetryingLaunch()
    {
        remember(QUEST); when(bridge.launch(OTHER, true)).thenReturn(new QuestHelperBridge.Result(QuestHelperBridge.State.PENDING, "Waiting"));
        coordinator.manual(A, live(A), OTHER);
        assertEquals(1, ticks(200, live(A)).size()); assertEquals(QUEST, store.current(A, live(A).getScope()).getQuest());
        verify(bridge, times(1)).launch(OTHER, true); assertFalse(coordinator.needsTick());
        when(bridge.launch(OTHER, true)).thenReturn(new QuestHelperBridge.Result(QuestHelperBridge.State.FAILED, "Failed"));
        coordinator.manual(A, live(A), OTHER); assertEquals(QUEST, store.current(A, live(A).getScope()).getQuest());
        coordinator.manual(A, null, OTHER); assertEquals(QUEST, store.current(A, live(A).getScope()).getQuest());
    }

    @Test public void profileIntentIsVersionedCanonicalAndValidatedWithoutDiscardingUnrecognizedData()
    {
        remember(QUEST); ResumeIntentStore.Intent intent = store.current(A, live(A).getScope());
        assertTrue(intent.getLastConfirmedAt() > 0); assertEquals(A, intent.getProfile());
        String good = persisted.get(A);
        for (String invalid : List.of("broken", "\"COOKS_ASSISTANT\"", good.replace("\"schema\":1", "\"schema\":42"), good.replace(QUEST, "UNMAPPED")))
        { persisted.put(A, invalid); assertNull(store.current(A, live(A).getScope())); assertEquals(invalid, persisted.get(A)); }
        persisted.put(A, good); profile = B; assertNull(store.current(A, live(A).getScope())); store.clear(A); assertEquals(good, persisted.get(A));
    }

    @Test public void runeLiteSettingsDescriptorExposesAnOrdinaryVisibleDefaultTrueBoolean()
    {
        when(manager.getConfigDescriptor(any())).thenCallRealMethod();
        ConfigDescriptor descriptor = manager.getConfigDescriptor(new OptimalQuestGuideConfig() { });
        assertEquals("optimalquestguide", descriptor.getGroup().value()); assertEquals(1, descriptor.getItems().size());
        ConfigItemDescriptor item = descriptor.getItems().iterator().next();
        assertEquals("resumeQuestOnLogin", item.getItem().keyName()); assertEquals(boolean.class, item.getType());
        assertFalse(item.getItem().hidden()); assertTrue(new OptimalQuestGuideConfig() { }.resumeQuestOnLogin());
    }
}
