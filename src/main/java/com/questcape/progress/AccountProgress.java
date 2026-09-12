package com.questcape.progress;

import java.util.*;
import lombok.Value;

@Value
public class AccountProgress
{
    public enum QuestStatus { INCOMPLETE, IN_PROGRESS, COMPLETE }
    int schema = 1;
    String scope;
    String username;
    String mode;
    String source;
    long retrievedAt;
    Long observedAt;
    Map<String, QuestStatus> quests;
    Map<String, Integer> levels;
    Map<String, Boolean> activities;
    Set<String> manual;

    public AccountProgress(String scope, String username, String mode, String source, long retrievedAt,
        Long observedAt, Map<String, QuestStatus> quests, Map<String, Integer> levels,
        Map<String, Boolean> activities, Set<String> manual)
    {
        this.scope = scope; this.username = username; this.mode = mode; this.source = source;
        this.retrievedAt = retrievedAt; this.observedAt = observedAt;
        this.quests = Collections.unmodifiableMap(new HashMap<>(quests));
        this.levels = Collections.unmodifiableMap(new HashMap<>(levels));
        this.activities = Collections.unmodifiableMap(new HashMap<>(activities));
        this.manual = Collections.unmodifiableSet(new HashSet<>(manual));
    }
    public AccountProgress withManual(Set<String> checks)
    { return new AccountProgress(scope, username, mode, source, retrievedAt, observedAt, quests, levels, activities, checks); }
}
