package com.questcape.persistence;

import com.google.gson.*;
import com.questcape.guide.parsing.GuideParser;
import java.io.*;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.Filepath;

@Slf4j
public class JsonStore
{
	public interface Directory
	{
		Filepath get() throws IOException;
	}

	private final Gson gson;
	private final Directory directory;
	private Filepath root;

	public JsonStore(Gson gson, Directory directory)
	{
		this.gson = gson;
		this.directory = directory;
	}

	private Filepath path(String category, String key) throws IOException
	{
		if (!CachePolicy.GUIDE.equals(category) && !CachePolicy.PROGRESS.equals(category))
		{
			throw new IOException("Unsupported cache category");
		}
		if (root == null)
		{
			root = directory.get();
		}
		return root.joinSegment(category).joinSegment(GuideParser.digest(key) + CachePolicy.EXTENSION);
	}

	public synchronized <T> T read(String category, String key, Class<T> type) throws IOException
	{
		try
		{
			Filepath file = path(category, key);
			if (!file.exists())
			{
				return null;
			}
			if (file.size() > CachePolicy.MAX_BYTES)
			{
				throw new IOException("Saved data exceeds size limit");
			}
			try (Reader reader = file.openBufferedReader())
			{
				StringBuilder content = new StringBuilder();
				char[] buffer = new char[CachePolicy.READ_BUFFER];
				int read;
				while ((read = reader.read(buffer)) != -1)
				{
					if (content.length() + read > CachePolicy.MAX_BYTES)
					{
						throw new IOException("Saved data exceeds size limit");
					}
					content.append(buffer, 0, read);
				}
				JsonObject object = new JsonParser().parse(content.toString()).getAsJsonObject();
				if (!object.has(CachePolicy.SCHEMA_FIELD)
					|| object.get(CachePolicy.SCHEMA_FIELD).getAsInt() != CachePolicy.SCHEMA)
				{
					throw new IOException("Unknown saved-data schema; original file retained");
				}
				return gson.fromJson(object, type);
			}
		}
		catch (RuntimeException e)
		{
			throw new IOException("Saved data could not be read; original file retained", e);
		}
	}

	public synchronized void write(String category, String key, Object value) throws IOException
	{
		Filepath temp = null;
		try
		{
			Filepath destination = path(category, key);
			destination.getParent().createDirectories();
			temp = destination.getParent().createTempFile(CachePolicy.TEMP_PREFIX, CachePolicy.TEMP_SUFFIX);
			temp.write(gson.toJson(value));
			// Refuse an unsafe/non-atomic replacement; the previous good file stays intact.
			temp.moveTo(destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (RuntimeException e)
		{
			throw new IOException("Saved data could not be written; original file retained", e);
		}
		finally
		{
			if (temp != null)
			{
				try
				{
					temp.deleteIfExists();
				}
				catch (IOException | RuntimeException e)
				{
					log.debug("QuestCape temporary cache cleanup failed ({})", e.getClass().getSimpleName());
				}
			}
		}
	}
}
