package com.optimalquestguide.progress;

import com.google.gson.*;
import com.optimalquestguide.integration.BoundedHttp;
import java.io.*;
import java.time.*;
import java.util.*;
import javax.inject.*;
import okhttp3.HttpUrl;

@Singleton
public class WikiSyncProgressProvider implements PlayerProgressProvider
{
    private final BoundedHttp http;
    @Inject public WikiSyncProgressProvider(BoundedHttp http) { this.http = http; }
    public static String normalizeName(String name)
    {
        String normalized = name == null ? "" : name.trim().replace(' ', '_');
        if (!normalized.matches("[A-Za-z0-9_-]{1,12}") || normalized.replace("_", "").replace("-", "").isEmpty())
            throw new IllegalArgumentException("Enter a RuneScape name (1–12 letters, numbers, spaces, underscores or hyphens).");
        return normalized;
    }
    public static String identity(String name) { return normalizeName(name).toLowerCase(Locale.ROOT); }
    public static String scope(String name) { return "lookup:" + identity(name) + ":STANDARD"; }
    public static String url(String name)
    {
        return Objects.requireNonNull(HttpUrl.parse("https://sync.runescape.wiki/runelite/player/"))
            .newBuilder().addPathSegment(normalizeName(name)).addPathSegment("STANDARD").build().toString();
    }
    @Override public AccountProgress lookup(String name) throws IOException
    {
        BoundedHttp.Result result = http.get(url(name), Map.of("Cache-Control", "no-cache"), 1_000_000);
        if ((result.getCode() == 400 && result.getBody().contains("NO_USER_DATA")) || result.getCode() == 404)
            throw new IOException("No WikiSync data for this account. The player needs to synchronize with WikiSync.");
        if (result.getCode() == 429) throw new IOException("WikiSync is throttling requests. Retry later" + (result.getRetryAfter() == null ? "." : " (Retry-After: " + result.getRetryAfter() + ")."));
        if (result.getCode() == 401 || result.getCode() == 403) throw new IOException("WikiSync access was refused.");
        if (result.getCode() != 200) throw new IOException("WikiSync returned HTTP " + result.getCode() + ". Cached observations retain their original age.");
        return parse(name, result.getBody(), System.currentTimeMillis());
    }
    public AccountProgress parse(String requestedName, String json, long retrievedAt) throws IOException
    {
        try
        {
            JsonObject data = new JsonParser().parse(json).getAsJsonObject();
            if (!data.has("username") || !identity(requestedName).equals(identity(data.get("username").getAsString())))
                throw new IOException("WikiSync returned a different account; response discarded.");
            Map<String, AccountProgress.QuestStatus> quests = new HashMap<>();
            Map<String, Integer> levels = new HashMap<>();
            if (data.has("quests") && data.get("quests").isJsonObject())
                for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject("quests").entrySet())
                {
                    String id = Identities.quest(entry.getKey());
                    Integer status = integer(entry.getValue());
                    if (id != null && status != null && status >= 0 && status <= 2)
                        quests.put(id, AccountProgress.QuestStatus.values()[status]);
                }
            if (data.has("levels") && data.get("levels").isJsonObject())
                for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject("levels").entrySet())
                {
                    String id = Identities.skill(entry.getKey());
                    Integer level = integer(entry.getValue());
                    if (id != null && level != null && level >= 1 && level <= 126) levels.put(id, level);
                }
            Long observedAt = null;
            if (data.has("timestamp") && !data.get("timestamp").isJsonNull())
            {
                try { observedAt = Instant.parse(data.get("timestamp").getAsString()).toEpochMilli(); }
                catch (DateTimeException ignored) { /* Unknown freshness is honest when timestamp syntax changes. */ }
            }
            return new AccountProgress(scope(requestedName), normalizeName(requestedName), "STANDARD", "WikiSync",
                retrievedAt, observedAt, quests, levels, Collections.emptyMap(), Collections.emptySet());
        }
        catch (JsonParseException | IllegalStateException | IllegalArgumentException e) { throw new IOException("Invalid WikiSync profile; missing values remain unknown.", e); }
    }
    private static Integer integer(JsonElement value)
    {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) return null;
        try { return value.getAsBigDecimal().intValueExact(); } catch (ArithmeticException | NumberFormatException e) { return null; }
    }
}
