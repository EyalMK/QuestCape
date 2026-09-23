package com.questcape.integration;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.*;
import lombok.Value;
import okhttp3.*;

@Singleton
public class BoundedHttp
{
	private final OkHttpClient client;
	private final Set<Call> calls = ConcurrentHashMap.newKeySet();
	private final java.util.concurrent.atomic.AtomicLong generation = new java.util.concurrent.atomic.AtomicLong();

	@Inject
	public BoundedHttp(OkHttpClient client)
	{
		this.client = client.newBuilder().connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(20, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS)
			.followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false).build();
	}

	@Value
	public static class Result
	{
		int code;
		String body;
		String etag;
		String lastModified;
		String location;
		String retryAfter;
	}

	public Result get(String url, Map<String, String> headers, int maxBytes) throws IOException
	{
		long token = generation.get();
		if (Thread.currentThread().isInterrupted())
		{
			throw new InterruptedIOException("Request canceled");
		}
		Request.Builder request = new Request.Builder().url(url).header("User-Agent",
			"QuestCape/0.1 (RuneLite companion)");
		headers.forEach(request::header);
		Call call = client.newCall(request.build());
		calls.add(call);
		if (generation.get() != token || Thread.currentThread().isInterrupted())
		{
			call.cancel();
		}
		try (Response response = call.execute())
		{
			ResponseBody body = response.body();
			String content = "";
			if (body != null && response.code() != 304)
			{
				if (body.contentLength() > maxBytes)
				{
					throw new IOException("Response too large");
				}
				byte[] bytes = body.byteStream().readNBytes(maxBytes + 1);
				if (bytes.length > maxBytes)
				{
					throw new IOException("Response too large");
				}
				content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
			}
			return new Result(response.code(), content, response.header("ETag"), response.header("Last-Modified"),
				response.header("Location"), response.header("Retry-After"));
		}
		finally
		{
			calls.remove(call);
		}
	}

	public void cancel()
	{
		generation.incrementAndGet();
		calls.forEach(Call::cancel);
	}
}
