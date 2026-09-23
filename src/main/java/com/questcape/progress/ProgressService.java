package com.questcape.progress;

import com.questcape.guide.GuideRow;
import com.questcape.persistence.JsonStore;
import java.io.*;
import java.util.*;
import javax.inject.*;

/** Disk operations run on the serial worker; client/EDT reads and logout never wait for disk. */
@Singleton
public class ProgressService
{
	private final JsonStore store;
	private final Map<String, AccountProgress> records = new java.util.concurrent.ConcurrentHashMap<>();
	private volatile AccountProgress live;
	private long session;

	@Inject
	public ProgressService(JsonStore store)
	{
		this.store = store;
	}

	private void load(String scope) throws IOException
	{
		if (records.containsKey(scope))
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
			records.put(scope, saved);
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
		load(observation.getScope());
		AccountProgress current = live;
		boolean unchanged = current != null && current.getScope().equals(observation.getScope())
			&& current.getUsername().equals(observation.getUsername())
			&& current.getQuests().equals(observation.getQuests())
			&& current.getLevels().equals(observation.getLevels())
			&& current.getActivities().equals(observation.getActivities());
		if (!force && unchanged && observation.getRetrievedAt() - current.getRetrievedAt() < 60_000)
		{
			return expectedSession == token();
		}
		AccountProgress previous = records.get(observation.getScope());
		AccountProgress next = observation.withManual(previous == null ? Collections.emptySet() : previous.getManual());
		if (expectedSession != token())
		{
			return false;
		}
		store.write("progress", next.getScope(), next);
		records.put(next.getScope(), next);
		synchronized (this)
		{
			if (expectedSession != session)
			{
				return false;
			}
			live = next;
			return true;
		}
	}

	public synchronized void logout()
	{
		live = null;
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

	public AccountProgress viewed()
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
		store.write("progress", changed.getScope(), changed);
		records.put(changed.getScope(), changed);
		synchronized (this)
		{
			if (expectedSession == session && live == record)
			{
				live = changed;
			}
		}
	}
}
