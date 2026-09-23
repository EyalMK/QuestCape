package com.questcape.integration;

import com.questcape.progress.Identities;
import java.io.*;
import java.net.URI;
import java.util.*;
import javax.inject.*;

@Singleton
public class TrainingGuideResolver
{
	public static final String FALLBACK = "https://www.theoatrix.net/all-guides";
	private final Properties urls = new Properties();
	private final BoundedHttp http;

	@Inject
	public TrainingGuideResolver(BoundedHttp http)
	{
		this.http = http;
		try (InputStream in = TrainingGuideResolver.class.getResourceAsStream("/theoatrix.properties"))
		{
			if (in == null)
			{
				throw new IllegalStateException("Missing packaged training links");
			}
			urls.load(in);
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Cannot read training links", e);
		}
	}

	public String resolve(String skill)
	{
		String id = Identities.skill(skill);
		String candidate = urls.getProperty(id == null ? "" : id, FALLBACK);
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
			return "https".equalsIgnoreCase(uri.getScheme()) && ("theoatrix.net".equalsIgnoreCase(uri.getHost())
				|| "www.theoatrix.net".equalsIgnoreCase(uri.getHost())) && uri.getUserInfo() == null
				&& (uri.getPort() == -1 || uri.getPort() == 443);
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
	}

	/** Validate every redirect before browser navigation. Runs only after user activation, off the EDT. */
	public String verifyDestination(String url) throws IOException
	{
		for (int redirects = 0; redirects < 5; redirects++)
		{
			if (!safe(url))
			{
				throw new IOException("Unsafe training destination. Use Browse Theoatrix guides.");
			}
			BoundedHttp.Result response = http.get(url, Collections.emptyMap(), 4_000_000);
			if (response.getCode() == 200)
			{
				return url;
			}
			if (response.getCode() < 300 || response.getCode() > 399 || response.getLocation() == null)
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
