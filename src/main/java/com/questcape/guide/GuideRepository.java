package com.questcape.guide;

import com.google.gson.*;
import com.questcape.integration.BoundedHttp;
import com.questcape.persistence.JsonStore;
import com.questcape.persistence.CachePolicy;
import com.questcape.integration.HttpPolicy;
import com.questcape.persistence.StoredView;
import com.questcape.persistence.StoredView.SaveState;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.*;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class GuideRepository
{
	public static final String ENDPOINT = "https://oldschool.runescape.wiki/api.php?action=parse&page=Optimal_quest_guide&prop=text%7Crevid&format=json";
	public static final long MAX_AGE = TimeUnit.HOURS.toMillis(24);
	private final GuideParser parser;
	private final BoundedHttp http;
	private final JsonStore store;
	private volatile GuideSnapshot snapshot;
	private volatile StoredView<GuideSnapshot> published = StoredView.empty();
	private long nextRefresh;
	private CompletableFuture<GuideSnapshot> pending;
	private long generation;
	private final Object persistenceLock = new Object();
	private volatile String persistenceStatus = "";
	private boolean unreadable;

	@Inject
	public GuideRepository(GuideParser parser, BoundedHttp http, JsonStore store)
	{
		this.parser = parser;
		this.http = http;
		this.store = store;
	}

	public synchronized long token()
	{
		return generation;
	}

	public GuideSnapshot load() throws IOException
	{
		return load(token());
	}

	public GuideSnapshot load(long token) throws IOException
	{
		synchronized (this)
		{
			checkActive(token);
		}
		synchronized (persistenceLock)
		{
			GuideSnapshot saved;
			try
			{
				saved = prepare(store.read(CachePolicy.GUIDE, CachePolicy.STANDARD_ROUTE, GuideSnapshot.class));
				unreadable = false;
			}
			catch (IOException e)
			{
				unreadable = true;
				throw e;
			}
			if (saved != null)
			{
				synchronized (this)
				{
					if (token == generation)
					{
						snapshot = saved;
						published = new StoredView<>(saved, SaveState.SAVED);
					}
				}
			}
		}
		return snapshot;
	}

	public StoredView<GuideSnapshot> view()
	{
		return published;
	}

	public GuideSnapshot current()
	{
		return snapshot;
	}

	public String persistenceStatus()
	{
		return persistenceStatus;
	}

	public boolean isStale(long now)
	{
		return snapshot == null || now - snapshot.getValidatedAt() >= MAX_AGE;
	}

	public CompletableFuture<GuideSnapshot> refresh(Executor executor, long now)
	{
		return refresh(executor, now, token());
	}

	public synchronized CompletableFuture<GuideSnapshot> refresh(Executor executor, long now, long expected)
	{
		if (expected != generation)
		{
			return CompletableFuture
				.failedFuture(new java.util.concurrent.CancellationException("Invalidated guide authority"));
		}
		if (pending != null && !pending.isDone())
		{
			return pending;
		}
		if (now < nextRefresh)
		{
			return CompletableFuture.failedFuture(new IOException("Guide refresh cooldown; retry shortly"));
		}
		nextRefresh = now + CachePolicy.GUIDE_COOLDOWN_MILLIS;
		long token = generation;
		pending = CompletableFuture.supplyAsync(() ->
		{
			try
			{
				GuideSnapshot previous = snapshot;
				Map<String, String> headers = new HashMap<>();
				if (previous != null)
				{
					if (previous.getEtag() != null)
					{
						headers.put(HttpPolicy.IF_NONE_MATCH, previous.getEtag());
					}
					if (previous.getLastModified() != null)
					{
						headers.put(HttpPolicy.IF_MODIFIED_SINCE, previous.getLastModified());
					}
				}
				BoundedHttp.Result response = http.get(ENDPOINT, headers, GuideSourcePolicy.MAX_CONTENT);
				GuideSnapshot candidate;
				long received = System.currentTimeMillis();
				if (response.getCode() == HttpPolicy.NOT_MODIFIED && previous != null)
				{
					candidate = previous.revalidated(received);
				}
				else
				{
					if (response.getCode() != HttpPolicy.OK)
					{
						throw new IOException("Wiki returned HTTP " + response.getCode());
					}
					JsonObject parse = new JsonParser().parse(response.getBody()).getAsJsonObject()
						.getAsJsonObject(GuideSourcePolicy.RESPONSE_PARSE);
					if (parse == null || !parse.has(GuideSourcePolicy.RESPONSE_TEXT))
					{
						throw new IOException("Wiki parse response is missing content");
					}
					String html = parse.getAsJsonObject(GuideSourcePolicy.RESPONSE_TEXT)
						.get(GuideSourcePolicy.RESPONSE_HTML).getAsString();
					String revision = parse.has(GuideSourcePolicy.RESPONSE_REVISION)
						? parse.get(GuideSourcePolicy.RESPONSE_REVISION).getAsString()
						: null;
					// The JDK parser repairs HTML, so independently require complete source table termination.
					if (!html.toLowerCase(Locale.ROOT).contains("</table>"))
					{
						throw new IOException("Unterminated guide table");
					}
					candidate = new GuideSnapshot(revision, received, received, response.getEtag(),
						response.getLastModified(), parser.parse(html));
					validate(candidate, previous);
				}
				synchronized (persistenceLock)
				{
					synchronized (this)
					{
						checkActive(token);
					}
					SaveState saveState = SaveState.SAVED;
					try
					{
						if (unreadable)
						{
							prepare(store.read(CachePolicy.GUIDE, CachePolicy.STANDARD_ROUTE, GuideSnapshot.class));
							unreadable = false;
						}
						store.write(CachePolicy.GUIDE, CachePolicy.STANDARD_ROUTE, candidate);
					}
					catch (IOException e)
					{
						saveState = SaveState.UNSAVED;
						log.debug("QuestCape guide cache write failed ({})", e.getClass().getSimpleName());
					}
					synchronized (this)
					{
						checkActive(token);
						snapshot = candidate;
						published = new StoredView<>(candidate, saveState);
						persistenceStatus = saveState == SaveState.UNSAVED ? "Route unsaved; cache unavailable. Refresh to retry." : "";
					}
				}
				return candidate;
			}
			catch (IOException | RuntimeException e)
			{
				throw new CompletionException(e);
			}
		}, executor);
		return pending;
	}

	private GuideSnapshot prepare(GuideSnapshot saved) throws IOException
	{
		if (saved == null)
		{
			return null;
		}
		validate(saved, null);
		try
		{
			return parser.reclassify(saved);
		}
		catch (RuntimeException malformed)
		{
			throw new IOException("Invalid guide enrichment; original retained", malformed);
		}
	}

	private void checkActive(long token) throws InterruptedIOException
	{
		if (token != generation || Thread.currentThread().isInterrupted())
		{
			throw new InterruptedIOException("Refresh canceled");
		}
	}

	public synchronized void cancel()
	{
		generation++;
		if (pending != null)
		{
			pending.cancel(true);
		}
	}

	public static void validate(GuideSnapshot candidate, GuideSnapshot previous) throws IOException
	{
		if (candidate.getSchema() != GuideSnapshot.SCHEMA || !GuideSnapshot.SOURCE.equals(candidate.getSource())
			|| candidate.getRows() == null || candidate.getRows().isEmpty()
			|| candidate.getRows().size() > GuideSourcePolicy.MAX_ROWS
			|| candidate.getRetrievedAt() <= 0 || candidate.getValidatedAt() <= 0)
		{
			throw new IOException("Invalid guide cache");
		}
		for (GuideRow row : candidate.getRows())
		{
			if (row == null || row.getKey() == null || row.getTitle() == null || row.getTitle().isBlank()
				|| row.getWikiTarget() == null
				|| row.getKind() == null || row.getFields() == null
				|| row.getFields().size() != GuideSourcePolicy.COLUMNS
				|| row.getTargets() == null
				|| row.getLinks() == null)
			{
				throw new IOException("Invalid guide row");
			}
			if (row.getFields().entrySet().stream().anyMatch(field -> field.getKey() == null || field.getValue() == null)
				|| row.getLinks().entrySet().stream().anyMatch(link -> link.getKey() == null || link.getValue() == null)
				|| row.getTargets().entrySet().stream().anyMatch(target -> target.getKey() == null || target.getValue() == null))
			{
				throw new IOException("Invalid guide field data; original retained");
			}
		}
		if (previous != null
			&& candidate.getRows().size() < previous.getRows().size() * GuideSourcePolicy.MIN_RETAINED_ROW_RATIO)
		{
			throw new IOException("Suspicious guide truncation; keeping the last valid revision");
		}
	}
}
