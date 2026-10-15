package com.questcape;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import com.questcape.ui.GuidePanel;
import com.questcape.persistence.JsonStore;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;

@PluginDescriptor(name = "QuestCape", internalName = "questcape", legacyDataDirectory = "questcape", description = "The ordered OSRS Wiki route with local character progress", tags =
{ "quest", "questcape", "guide", "wiki" })
public class QuestCapePlugin extends Plugin implements GuidePanel.Actions
{
	@Provides
	@Singleton
	JsonStore provideJsonStore(Gson gson)
	{
		return new JsonStore(gson, this::getPluginDirectory);
	}

	@Inject
	private ClientThread clientThread;
	@Inject
	private Client client;
	@Inject
	private ClientToolbar toolbar;
	@Inject
	private GuideRepository guide;
	@Inject
	private ProgressService progress;
	@Inject
	private LiveProgressReader liveReader;
	@Inject
	private TrainingGuideResolver training;
	@Inject
	private BoundedHttp http;
	@Inject
	private BrowserLinks browser;
	@Inject
	private SpriteManager spriteManager;
	@Inject
	private SkillIconManager skillIconManager;
	private volatile QuestCapeSession session;

	@Override
	protected void startUp()
	{
		if (session != null)
		{
			session.close();
		}
		session = new QuestCapeSession(clientThread, client, toolbar, guide, progress, liveReader, training, http,
			browser, spriteManager, skillIconManager);
	}

	@Override
	protected void shutDown()
	{
		QuestCapeSession previous = session;
		session = null;
		if (previous != null)
		{
			previous.close();
		}
	}

	private void withSession(java.util.function.Consumer<QuestCapeSession> action)
	{
		QuestCapeSession current = session;
		if (current != null && current.active())
		{
			action.accept(current);
		}
	}

	@Override
	public void syncPlayer()
	{
		withSession(current -> current.progress.syncPlayer());
	}

	@Override
	public void refresh()
	{
		withSession(current -> current.guide.refresh());
	}

	@Override
	public void training(String skill)
	{
		withSession(current -> current.links.training(skill));
	}

	@Override
	public void manual(String scope, GuideRow row, boolean checked)
	{
		withSession(current -> current.progress.manual(scope, row, checked));
	}

	@Override
	public void source(String url)
	{
		withSession(current -> current.links.source(url));
	}

	@Override
	public void report()
	{
		withSession(current -> current.links.report());
	}

	@Override
	public boolean copyLink(String url)
	{
		QuestCapeSession current = session;
		return current != null && current.active() && current.links.copyLink(url);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		withSession(current -> current.progress.onGameStateChanged(event));
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		withSession(current -> current.progress.onVarbitChanged(event));
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		withSession(current -> current.progress.onStatChanged(event));
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		withSession(current -> current.progress.onGameTick(event));
	}
}
