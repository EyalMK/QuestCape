package com.questcape.integration.http;

import com.questcape.integration.links.TrainingGuideResolver;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.*;
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
	public void standardSourceResourcesSupportTrainingVerificationWithoutRootDescriptor() throws Exception
	{
		try (URLClassLoader resources = new URLClassLoader(new URL[]
		{ Paths.get("src/main/resources").toUri().toURL() }, ClassLoader.getPlatformClassLoader()))
		{
			assertNull(resources.getResource("runelite-plugin.properties"));
			PluginMetadata.Loader metadata = new PluginMetadata.Loader(
				() -> resources.getResourceAsStream(PluginMetadata.RESOURCE.substring(1)));
			AtomicInteger requests = new AtomicInteger();
			String guide = "https://www.theoatrix.net/post/theoatrix-s-1-99-magic-guide-osrs";
			OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain ->
			{
				requests.incrementAndGet();
				assertEquals(guide, chain.request().url().toString());
				assertEquals("QuestCape/" + System.getProperty("questcape.version") + " (RuneLite companion)",
					chain.request().header(HttpPolicy.USER_AGENT));
				return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
					.code(200).message("OK").body(ResponseBody.create(null, "guide")).build();
			}).build();
			TrainingGuideResolver resolver = new TrainingGuideResolver(new BoundedHttp(client, metadata));
			resolver.prepare();
			assertEquals(guide, resolver.verifyDestination(resolver.resolve("MAGIC")));
			assertEquals(1, requests.get());
		}
	}

	@Test
	public void developmentAndPackagedMetadataAreIdenticalAndUnique() throws Exception
	{
		PluginMetadata development = new PluginMetadata.Loader().get();
		assertEquals(System.getProperty("questcape.version"), development.getVersion());
		String resourceName = PluginMetadata.RESOURCE.substring(1);
		try (InputStream descriptor = Files.newInputStream(Paths.get(System.getProperty("questcape.descriptor"))))
		{
			assertEquals(PluginMetadata.read(descriptor).getUserAgent(), development.getUserAgent());
		}
		try (URLClassLoader jar = new URLClassLoader(new URL[]
		{ Paths.get(System.getProperty("questcape.distributionJar")).toUri().toURL() },
			ClassLoader.getPlatformClassLoader()))
		{
			assertNull(jar.getResource("runelite-plugin.properties"));
			assertEquals(1, Collections.list(jar.getResources(resourceName)).size());
			assertEquals(1, Collections
				.list(PluginMetadata.class.getClassLoader().getResources(resourceName)).size());
			PluginMetadata packaged = PluginMetadata.read(jar.getResourceAsStream(resourceName));
			assertEquals(development.getDisplayName(), packaged.getDisplayName());
			assertEquals(development.getUserAgent(), packaged.getUserAgent());
		}
	}
}
