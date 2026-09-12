package com.questcape.persistence;

import com.google.gson.Gson;
import com.questcape.guide.GuideParser;
import com.questcape.progress.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonStoreTest
{
    @Test public void unknownSchemaAndInvalidJsonAreRetained() throws Exception
    {
        Path root = Files.createTempDirectory("oqg-store"); JsonStore store = new JsonStore(new Gson(), root);
        Path file = root.resolve("progress").resolve(GuideParser.digest("live:A") + ".json"); Files.createDirectories(file.getParent());
        for (String content : List.of("{\"schema\":987}", "{not json"))
        {
            Files.writeString(file, content);
            try { store.read("progress", "live:A", AccountProgress.class); fail(); } catch (IOException expected) { }
            assertEquals(content, Files.readString(file));
        }
    }
    @Test public void atomicRoundtripAndCategorySeparation() throws Exception
    {
        Path root = Files.createTempDirectory("oqg-store"); JsonStore store = new JsonStore(new Gson(), root);
        AccountProgress account = ProgressTest.observation("live:A", "Alice", "STANDARD", Map.of(), Map.of("ATTACK", 20));
        store.write("progress", "live:A", account); assertEquals(account, store.read("progress", "live:A", AccountProgress.class));
        assertNull(store.read("guide", "live:A", AccountProgress.class));
        try (java.util.stream.Stream<Path> paths = Files.walk(root)) { assertFalse(paths.anyMatch(p -> p.toString().endsWith(".tmp"))); }
    }
}
