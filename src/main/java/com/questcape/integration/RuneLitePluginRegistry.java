package com.questcape.integration;

import javax.inject.*;
import net.runelite.client.plugins.*;

@Singleton
public class RuneLitePluginRegistry
{
	public static final String QUEST_HELPER = "com.questhelper.QuestHelperPlugin";

	public enum State
	{
		ABSENT, DISABLED, INACTIVE, ACTIVE
	}

	private final PluginManager manager;
	private final java.util.function.Function<Plugin, String> identity;
	private volatile State questHelper = State.ABSENT;

	@Inject
	public RuneLitePluginRegistry(PluginManager manager)
	{
		this(manager, plugin -> plugin.getClass().getName());
	}

	RuneLitePluginRegistry(PluginManager manager, java.util.function.Function<Plugin, String> identity)
	{
		this.manager = manager;
		this.identity = identity;
	}

	public void refresh()
	{
		questHelper = find(QUEST_HELPER);
	}

	private State find(String identity)
	{
		for (Plugin plugin : manager.getPlugins())
		{
			if (this.identity.apply(plugin).equals(identity))
			{
				if (!manager.isPluginEnabled(plugin))
				{
					return State.DISABLED;
				}
				return manager.isPluginActive(plugin) ? State.ACTIVE : State.INACTIVE;
			}
		}
		return State.ABSENT;
	}

	public State questHelper()
	{
		return questHelper;
	}

	public static String guidance(String name, State state)
	{
		switch (state)
		{
		case ABSENT:
			return "Install " + name + " from RuneLite's Plugin Hub.";
		case DISABLED:
			return "Enable " + name + " in RuneLite's plugin settings.";
		case INACTIVE:
			return name + " is enabled but has not started. Check its RuneLite error/status.";
		default:
			return name + " is active.";
		}
	}
}
