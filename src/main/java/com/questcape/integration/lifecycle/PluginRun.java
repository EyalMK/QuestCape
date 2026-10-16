package com.questcape.integration.lifecycle;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import lombok.extern.slf4j.Slf4j;

/** Owns only resources and original enablement authority, never domain or widget state. */
@Slf4j
public final class PluginRun implements AutoCloseable
{
	private static final int STATE_WORKERS = 1, NETWORK_WORKERS = 2, QUEUE_CAPACITY = 16;
	private static final AtomicLong NEXT_ID = new AtomicLong();
	private final long id = NEXT_ID.incrementAndGet();
	private final AtomicBoolean active = new AtomicBoolean(true);
	private final List<Runnable> cleanup = new ArrayList<>();
	private final ThreadPoolExecutor state, network;

	public PluginRun()
	{
		state = executor("questcape-state", STATE_WORKERS);
		try
		{
			network = executor("questcape-http", NETWORK_WORKERS);
		}
		catch (RuntimeException failure)
		{
			state.shutdownNow();
			throw failure;
		}
	}

	private static ThreadPoolExecutor executor(String name, int threads)
	{
		return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY),
			task ->
			{
				Thread worker = new Thread(task, name);
				worker.setDaemon(true);
				return worker;
			}, new ThreadPoolExecutor.AbortPolicy());
	}

	public long id()
	{
		return id;
	}

	public boolean active()
	{
		return active.get();
	}

	public Executor network()
	{
		return network;
	}

	public boolean work(Runnable task)
	{
		return submit(state, task);
	}

	public boolean network(Runnable task)
	{
		return submit(network, task);
	}

	private boolean submit(Executor executor, Runnable task)
	{
		if (!active())
		{
			return false;
		}
		try
		{
			executor.execute(() ->
			{
				if (active())
				{
					task.run();
				}
			});
			return true;
		}
		catch (RejectedExecutionException failure)
		{
			return false;
		}
	}

	public void own(Runnable release)
	{
		synchronized (cleanup)
		{
			if (active())
			{
				cleanup.add(release);
				return;
			}
		}
		release.run();
	}

	@Override
	public void close()
	{
		if (!active.compareAndSet(true, false))
		{
			return;
		}
		state.shutdownNow();
		network.shutdownNow();
		List<Runnable> acquired;
		synchronized (cleanup)
		{
			acquired = new ArrayList<>(cleanup);
			cleanup.clear();
		}
		Collections.reverse(acquired);
		for (Runnable release : acquired)
		{
			try
			{
				release.run();
			}
			catch (RuntimeException failure)
			{
				log.debug("QuestCape resource cleanup failed ({})", failure.getClass().getSimpleName());
			}
		}
	}
}
