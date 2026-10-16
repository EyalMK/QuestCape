package com.questcape.integration.http;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import javax.inject.*;

/** Authoritative immutable product identity, also usable from an isolated distribution JAR. */
public final class PluginMetadata
{
	public static final String RESOURCE = "/runelite-plugin.properties";
	private static final String DISPLAY_NAME = "displayName", VERSION = "version", COMPANION = " (RuneLite companion)";
	private final String displayName, version, userAgent;

	private PluginMetadata(Properties values) throws IOException
	{
		displayName = required(values, DISPLAY_NAME);
		version = required(values, VERSION);
		if (!version.matches("[0-9]+\\.[0-9]+\\.[0-9]+(?:[-+][A-Za-z0-9.-]+)?"))
		{
			throw new MetadataException("Invalid plugin version metadata");
		}
		String token = displayName.replaceAll("[^A-Za-z0-9!#$%&'*+.^_`|~-]+", "-");
		if (token.isBlank() || token.chars().allMatch(value -> value == '-'))
		{
			throw new MetadataException("Invalid plugin product metadata");
		}
		userAgent = token + "/" + version + COMPANION;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getVersion()
	{
		return version;
	}

	public String getUserAgent()
	{
		return userAgent;
	}

	public static PluginMetadata read(InputStream input) throws IOException
	{
		if (input == null)
		{
			throw new MetadataException("Plugin metadata resource is unavailable");
		}
		try (InputStream owned = input)
		{
			Properties values = new Properties();
			values.load(owned);
			return new PluginMetadata(values);
		}
	}

	private static String required(Properties values, String name) throws IOException
	{
		String value = values.getProperty(name);
		if (value == null || value.isBlank() || value.chars().anyMatch(c -> c < 32 || c > 126))
		{
			throw new MetadataException("Missing or unsafe plugin metadata: " + name);
		}
		return value.trim();
	}

	public static final class MetadataException extends IOException
	{
		MetadataException(String message)
		{
			super(message);
		}
	}

	/** First use is on the HTTP worker, never during injection or a lifecycle/EDT callback. */
	@Singleton
	public static class Loader
	{
		private final Supplier<InputStream> resource;
		private PluginMetadata loaded;
		private IOException failure;
		private boolean attempted;

		@Inject
		public Loader()
		{
			this(() -> PluginMetadata.class.getResourceAsStream(RESOURCE));
		}

		public Loader(Supplier<InputStream> resource)
		{
			this.resource = Objects.requireNonNull(resource);
		}

		public synchronized PluginMetadata get() throws IOException
		{
			if (!attempted)
			{
				attempted = true;
				try
				{
					loaded = read(resource.get());
				}
				catch (IOException e)
				{
					failure = e;
				}
				catch (RuntimeException e)
				{
					failure = new IOException("Plugin metadata could not be read", e);
				}
			}
			if (failure != null)
			{
				throw failure;
			}
			return loaded;
		}
	}
}
