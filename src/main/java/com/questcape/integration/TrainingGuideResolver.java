package com.questcape.integration;

import com.questcape.progress.Identities;
import java.io.*;
import java.net.URI;
import java.util.*;
import javax.inject.*;

@Singleton
public class TrainingGuideResolver
{
	private static final String RESOURCE = "/theoatrix.properties", HOST = "theoatrix.net",
		WWW_HOST = "www.theoatrix.net";
	public static final String FALLBACK = "https://www.theoatrix.net/all-guides";
	private volatile Map<String, String> urls = Collections.emptyMap();
	private boolean prepared;
	private final BoundedHttp http;

	@Inject
	public TrainingGuideResolver(BoundedHttp http)
	{
		this.http = http;
	}

	/** Worker-only local resource preparation, before sidebar publication; resolution itself performs no IO. */
	public synchronized void prepare() throws IOException
	{
		if (prepared)
		{
			return;
		}
		Properties definitions = new Properties();
		try (InputStream in = TrainingGuideResolver.class.getResourceAsStream(RESOURCE))
		{
			if (in == null)
			{
				throw new IOException("Missing packaged training links");
			}
			definitions.load(in);
		}
		Map<String, String> values = new HashMap<>();
		definitions.stringPropertyNames().forEach(name -> values.put(name, definitions.getProperty(name)));
		urls = Collections.unmodifiableMap(values);
		prepared = true;
	}

	public String resolve(String skill)
	{
		String id = Identities.skill(skill);
		String candidate = urls.getOrDefault(id == null ? "" : id, FALLBACK);
		return safe(candidate) ? candidate : FALLBACK;
	}

	public String label(String skill)
	{
		return resolve(skill).equals(FALLBACK) ? "Browse Theoatrix guides"
			: skill.replace('_', ' ') + " · Theoatrix guide";
	}

	public static boolean safe(String value)
	{
		try
		{
			URI uri = URI.create(value);
			return "https".equalsIgnoreCase(uri.getScheme()) && (HOST.equalsIgnoreCase(uri.getHost())
				|| WWW_HOST.equalsIgnoreCase(uri.getHost())) && uri.getUserInfo() == null
				&& (uri.getPort() == -1 || uri.getPort() == com.questcape.guide.GuideSourcePolicy.SECURE_PORT);
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
	}

	/** Validate every redirect before browser navigation. Runs only after user activation, off the EDT. */
	public String verifyDestination(String url) throws IOException
	{
		for (int redirects = 0; redirects < HttpPolicy.MAX_REDIRECTS; redirects++)
		{
			if (!safe(url))
			{
				throw new IOException("Unsafe training destination. Use Browse Theoatrix guides.");
			}
			BoundedHttp.Result response = http.get(url, Collections.emptyMap(),
				com.questcape.guide.GuideSourcePolicy.MAX_CONTENT);
			if (response.getCode() == HttpPolicy.OK)
			{
				return url;
			}
			if (response.getCode() < HttpPolicy.REDIRECT_MIN || response.getCode() > HttpPolicy.REDIRECT_MAX
				|| response.getLocation() == null)
			{
				throw new IOException(
					"Theoatrix returned HTTP " + response.getCode() + ". Retry or copy the guide link.");
			}
			try
			{
				url = URI.create(url).resolve(response.getLocation()).toString();
			}
			catch (IllegalArgumentException e)
			{
				throw new IOException("Invalid training redirect", e);
			}
		}
		throw new IOException("Too many training-guide redirects.");
	}
}
