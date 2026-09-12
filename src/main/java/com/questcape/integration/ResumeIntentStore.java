package com.questcape.integration;

import com.questcape.QuestCapeConfig;
import com.google.gson.Gson;
import javax.inject.*;
import lombok.Value;
import net.runelite.api.Quest;
import net.runelite.client.config.ConfigManager;

/** Confirmation is mandatory; the currently distributed bridge never writes an intent. */
@Singleton
public class ResumeIntentStore
{
    static final String KEY = "confirmedResumeQuest";
    private final ConfigManager config;
    private final Gson gson;
    @Value public static class Intent
    {
        int schema;
        String profile;
        String scope;
        String quest;
        long lastConfirmedAt;
        public Intent(String profile, String scope, String quest, long lastConfirmedAt)
        { this.schema = 1; this.profile = profile; this.scope = scope; this.quest = quest; this.lastConfirmedAt = lastConfirmedAt; }
    }
    @Inject public ResumeIntentStore(ConfigManager config, Gson gson) { this.config = config; this.gson = gson; }
    public void remember(String expectedProfile, String scope, String quest, QuestHelperBridge.Result result)
    {
        if (!isCurrent(expectedProfile) || !validScope(expectedProfile, scope) || !canonical(quest) || result == null) return;
        if (result.getState() == QuestHelperBridge.State.CONFIRMED || result.getState() == QuestHelperBridge.State.ALREADY_ACTIVE)
            config.setConfiguration(QuestCapeConfig.GROUP, expectedProfile, KEY,
                gson.toJson(new Intent(expectedProfile, scope, quest, System.currentTimeMillis())));
    }
    public void clear(String expectedProfile)
    {
        if (isCurrent(expectedProfile)) config.unsetConfiguration(QuestCapeConfig.GROUP, expectedProfile, KEY);
    }
    public Intent current(String expectedProfile, String scope)
    {
        if (!isCurrent(expectedProfile) || !validScope(expectedProfile, scope)) return null;
        try
        {
            Intent intent = gson.fromJson(config.getConfiguration(QuestCapeConfig.GROUP, expectedProfile, KEY), Intent.class);
            return intent != null && intent.getSchema() == 1 && expectedProfile.equals(intent.getProfile())
                && scope.equals(intent.getScope()) && canonical(intent.getQuest()) && intent.getLastConfirmedAt() > 0 ? intent : null;
        }
        catch (RuntimeException e) { return null; } // Retain malformed/old data, but never launch from it.
    }
    private boolean isCurrent(String profile) { return profile != null && profile.equals(config.getRSProfileKey()); }
    static boolean validScope(String profile, String scope) { return profile != null && scope != null && scope.startsWith("live:" + profile + ":"); }
    private static boolean canonical(String quest)
    { try { return quest != null && Quest.valueOf(quest) != null; } catch (IllegalArgumentException e) { return false; } }
}
