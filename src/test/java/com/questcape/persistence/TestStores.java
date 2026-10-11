package com.questcape.persistence;

import com.google.gson.Gson;
import java.nio.file.Path;
import net.runelite.client.util.Filepath;

/** Arbitrary temporary roots are limited to tests; runtime uses Plugin.getPluginDirectory(). */
public final class TestStores
{
	private TestStores()
	{
	}

	public static JsonStore store(Gson gson, Path root)
	{
		return new JsonStore(gson, () -> Filepath.Unchecked.getRooted(root));
	}
}
