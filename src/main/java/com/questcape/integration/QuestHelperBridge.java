package com.questcape.integration;

import javax.inject.*;
import lombok.Value;

/** Fail-closed until a distributed, supported launch AND observation contract is available. */
@Singleton
public class QuestHelperBridge
{
	public enum State
	{
		UNAVAILABLE, LOGGED_OUT, UNSUPPORTED, INCOMPATIBLE, SEARCH_READY, RESULT_SELECTED, PENDING, CONFIRMED,
		ALREADY_ACTIVE, FAILED
	}

	@Value
	public static class Result
	{
		State state;
		String message;
	}

	/** Internal adapter result, not a claim that an external message/observation API exists. */
	@Value
	public static class Selection
	{
		boolean known;
		String questIdentity;
	}

	private final RuneLitePluginRegistry registry;

	@Inject
	public QuestHelperBridge(RuneLitePluginRegistry registry)
	{
		this.registry = registry;
	}

	public Result launch(String identity, boolean loggedIn)
	{
		if (!loggedIn)
		{
			return new Result(State.LOGGED_OUT, "Log in before selecting a quest helper.");
		}
		if (registry.questHelper() != RuneLitePluginRegistry.State.ACTIVE)
		{
			return new Result(State.UNAVAILABLE,
				RuneLitePluginRegistry.guidance("Quest Helper", registry.questHelper()));
		}
		if (identity == null)
		{
			return new Result(State.UNSUPPORTED, "This quest has no verified canonical helper mapping.");
		}
		return new Result(State.INCOMPATIBLE, "Use Quest Helper search to select this quest.");
	}

	public boolean canConfirmLaunch()
	{
		return false;
	}

	public Selection selectedHelper()
	{
		return new Selection(false, null);
	}

	public String availability()
	{
		return registry.questHelper() == RuneLitePluginRegistry.State.ACTIVE
			? "Quest Helper opens a single search result automatically. Choose between multiple matches; automatic resume is unavailable."
			: RuneLitePluginRegistry.guidance("Quest Helper", registry.questHelper());
	}
}
