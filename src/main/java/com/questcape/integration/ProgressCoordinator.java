package com.questcape.integration;

import com.questcape.guide.*;
import com.questcape.progress.*;
import com.questcape.integration.OperationOutcome.State;
import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.function.BiConsumer;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;

/** ClientThread readiness/capture and scoped immutable handoff to the run's serial worker. */
public final class ProgressCoordinator
{
	private static final long CLOSED_SESSION = -1;
	private static final String QUEUE_BUSY = "Work queue is busy. Retry shortly.",
		NOT_READY = "Log in and wait for your character to be ready before syncing.",
		ALREADY_SYNCING = "A character sync is already running. Retry shortly.",
		CHARACTER_CHANGED = "Character changed or is not ready. Wait for the next game tick and retry.",
		RESYNCING = "Resyncing progress…", SAVE_FAILED = "Could not save character progress",
		CAPTURE_UNAVAILABLE = "Character progress is not ready yet. Retry after the next game tick.";
	private final PluginRun run;
	private final ClientThread clientThread;
	private final Client client;
	private final ProgressService progress;
	private final LiveProgressReader liveReader;
	private final Runnable changed;
	private final BiConsumer<Long, String> message;
	private final AtomicLong liveGeneration = new AtomicLong();
	private final AtomicLong serviceSession;
	private final AtomicBoolean livePending = new AtomicBoolean();
	private final AtomicReference<OperationOutcome> progressStatus;
	private volatile boolean ready, dirty = true;

	public ProgressCoordinator(PluginRun run, ClientThread clientThread, Client client, ProgressService progress,
		LiveProgressReader liveReader, Runnable changed, BiConsumer<Long, String> message)
	{
		this.run = run;
		this.clientThread = clientThread;
		this.client = client;
		this.progress = progress;
		this.liveReader = liveReader;
		this.changed = changed;
		this.message = message;
		serviceSession = new AtomicLong(progress.token());
		progressStatus = new AtomicReference<>(new OperationOutcome(run.id(), 0, State.IDLE, ""));
	}

	public boolean ready()
	{
		return ready;
	}

	public long authority()
	{
		return liveGeneration.get();
	}

	public OperationOutcome status()
	{
		return progressStatus.get();
	}

	public void syncPlayer()
	{
		syncPlayer(run.id());
	}

	public void manual(String scope, GuideRow row, boolean checked)
	{
		manual(run.id(), scope, row, checked);
	}

	private boolean valid(long token)
	{
		return run.active() && token == run.id();
	}

	private boolean work(long token, Runnable action)
	{
		boolean accepted = valid(token) && run.work(action);
		if (!accepted && valid(token))
		{
			show(token, authority(), QUEUE_BUSY);
		}
		return accepted;
	}

	private void show(long token, long account, String value)
	{
		if (valid(token))
		{
			message.accept(account, value);
		}
	}

	private void show(String value)
	{
		show(run.id(), authority(), value);
	}

	private void render(long token)
	{
		if (valid(token))
		{
			changed.run();
		}
	}

	private void render()
	{
		render(run.id());
	}

	private void outcome(AtomicReference<OperationOutcome> holder, long token, long account, State state, String detail)
	{
		OperationOutcome previous = holder.get();
		if (valid(token) && account == authority())
		{
			holder.compareAndSet(previous, new OperationOutcome(token, account, state, detail));
		}
	}

	private void syncPlayer(long life)
	{
		if (!valid(life))
		{
			return;
		}
		long accountToken = liveGeneration.get();
		AccountProgress expected = progress.current();
		clientThread.invokeLater(() ->
		{
			if (!valid(life) || accountToken != liveGeneration.get())
			{
				return;
			}
			if (!ready || expected == null || client.getGameState() != GameState.LOGGED_IN)
			{
				show(life, accountToken, NOT_READY);
				return;
			}
			captureProgress(life, accountToken, true, expected.getScope());
		});
	}

	private void manual(long token, String scope, GuideRow row, boolean checked)
	{
		long accountToken = liveGeneration.get();
		long session = serviceSession.get();
		work(token, () ->
		{
			try
			{
				if (accountToken != liveGeneration.get())
				{
					return;
				}
				progress.toggleManual(scope, row, checked, session);
			}
			catch (IOException e)
			{
				show(token, accountToken, e.getMessage());
			}
			render(token);
		});
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (!run.active())
		{
			return;
		}
		dirty = true;
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
		{
			ready = false;
			liveGeneration.incrementAndGet();
			invalidateSession();
			livePending.set(false);
			progressStatus.set(new OperationOutcome(run.id(), liveGeneration.get(), State.IDLE, ""));
			show("");
			render();
		}
		if (state == GameState.LOGGED_IN)
		{
			ready = false;
		}
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		dirty = true;
	}

	public void onStatChanged(StatChanged event)
	{
		dirty = true;
	}

	public void onGameTick(GameTick event)
	{
		if (!run.active() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		ready = true;
		if (!dirty)
		{
			return;
		}
		captureProgress(run.id(), liveGeneration.get(), false, null);
	}

	private void captureProgress(long life, long accountToken, boolean explicit, String expectedScope)
	{
		if (!livePending.compareAndSet(false, true))
		{
			if (explicit)
			{
				show(life, accountToken, ALREADY_SYNCING);
			}
			return;
		}
		dirty = false;
		long session = serviceSession.get();
		try
		{
			AccountProgress observation = liveReader.read();
			if (observation == null || (expectedScope != null && !expectedScope.equals(observation.getScope())))
			{
				dirty = true;
				livePending.set(false);
				if (explicit)
				{
					show(life, accountToken, CHARACTER_CHANGED);
				}
				return;
			}
			if (explicit)
			{
				outcome(progressStatus, life, accountToken, State.RUNNING, RESYNCING);
				render(life);
			}
			if (!work(life, () ->
			{
				try
				{
					if (!valid(life) || accountToken != liveGeneration.get())
					{
						return;
					}
					AccountProgress previous = progress.current();
					boolean newAccount = previous == null || !previous.getScope().equals(observation.getScope());
					if (!progress.acceptLive(observation, session, explicit))
					{
						return;
					}
					if (!valid(life) || accountToken != liveGeneration.get())
					{
						return;
					}
					if (newAccount)
					{
						show(life, accountToken, "");
					}
					outcome(progressStatus, life, accountToken, State.IDLE, "");
					render(life);
				}
				catch (IOException e)
				{
					if (valid(life) && accountToken == liveGeneration.get())
					{
						dirty = true;
						outcome(progressStatus, life, accountToken, State.FAILED, SAVE_FAILED);
						show(life, accountToken, e.getMessage());
						render(life);
					}
				}
				finally
				{
					if (valid(life) && accountToken == liveGeneration.get())
					{
						livePending.set(false);
					}
				}
			}))
			{
				dirty = true;
				livePending.set(false);
				outcome(progressStatus, life, accountToken, State.FAILED, QUEUE_BUSY);
			}
		}
		catch (RuntimeException e)
		{
			dirty = true;
			livePending.set(false);
			show(life, accountToken, CAPTURE_UNAVAILABLE);
		}
	}

	private void invalidateSession()
	{
		long expected = serviceSession.get();
		if (expected >= 0 && progress.logoutIfSession(expected))
		{
			serviceSession.compareAndSet(expected, expected + 1);
		}
	}

	public void close()
	{
		ready = false;
		liveGeneration.incrementAndGet();
		livePending.set(false);
		long expected = serviceSession.getAndSet(CLOSED_SESSION);
		if (expected >= 0)
		{
			progress.logoutIfSession(expected);
		}
	}

}
