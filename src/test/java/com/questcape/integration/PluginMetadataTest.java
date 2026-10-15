package com.questcape.integration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.OkHttpClient;
import org.junit.Test;
import static org.junit.Assert.*;

public class PluginMetadataTest
{
	private InputStream resource(String value)
	{
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.ISO_8859_1));
	}

	@Test
	public void headerUsesOneValidatedLoadAndChangedPropertiesWithoutARequest() throws Exception
	{
		AtomicInteger loads = new AtomicInteger();
		PluginMetadata.Loader metadata = new PluginMetadata.Loader(() ->
		{
			loads.incrementAndGet();
			return resource("displayName=Quest Cape\nversion=7.8.9\n");
		});
		BoundedHttp transport = new BoundedHttp(new OkHttpClient(), metadata);
		assertEquals("Quest-Cape/7.8.9 (RuneLite companion)",
			transport.request("https://example.invalid", Map.of()).header("User-Agent"));
		assertSame(metadata.get(), metadata.get());
		assertEquals(1, loads.get());
		for (String invalid : List.of("version=1.2.3", "displayName= \nversion=1.2.3",
			"displayName=QuestCape\nversion=broken",
			"displayName=QuestCape\nversion=1.2.3\\r\\nInjected",
			"displayName=QuestCape\\nInjected\nversion=1.2.3"))
		{
			assertThrows(IOException.class, () -> PluginMetadata.read(resource(invalid)));
		}
		assertThrows(IOException.class, () -> PluginMetadata.read(null));
	}

	@Test
	public void developmentAndPackagedMetadataAreIdenticalAndUnique() throws Exception
	{
		PluginMetadata development = PluginMetadata
			.read(PluginMetadata.class.getResourceAsStream(PluginMetadata.RESOURCE));
		assertEquals(System.getProperty("questcape.version"), development.getVersion());
		try (URLClassLoader jar = new URLClassLoader(new URL[]
		{ Paths.get(System.getProperty("questcape.distributionJar")).toUri().toURL() },
			ClassLoader.getPlatformClassLoader()))
		{
			assertEquals(1, Collections.list(jar.getResources("runelite-plugin.properties")).size());
			assertEquals(1, Collections
				.list(PluginMetadata.class.getClassLoader().getResources("runelite-plugin.properties")).size());
			PluginMetadata packaged = PluginMetadata.read(jar.getResourceAsStream("runelite-plugin.properties"));
			assertEquals(development.getUserAgent(), packaged.getUserAgent());
		}
	}
}
