package com.optimalquestguide.guide;

import com.google.gson.*;
import com.optimalquestguide.integration.BoundedHttp;
import com.optimalquestguide.persistence.JsonStore;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.*;

@Singleton
public class GuideRepository
{
    public static final String ENDPOINT = "https://oldschool.runescape.wiki/api.php?action=parse&page=Optimal_quest_guide&prop=text%7Crevid&format=json";
    public static final long MAX_AGE = TimeUnit.HOURS.toMillis(24);
    private final GuideParser parser;
    private final BoundedHttp http;
    private final JsonStore store;
    private volatile GuideSnapshot snapshot;
    private long nextRefresh;
    private CompletableFuture<GuideSnapshot> pending;
    private long generation;
    @Inject public GuideRepository(GuideParser parser, BoundedHttp http, JsonStore store)
    { this.parser = parser; this.http = http; this.store = store; }

    public synchronized GuideSnapshot load() throws IOException
    {
        GuideSnapshot saved = store.read("guide", "standard", GuideSnapshot.class);
        if (saved != null) { validate(saved, null); snapshot = saved; }
        return snapshot;
    }
    public GuideSnapshot current() { return snapshot; }
    public boolean isStale(long now) { return snapshot == null || now - snapshot.getValidatedAt() >= MAX_AGE; }
    public synchronized CompletableFuture<GuideSnapshot> refresh(Executor executor, long now)
    {
        if (pending != null && !pending.isDone()) return pending;
        if (now < nextRefresh) return CompletableFuture.failedFuture(new IOException("Guide refresh cooldown; retry shortly"));
        nextRefresh = now + 5000;
        long token = generation;
        pending = CompletableFuture.supplyAsync(() ->
        {
            try
            {
                GuideSnapshot previous = snapshot;
                Map<String, String> headers = new HashMap<>();
                if (previous != null)
                {
                    if (previous.getEtag() != null) headers.put("If-None-Match", previous.getEtag());
                    if (previous.getLastModified() != null) headers.put("If-Modified-Since", previous.getLastModified());
                }
                BoundedHttp.Result response = http.get(ENDPOINT, headers, 4_000_000);
                GuideSnapshot candidate;
                long received = System.currentTimeMillis();
                if (response.getCode() == 304 && previous != null) candidate = previous.revalidated(received);
                else
                {
                    if (response.getCode() != 200) throw new IOException("Wiki returned HTTP " + response.getCode());
                    JsonObject parse = new JsonParser().parse(response.getBody()).getAsJsonObject().getAsJsonObject("parse");
                    if (parse == null || !parse.has("text")) throw new IOException("Wiki parse response is missing content");
                    String html = parse.getAsJsonObject("text").get("*").getAsString();
                    String revision = parse.has("revid") ? parse.get("revid").getAsString() : null;
                    // The JDK parser repairs HTML, so independently require complete source table termination.
                    if (!html.toLowerCase(Locale.ROOT).contains("</table>")) throw new IOException("Unterminated guide table");
                    candidate = new GuideSnapshot(revision, received, received, response.getEtag(), response.getLastModified(), parser.parse(html));
                    validate(candidate, previous);
                }
                synchronized (this)
                {
                    if (token != generation || Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Refresh canceled");
                    store.write("guide", "standard", candidate);
                    snapshot = candidate;
                }
                return candidate;
            }
            catch (IOException | RuntimeException e) { throw new CompletionException(e); }
        }, executor);
        return pending;
    }
    public synchronized void cancel() { generation++; if (pending != null) pending.cancel(true); }
    public static void validate(GuideSnapshot candidate, GuideSnapshot previous) throws IOException
    {
        if (candidate.getSchema() != GuideSnapshot.SCHEMA || !GuideSnapshot.SOURCE.equals(candidate.getSource())
            || candidate.getRows() == null || candidate.getRows().isEmpty() || candidate.getRows().size() > 2000
            || candidate.getRetrievedAt() <= 0 || candidate.getValidatedAt() <= 0) throw new IOException("Invalid guide cache");
        for (GuideRow row : candidate.getRows())
            if (row == null || row.getKey() == null || row.getTitle() == null || row.getTitle().isBlank()
                || row.getKind() == null || row.getFields() == null || row.getFields().size() != 7 || row.getTargets() == null
                || row.getLinks() == null) throw new IOException("Invalid guide row");
        if (previous != null && candidate.getRows().size() < previous.getRows().size() * 0.8)
            throw new IOException("Suspicious guide truncation; keeping the last valid revision");
    }
}
