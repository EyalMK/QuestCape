package com.questcape.integration;

import com.questcape.QuestCapeConfig;
import com.questcape.progress.AccountProgress;
import javax.inject.*;

/** Client-thread state machine. No transport assumptions: launch requires a confirming bridge. */
@Singleton
public class ResumeCoordinator
{
    static final int MAX_READINESS_TICKS = 50;
    private final QuestCapeConfig config;
    private final ResumeIntentStore store;
    private final QuestHelperBridge bridge;
    private boolean sessionOpen, eligible;
    private int ticks;
    private String sessionScope;
    private Attempt attempt;

    private static final class Attempt
    {
        final String profile, scope, quest;
        final boolean automatic;
        int remaining;
        boolean requested;
        Attempt(String profile, String scope, String quest, boolean automatic, int remaining)
        { this.profile = profile; this.scope = scope; this.quest = quest; this.automatic = automatic; this.remaining = remaining; }
    }

    @Inject public ResumeCoordinator(QuestCapeConfig config, ResumeIntentStore store, QuestHelperBridge bridge)
    { this.config = config; this.store = store; this.bridge = bridge; }

    /** Repeated LOGGED_IN and hop/reconnect events keep the same budget and completed decision. */
    public void login()
    {
        if (sessionOpen) return;
        sessionOpen = true; eligible = config.resumeQuestOnLogin(); ticks = 0; sessionScope = null; attempt = null;
    }
    public void logout() { sessionOpen = false; eligible = false; sessionScope = null; attempt = null; }
    public boolean needsTick() { return !sessionOpen || eligible || attempt != null; }
    public void preferenceChanged()
    {
        if (!config.resumeQuestOnLogin())
        {
            eligible = false;
            if (attempt != null && attempt.automatic) attempt = null;
        }
        // Enabling cannot re-arm this session; only the next login reads the new preference.
    }
    public void clear(String profile) { eligible = false; attempt = null; store.clear(profile); }

    public QuestHelperBridge.Result manual(String profile, AccountProgress live, String quest)
    {
        boolean ready = established(profile, live);
        if (ready) { login(); sessionScope = live.getScope(); }
        eligible = false; attempt = null;
        QuestHelperBridge.Result result = bridge.launch(quest, ready);
        if (!ready) return result;
        store.remember(profile, live.getScope(), quest, result);
        if (result.getState() == QuestHelperBridge.State.PENDING)
        {
            attempt = new Attempt(profile, live.getScope(), quest, false, MAX_READINESS_TICKS);
            attempt.requested = true;
        }
        return result;
    }

    /** Called once per logged-in game tick with a fresh ClientThread observation, never remote/cache data. */
    public String tick(String profile, AccountProgress live)
    {
        login();
        boolean ready = established(profile, live);
        if (ready)
        {
            if (sessionScope != null && !sessionScope.equals(live.getScope())) { logout(); login(); }
            sessionScope = live.getScope();
        }
        preferenceChanged();
        if (attempt == null && eligible)
        {
            if (++ticks > MAX_READINESS_TICKS) { eligible = false; return "Login resume skipped: account or quest state did not become ready."; }
            if (!ready) return null;
            ResumeIntentStore.Intent intent = store.current(profile, live.getScope());
            eligible = false;
            if (intent == null) return null;
            attempt = new Attempt(profile, live.getScope(), intent.getQuest(), true, MAX_READINESS_TICKS - ticks + 1);
        }
        if (attempt == null) return null;
        if (--attempt.remaining < 0) return finish(attempt.requested
            ? "Quest activation could not be confirmed. No new resume target was saved."
            : "Login resume skipped: " + bridge.availability() + ". Select a quest manually to retry.");
        if (!ready || !attempt.profile.equals(profile) || !attempt.scope.equals(live.getScope())) return null;
        AccountProgress.QuestStatus state = live.getQuests().get(attempt.quest);
        if (state == AccountProgress.QuestStatus.COMPLETE)
        {
            ResumeIntentStore.Intent saved = store.current(profile, live.getScope());
            if (saved != null && saved.getQuest().equals(attempt.quest)) store.clear(profile);
            return finish("Remembered quest is complete; login resume skipped.");
        }
        if (state == null || !bridge.canConfirmLaunch()) return null;
        QuestHelperBridge.Selection selected = bridge.selectedHelper();
        if (!selected.isKnown()) return null;
        if (attempt.quest.equals(selected.getQuestIdentity()))
        {
            store.remember(profile, live.getScope(), attempt.quest, new QuestHelperBridge.Result(QuestHelperBridge.State.ALREADY_ACTIVE, ""));
            return finish(attempt.requested ? "Quest activation confirmed." : "Remembered quest is already active.");
        }
        if (selected.getQuestIdentity() != null)
            return attempt.automatic ? finish("Another quest helper is active; login resume skipped.") : null;
        if (attempt.requested) return null;
        attempt.requested = true;
        QuestHelperBridge.Result result = bridge.launch(attempt.quest, true);
        store.remember(profile, live.getScope(), attempt.quest, result);
        return result.getState() == QuestHelperBridge.State.PENDING ? null : finish(result.getMessage());
    }

    private String finish(String message) { attempt = null; eligible = false; return message; }
    private static boolean established(String profile, AccountProgress live)
    { return live != null && "Live RuneLite".equals(live.getSource()) && ResumeIntentStore.validScope(profile, live.getScope()); }
}
