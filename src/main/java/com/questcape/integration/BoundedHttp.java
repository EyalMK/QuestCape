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
	private final PluginMetadata.Loader metadata;
	private final Set<Call> calls = ConcurrentHashMap.newKeySet();
	private final java.util.concurrent.atomic.AtomicLong generation = new java.util.concurrent.atomic.AtomicLong();

	public BoundedHttp(OkHttpClient client)
	{
		this(client, new PluginMetadata.Loader());
	}

	@Inject
	public BoundedHttp(OkHttpClient client, PluginMetadata.Loader metadata)
	{
		this.metadata = metadata;
		this.client = client.newBuilder().connectTimeout(HttpPolicy.CONNECT_SECONDS, TimeUnit.SECONDS)
			.readTimeout(HttpPolicy.READ_SECONDS, TimeUnit.SECONDS)
			.callTimeout(HttpPolicy.CALL_SECONDS, TimeUnit.SECONDS)
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
		Call call = client.newCall(request(url, headers));
		calls.add(call);
		if (generation.get() != token || Thread.currentThread().isInterrupted())
		{
			call.cancel();
		}
		try (Response response = call.execute())
		{
			ResponseBody body = response.body();
			String content = "";
			if (response.code() != HttpPolicy.NOT_MODIFIED)
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
			return new Result(response.code(), content, response.header(HttpPolicy.ETAG),
				response.header(HttpPolicy.LAST_MODIFIED),
				response.header(HttpPolicy.LOCATION), response.header(HttpPolicy.RETRY_AFTER));
		}
		finally
		{
			calls.remove(call);
		}
	}

	public Request request(String url, Map<String, String> headers) throws IOException
	{
		Request.Builder request = new Request.Builder().url(url);
		headers.forEach(request::header);
		return request.header(HttpPolicy.USER_AGENT, metadata.get().getUserAgent()).build();
	}

	public void cancel()
	{
		generation.incrementAndGet();
		calls.forEach(Call::cancel);
	}
}
