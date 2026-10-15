package com.questcape.integration;

import com.questcape.guide.GuideRepository;
import com.questcape.integration.OperationOutcome.State;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Guide request lifecycle and its typed outcome, tied to the original enabled run. */
public final class GuideRefreshCoordinator
{
	private final PluginRun run;
	private final GuideRepository guide;
	private final long authority;
	private final Runnable render;
	private final AtomicReference<OperationOutcome> status;

	public GuideRefreshCoordinator(PluginRun run, GuideRepository guide, Runnable render)
	{
		this.run = run;
		this.guide = guide;
		this.render = render;
		this.authority = guide.token();
		status = new AtomicReference<>(new OperationOutcome(run.id(), -1, State.IDLE, ""));
	}

	public OperationOutcome status()
	{
		return status.get();
	}

	public void start()
	{
		run.work(() ->
		{
			try
			{
				guide.load(authority);
				outcome(State.IDLE, "");
			}
			catch (IOException | RuntimeException failure)
			{
				outcome(State.FAILED, failure.getMessage());
			}
			if (run.active() && guide.isStale(System.currentTimeMillis()))
			{
				refresh();
			}
		});
	}

	public void refresh()
	{
		if (!run.active())
		{
			return;
		}
		outcome(State.RUNNING, "");
		try
		{
			guide.refresh(run.network(), System.currentTimeMillis(), authority).whenComplete((snapshot, failure) ->
			{
				if (run.active())
				{
					outcome(failure == null ? State.IDLE : State.FAILED,
						failure == null ? "" : "Refresh failed: " + cause(failure));
				}
			});
		}
		catch (RejectedExecutionException failure)
		{
			outcome(State.FAILED, "Request queue is busy.");
		}
	}

	private void outcome(State state, String detail)
	{
		if (run.active())
		{
			status.set(new OperationOutcome(run.id(), -1, state, detail));
			render.run();
		}
	}

	private static String cause(Throwable failure)
	{
		while (failure.getCause() != null)
		{
			failure = failure.getCause();
		}
		return failure.getMessage();
	}
}
