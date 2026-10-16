package com.questcape.progress.tracking;

import com.questcape.guide.model.GuideRow;
import com.questcape.persistence.CachePolicy;
import com.questcape.persistence.JsonStore;
import com.questcape.persistence.StoredView;
import com.questcape.persistence.StoredView.SaveState;
import com.questcape.progress.model.AccountProgress;
import java.io.*;
import java.util.*;
import javax.inject.*;
import lombok.extern.slf4j.Slf4j;

/** Disk operations run on the serial worker; client/EDT reads and logout never wait for disk. */
@Singleton
@Slf4j
public class ProgressService
{
	private final JsonStore store;
	private final Map<String, AccountProgress> records = new java.util.concurrent.ConcurrentHashMap<>();
	private volatile AccountProgress live;
	private volatile StoredView<AccountProgress> published = StoredView.empty();
	private final PendingManualEdits edits = new PendingManualEdits();
	private long session;
	private final Set<String> unreadable = java.util.concurrent.ConcurrentHashMap.newKeySet();
	private volatile String persistenceStatus = "";
	private volatile long nextSaveAt;

	@Inject
	public ProgressService(JsonStore store)
	{
		this.store = store;
	}

	private void load(String scope, boolean retry, long expectedSession) throws IOException
	{
		if (records.containsKey(scope) && (!unreadable.contains(scope) || !retry))
		{
			return;
		}
		AccountProgress saved = store.read(CachePolicy.PROGRESS, scope, AccountProgress.class);
		if (saved != null)
		{
			if (!scope.equals(saved.getScope()) || saved.getQuests() == null || saved.getLevels() == null
				|| saved.getManual() == null || saved.getActivities() == null || saved.getUsername() == null
				|| saved.getMode() == null)
			{
				throw new IOException("Invalid account cache; original retained");
			}
		}
		synchronized (this)
		{
			if (expectedSession != session)
			{
				return;
			}
			if (saved != null)
			{
				records.put(scope, saved.withManual(edits.overlay(scope, saved.getManual())));
			}
			unreadable.remove(scope);
		}
	}

	public void acceptLive(AccountProgress observation) throws IOException
	{
		acceptLive(observation, token(), false);
	}

	/** A session token also prevents a disk operation finishing after logout from republishing a character. */
	public boolean acceptLive(AccountProgress observation, long expectedSession, boolean force) throws IOException
	{
		if (observation == null || expectedSession != token())
		{
			return false;
		}
		try
		{
			load(observation.getScope(), force, expectedSession);
		}
		catch (IOException e)
		{
			synchronized (this)
			{
				if (expectedSession == session)
				{
					unreadable.add(observation.getScope());
				}
			}
			log.debug("QuestCape account cache could not be read ({})", e.getClass().getSimpleName());
		}
		AccountProgress current = live;
		boolean unchanged = current != null && current.getScope().equals(observation.getScope())
			&& current.getUsername().equals(observation.getUsername())
			&& current.getQuests().equals(observation.getQuests())
			&& current.getLevels().equals(observation.getLevels())
			&& current.getActivities().equals(observation.getActivities());
		if (!force && unchanged && published.getSaveState() == SaveState.SAVED
			&& observation.getRetrievedAt() - current.getRetrievedAt() < CachePolicy.UNCHANGED_SAVE_MILLIS)
		{
			return expectedSession == token();
		}
		AccountProgress previous = records.get(observation.getScope());
		AccountProgress next = observation.withManual(edits.overlay(observation.getScope(),
			previous == null ? Collections.emptySet() : previous.getManual()));
		Map<String, PendingManualEdits.Edit> captured;
		if (expectedSession != token())
		{
			return false;
		}
		synchronized (this)
		{
			if (expectedSession != session)
			{
				return false;
			}
			records.put(next.getScope(), next);
			live = next;
			published = new StoredView<>(next, SaveState.PENDING);
			captured = edits.snapshot(next.getScope());
			if (current == null || !current.getScope().equals(next.getScope()))
			{
				nextSaveAt = 0;
			}
		}
		save(next, expectedSession, force, captured);
		return expectedSession == token();
	}

	/** Worker-only persistence; publication never needs to acquire the store's disk lock. */
	private void save(AccountProgress record, long expectedSession, boolean force,
		Map<String, PendingManualEdits.Edit> captured)
	{
		long now = System.currentTimeMillis();
		SaveState saved = SaveState.UNSAVED;
		long retryAt = nextSaveAt;
		if (!unreadable.contains(record.getScope()) && (force || now >= nextSaveAt))
		{
			try
			{
				store.write(CachePolicy.PROGRESS, record.getScope(), record);
				saved = SaveState.SAVED;
				edits.acknowledge(record.getScope(), captured);
				retryAt = 0;
			}
			catch (IOException e)
			{
				retryAt = now + CachePolicy.SAVE_RETRY_MILLIS;
				log.debug("QuestCape progress cache write failed ({})", e.getClass().getSimpleName());
			}
		}
		synchronized (this)
		{
			if (session == expectedSession && live == record)
			{
				nextSaveAt = retryAt;
				published = new StoredView<>(record, saved);
				persistenceStatus = saved == SaveState.UNSAVED
					? "Live progress · unsaved; cache unavailable. Resync Progress to retry."
					: "";
			}
		}
	}

	public String persistenceStatus()
	{
		return persistenceStatus;
	}

	public synchronized boolean logoutIfSession(long expected)
	{
		if (session != expected)
		{
			return false;
		}
		logout();
		return true;
	}

	public synchronized void logout()
	{
		live = null;
		published = StoredView.empty();
		persistenceStatus = "";
		session++;
	}

	public synchronized long token()
	{
		return session;
	}

	public StoredView<AccountProgress> view()
	{
		return published;
	}

	public AccountProgress current()
	{
		return live;
	}

	public void toggleManual(String expectedScope, GuideRow row, boolean checked) throws IOException
	{
		toggleManual(expectedScope, row, checked, token());
	}

	/** The coordinator passes its original service session; queued old actions cannot adopt a later login. */
	public void toggleManual(String expectedScope, GuideRow row, boolean checked, long expectedSession)
		throws IOException
	{
		AccountProgress record = live;
		if (expectedSession != token() || record == null || !record.getScope().equals(expectedScope) || !row.isManual())
		{
			return;
		}
		AccountProgress changed;
		Map<String, PendingManualEdits.Edit> captured;
		synchronized (this)
		{
			if (expectedSession != session || live != record)
			{
				return;
			}
			edits.put(expectedScope, row.getKey(), checked);
			changed = record.withManual(edits.overlay(expectedScope, record.getManual()));
			captured = edits.snapshot(expectedScope);
			records.put(changed.getScope(), changed);
			live = changed;
			published = new StoredView<>(changed, SaveState.PENDING);
		}
		save(changed, expectedSession, true, captured);
	}
}
