package com.questcape.integration;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.repository.GuideRepository;
import com.questcape.integration.coordination.GuideRefreshCoordinator;
import com.questcape.integration.coordination.ProgressCoordinator;
import com.questcape.integration.http.BoundedHttp;
import com.questcape.integration.lifecycle.PluginRun;
import com.questcape.integration.links.BrowserLinks;
import com.questcape.integration.links.TrainingGuideResolver;
import com.questcape.integration.links.TrainingLinkController;
import com.questcape.progress.capture.LiveProgressReader;
import com.questcape.progress.tracking.ProgressService;
import com.questcape.ui.GuideActions;
import com.questcape.ui.sidebar.SidebarController;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.*;
import net.runelite.client.ui.ClientToolbar;

/** Composition root for one enabled run. It owns no capture, completion or widget algorithms. */
public final class QuestCapeSession implements AutoCloseable
{
	private final PluginRun run;
	public final ProgressCoordinator progress;
	public final GuideRefreshCoordinator guide;
	public final TrainingLinkController links;

	public QuestCapeSession(ClientThread clientThread, Client client, ClientToolbar toolbar, GuideRepository repository,
		ProgressService progressService,
		LiveProgressReader reader, TrainingGuideResolver training, BoundedHttp http, BrowserLinks browser,
		SpriteManager sprites, SkillIconManager skills)
	{
		run = new PluginRun();
		try
		{
			run.own(repository::cancel);
			run.own(http::cancel);
			AtomicReference<SidebarController> target = new AtomicReference<>();
			Runnable render = () ->
			{
				SidebarController view = target.get();
				if (view != null)
				{
					view.render();
				}
			};
			BiConsumer<Long, String> message = (authority, text) ->
			{
				SidebarController view = target.get();
				if (view != null)
				{
					view.message(authority, text);
				}
			};
			progress = new ProgressCoordinator(run, clientThread, client, progressService, reader, render, message);
			run.own(progress::close);
			guide = new GuideRefreshCoordinator(run, repository, render);
			links = new TrainingLinkController(run, training, browser, progress::authority, message);
			GuideActions actions = new GuideActions()
			{
				public void syncPlayer()
				{
					progress.syncPlayer();
				}

				public void refresh()
				{
					guide.refresh();
				}

				public void training(String skill)
				{
					links.training(skill);
				}

				public void manual(String scope, GuideRow row, boolean checked)
				{
					progress.manual(scope, row, checked);
				}

				public void source(String url)
				{
					links.source(url);
				}

				public void report()
				{
					links.report();
				}

				public boolean copyLink(String url)
				{
					return links.copyLink(url);
				}
			};
			SidebarController sidebar = new SidebarController(run, toolbar, sprites, skills, repository,
				progressService, progress, guide, training, actions);
			target.set(sidebar);
			run.own(sidebar::close);
			sidebar.start();
			guide.start();
		}
		catch (RuntimeException failure)
		{
			run.close();
			throw failure;
		}
	}

	public boolean active()
	{
		return run.active();
	}

	@Override
	public void close()
	{
		run.close();
	}
}
