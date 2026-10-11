package com.questcape.progress;

import com.questcape.guide.GuideRow;
import com.questcape.persistence.JsonStore;
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
	private long session;
	private final Set<String> unreadable = new HashSet<>();
	private volatile String persistenceStatus = "";
	private long nextSaveAt;

	@Inject
	public ProgressService(JsonStore store)
	{
		this.store = store;
	}

	private void load(String scope, boolean retry) throws IOException
	{
		if (records.containsKey(scope) && (!unreadable.contains(scope) || !retry))
		{
			return;
		}
		AccountProgress saved = store.read("progress", scope, AccountProgress.class);
		if (saved != null)
		{
			if (!scope.equals(saved.getScope()) || saved.getQuests() == null || saved.getLevels() == null
				|| saved.getManual() == null || saved.getActivities() == null || saved.getUsername() == null
				|| saved.getMode() == null)
			{
				throw new IOException("Invalid account cache; original retained");
			}
			AccountProgress pending = records.get(scope);
			if (pending != null && unreadable.contains(scope))
			{
				Set<String> checks = new HashSet<>(saved.getManual());
				checks.addAll(pending.getManual());
				saved = saved.withManual(checks);
			}
			records.put(scope, saved);
		}
		unreadable.remove(scope);
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
			load(observation.getScope(), force);
		}
		catch (IOException e)
		{
			unreadable.add(observation.getScope());
			log.debug("QuestCape account cache could not be read ({})", e.getClass().getSimpleName());
		}
		AccountProgress current = live;
		boolean unchanged = current != null && current.getScope().equals(observation.getScope())
			&& current.getUsername().equals(observation.getUsername())
			&& current.getQuests().equals(observation.getQuests())
			&& current.getLevels().equals(observation.getLevels())
			&& current.getActivities().equals(observation.getActivities());
		if (!force && unchanged && persistenceStatus.isEmpty()
			&& observation.getRetrievedAt() - current.getRetrievedAt() < 60_000)
		{
			return expectedSession == token();
		}
		AccountProgress previous = records.get(observation.getScope());
		AccountProgress next = observation.withManual(previous == null ? Collections.emptySet() : previous.getManual());
		if (expectedSession != token())
		{
			return false;
		}
		records.put(next.getScope(), next);
		synchronized (this)
		{
			if (expectedSession != session)
			{
				return false;
			}
			live = next;
		}
		if (current == null || !current.getScope().equals(next.getScope()))
		{
			nextSaveAt = 0;
		}
		save(next, expectedSession, force);
		return expectedSession == token();
	}

	/** Worker-only persistence; publication never needs to acquire the store's disk lock. */
	private void save(AccountProgress record, long expectedSession, boolean force)
	{
		long now = System.currentTimeMillis();
		String status = "Live progress · unsaved; cache unavailable. Sync to retry.";
		if (!unreadable.contains(record.getScope()) && (force || now >= nextSaveAt))
		{
			try
			{
				store.write("progress", record.getScope(), record);
				status = "";
				nextSaveAt = 0;
			}
			catch (IOException e)
			{
				nextSaveAt = now + 60_000;
				log.debug("QuestCape progress cache write failed ({})", e.getClass().getSimpleName());
			}
		}
		synchronized (this)
		{
			if (session == expectedSession)
			{
				persistenceStatus = status;
			}
		}
	}

	public String persistenceStatus()
	{
		return persistenceStatus;
	}

	public synchronized void logout()
	{
		live = null;
		persistenceStatus = "";
		session++;
	}

	public synchronized long token()
	{
		return session;
	}

	public AccountProgress current()
	{
		return live;
	}

	public void toggleManual(String expectedScope, GuideRow row, boolean checked) throws IOException
	{
		long expectedSession = token();
		AccountProgress record = live;
		if (record == null || !record.getScope().equals(expectedScope) || !row.isManual())
		{
			return;
		}
		Set<String> checks = new HashSet<>(record.getManual());
		if (checked)
		{
			checks.add(row.getKey());
		}
		else
		{
			checks.remove(row.getKey());
		}
		AccountProgress changed = record.withManual(checks);
		records.put(changed.getScope(), changed);
		synchronized (this)
		{
			if (expectedSession == session && live == record)
			{
				live = changed;
			}
		}
		save(changed, expectedSession, true);
	}
}
