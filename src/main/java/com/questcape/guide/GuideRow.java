package com.questcape.guide;

import java.util.*;
import lombok.Value;

@Value
public class GuideRow
{
    public enum Kind { QUEST, MINIQUEST, TRAINING, DIARY, UNLOCK, ACTIVITY, UNKNOWN, INFORMATION }
    String key;
    int position;
    Kind kind;
    String title;
    String wikiTarget;
    String questIdentity;
    Map<String, Integer> targets;
    Map<String, String> fields;
    Map<String, String> links;

    public GuideRow(String key, int position, Kind kind, String title, String wikiTarget,
        String questIdentity, Map<String, Integer> targets, Map<String, String> fields, Map<String, String> links)
    {
        this.key = key;
        this.position = position;
        this.kind = kind;
        this.title = title;
        this.wikiTarget = wikiTarget;
        this.questIdentity = questIdentity;
        this.targets = Collections.unmodifiableMap(new LinkedHashMap<>(targets));
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        this.links = Collections.unmodifiableMap(new LinkedHashMap<>(links));
    }

    public boolean isManual() { return !key.contains(":repeated:") && (kind == Kind.ACTIVITY || kind == Kind.UNLOCK || kind == Kind.DIARY); }
    public boolean isActionable() { return kind != Kind.INFORMATION; }
}
