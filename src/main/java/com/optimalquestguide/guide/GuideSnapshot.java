package com.optimalquestguide.guide;

import java.util.*;
import lombok.Value;

@Value
public class GuideSnapshot
{
    public static final int SCHEMA = 1;
    public static final String SOURCE = "https://oldschool.runescape.wiki/w/Optimal_quest_guide";
    int schema;
    String source;
    String revision;
    long retrievedAt;
    long validatedAt;
    String etag;
    String lastModified;
    List<GuideRow> rows;

    public GuideSnapshot(String revision, long retrievedAt, long validatedAt, String etag, String lastModified, List<GuideRow> rows)
    {
        this.schema = SCHEMA;
        this.source = SOURCE;
        this.revision = revision;
        this.retrievedAt = retrievedAt;
        this.validatedAt = validatedAt;
        this.etag = etag;
        this.lastModified = lastModified;
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public GuideSnapshot revalidated(long at)
    {
        return new GuideSnapshot(revision, retrievedAt, at, etag, lastModified, rows);
    }
}
