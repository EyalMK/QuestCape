package com.optimalquestguide.guide;

import com.google.gson.*;
import com.optimalquestguide.integration.BoundedHttp;
import com.optimalquestguide.persistence.JsonStore;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GuideRepositoryTest
{
    private JsonStore store;
    private BoundedHttp http;
    private GuideRepository repo;
    @Before public void setup() throws Exception
    {
        store = new JsonStore(new Gson(), Files.createTempDirectory("oqg-guide-test")); http = mock(BoundedHttp.class);
        repo = new GuideRepository(new GuideParser(), http, store);
    }
    private String payload(String html)
    {
        JsonObject parse = new JsonObject(), body = new JsonObject(), text = new JsonObject(); text.addProperty("*", html);
        parse.add("text", text); parse.addProperty("revid", 123); body.add("parse", parse); return body.toString();
    }
    private BoundedHttp.Result result(int code, String body) { return new BoundedHttp.Result(code, body, "tag", "modified", null, null); }
    @Test public void offlineStartupAndAtomicLastGoodCache() throws Exception
    {
        when(http.get(anyString(), anyMap(), anyInt())).thenThrow(new IOException("Offline"));
        assertNull(repo.load()); assertTrue(repo.isStale(System.currentTimeMillis()));
        try { repo.refresh(Runnable::run, 10000).join(); fail(); } catch (CompletionException expected) { }
        assertNull(repo.current());
        String original = GuideParserTest.fixture();
        doReturn(result(200, payload(original))).when(http).get(anyString(), anyMap(), anyInt());
        GuideSnapshot good = repo.refresh(Runnable::run, 20000).join();
        assertEquals(351, good.getRows().size());
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(result(200, payload(GuideParserTest.table(GuideParserTest.activity("short")))));
        try { repo.refresh(Runnable::run, 30000).join(); fail(); } catch (CompletionException expected) { }
        assertSame(good, repo.current());
        GuideRepository restart = new GuideRepository(new GuideParser(), http, store); assertEquals(351, restart.load().getRows().size());
        assertTrue(restart.isStale(good.getValidatedAt() + GuideRepository.MAX_AGE));
    }
    @Test public void requestsCoalesceAndRevalidateConditionally() throws Exception
    {
        List<Runnable> queue = new ArrayList<>();
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(result(200, payload(GuideParserTest.fixture())));
        CompletableFuture<GuideSnapshot> first = repo.refresh(queue::add, 10000);
        assertSame(first, repo.refresh(queue::add, 11000)); assertEquals(1, queue.size()); queue.remove(0).run(); first.join();
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(result(304, ""));
        repo.refresh(Runnable::run, 20000).join(); verify(http).get(eq(GuideRepository.ENDPOINT), eq(Map.of("If-None-Match", "tag", "If-Modified-Since", "modified")), eq(4_000_000));
        assertTrue(repo.refresh(Runnable::run, 20001).isCompletedExceptionally());
    }
    @Test public void revisedContentAndCanceledWork() throws Exception
    {
        String original = GuideParserTest.fixture();
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(result(200, payload(original)));
        GuideSnapshot old = repo.refresh(Runnable::run, 10000).join();
        String added = original.replace("</table>", GuideParserTest.activity("New route activity") + "</table>");
        when(http.get(anyString(), anyMap(), anyInt())).thenReturn(result(200, payload(added)));
        assertEquals(old.getRows().size() + 1, repo.refresh(Runnable::run, 20000).join().getRows().size());
        List<Runnable> queue = new ArrayList<>(); repo.refresh(queue::add, 30000); repo.cancel(); queue.get(0).run();
        assertEquals(352, repo.current().getRows().size());
    }
}
