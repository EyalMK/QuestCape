package com.questcape.persistence;

import com.google.gson.*;
import com.questcape.guide.GuideParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.inject.*;
import net.runelite.client.RuneLite;

@Singleton
public class JsonStore
{
    private final Gson gson;
    private final Path root;
    @Inject public JsonStore(Gson gson) { this(gson, RuneLite.RUNELITE_DIR.toPath().resolve("questcape")); }
    public JsonStore(Gson gson, Path root) { this.gson = gson; this.root = root; }
    private Path path(String category, String key) { return root.resolve(category).resolve(GuideParser.digest(key) + ".json"); }
    public synchronized <T> T read(String category, String key, Class<T> type) throws IOException
    {
        Path file = path(category, key);
        if (!Files.exists(file)) return null;
        if (Files.size(file) > 4_000_000) throw new IOException("Saved data exceeds size limit");
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
            if (!object.has("schema") || object.get("schema").getAsInt() != 1) throw new IOException("Unknown saved-data schema; original file retained");
            return gson.fromJson(object, type);
        }
        catch (JsonParseException | IllegalStateException | NumberFormatException e) { throw new IOException("Saved data is invalid; original file retained", e); }
    }
    public synchronized void write(String category, String key, Object value) throws IOException
    {
        Path destination = path(category, key);
        Files.createDirectories(destination.getParent());
        Path temp = Files.createTempFile(destination.getParent(), "candidate-", ".tmp");
        try
        {
            Files.writeString(temp, gson.toJson(value), StandardCharsets.UTF_8);
            // Refuse an unsafe/non-atomic replacement; the previous good file stays intact.
            Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        finally { Files.deleteIfExists(temp); }
    }
}
