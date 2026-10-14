package com.questcape;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import com.questcape.ui.GuidePanel;
import com.questcape.ui.SidebarStatus;
import com.questcape.persistence.StoredView;
import com.questcape.integration.OperationOutcome.State;
import com.questcape.persistence.JsonStore;
import com.google.gson.Gson;
import com.google.inject.Provides;
import java.io.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;

@Slf4j
@PluginDescriptor(name = "QuestCape", internalName = "questcape", legacyDataDirectory = "questcape",
	description = "The ordered OSRS Wiki route with local character progress", tags =
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
	private ThreadPoolExecutor worker, network;
	private GuidePanel panel;
	private NavigationButton navigation;
	private volatile boolean running;
	private final AtomicLong generation = new AtomicLong();
	private final AtomicLong liveGeneration = new AtomicLong();
	private final AtomicReference<OperationOutcome> contentStatus = new AtomicReference<>(), progressStatus = new AtomicReference<>();
	private volatile boolean dirty = true, ready;
	private final AtomicBoolean livePending = new AtomicBoolean();
	private final AtomicLong renderQueued = new AtomicLong();

	private static ThreadPoolExecutor executor(String name, int threads)
	{
		return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(16), r ->
		{
			Thread t = new Thread(r, name);
			t.setDaemon(true);
			return t;
		}, new ThreadPoolExecutor.AbortPolicy());
	}

	@Override
	protected void startUp() throws Exception
	{
		running = true;
		long token = generation.incrementAndGet();
		contentStatus.set(new OperationOutcome(token, -1, State.IDLE, ""));
		renderQueued.set(0);
		ready = false;
		dirty = true;
		progressStatus.set(new OperationOutcome(generation.get(), liveGeneration.get(), State.IDLE, ""));
		worker = executor("questcape-state", 1);
		network = executor("questcape-http", 2);
		work(token, () -> createPanel(token));
		work(token, () ->
		{
			try
			{
				guide.load();
				outcome(contentStatus, token, -1, State.IDLE, "Cached guide loaded.");
			}
			catch (IOException e)
			{
				outcome(contentStatus, token, -1, State.FAILED, e.getMessage());
			}
			render(token);
			if (valid(token) && guide.isStale(System.currentTimeMillis()))
			{
				refresh(token);
			}
		});
	}

	/** Resource decoding uses memory only and never runs on ClientThread or the EDT. */
	private void createPanel(long token)
	{
		final java.awt.image.BufferedImage loadedIcon, headerIcon, githubIcon;
		try
		{
			loadedIcon = com.questcape.ui.SidebarImages.load("/quest-route-icon.png");
			headerIcon = net.runelite.client.util.ImageUtil.resizeImage(loadedIcon, 22, 22);
			githubIcon = com.questcape.ui.SidebarImages.load("/github-mark.png");
		}
		catch (IOException | RuntimeException e)
		{
			log.debug("QuestCape sidebar assets unavailable ({})", e.getClass().getSimpleName());
			return;
		}
		Map<Skill, java.awt.image.BufferedImage> icons = new java.util.EnumMap<>(Skill.class);
		Map<Skill, java.awt.image.BufferedImage> projections = new java.util.EnumMap<>(Skill.class);
		loadSkillIcons(icons, projections);
		SwingUtilities.invokeLater(() ->
		{
			if (!valid(token))
			{
				return;
			}
			panel = new GuidePanel(actions(token), training);
			panel.setSkillIcons(icons, projections);
			panel.setHeaderIcons(headerIcon, githubIcon);
			navigation = NavigationButton.builder().tooltip("QuestCape").icon(loadedIcon).priority(6).panel(panel)
				.build();
			toolbar.addNavigation(navigation);
			loadStepIcons(token);
			render(token);
		});
	}

	private void loadStepIcons(long token)
	{
		Map<GuideRow.Kind, Integer> sprites = Map.of(
			GuideRow.Kind.QUEST, SpriteID.AchievementDiaryIcons.BLUE_QUESTS,
			GuideRow.Kind.DIARY, SpriteID.AchievementDiaryIcons.GREEN_ACHIEVEMENT_DIARIES,
			GuideRow.Kind.TRAINING, SpriteID.SideIcons.STATS);
		sprites.forEach((kind, sprite) -> spriteManager.getSpriteAsync(sprite, 0, image ->
		{
			work(token, () ->
			{
				java.awt.image.BufferedImage resized = net.runelite.client.util.ImageUtil.resizeImage(image, 16, 16);
				SwingUtilities.invokeLater(() ->
				{
					if (valid(token) && panel != null)
					{
						panel.setStepSprite(kind, resized);
					}
				});
			});
		}));
	}

	/** Prepare bundled skill images on the worker before publishing any sidebar content. */
	private void loadSkillIcons(Map<Skill, java.awt.image.BufferedImage> icons,
		Map<Skill, java.awt.image.BufferedImage> projections)
	{
		for (Skill skill : Skill.values())
		{
			java.awt.image.BufferedImage image = skillIconManager.getSkillImage(skill, false);
			if (image != null)
			{
				icons.put(skill, net.runelite.client.util.ImageUtil.resizeImage(image, 16, 16));
				projections.put(skill, net.runelite.client.util.ImageUtil.resizeImage(image, 18, 18));
			}
		}
	}

	@Override
	protected void shutDown()
	{
		running = false;
		generation.incrementAndGet();
		liveGeneration.incrementAndGet();
		guide.cancel();
		http.cancel();
		if (worker != null)
		{
			worker.shutdownNow();
		}
		if (network != null)
		{
			network.shutdownNow();
		}
		progress.logout();
		livePending.set(false);
		NavigationButton old = navigation;
		GuidePanel oldPanel = panel;
		SwingUtilities.invokeLater(() ->
		{
			if (oldPanel != null)
			{
				oldPanel.dispose();
			}
			if (old != null)
			{
				toolbar.removeNavigation(old);
			}
		});
		navigation = null;
		panel = null;
	}

	/** Even a callback retained by a disposed widget keeps its originating run. */
	private GuidePanel.Actions actions(long run)
	{
		return new GuidePanel.Actions()
		{
			public void syncPlayer()
			{
				QuestCapePlugin.this.syncPlayer(run);
			}

			public void refresh()
			{
				QuestCapePlugin.this.refresh(run);
			}

			public void training(String skill)
			{
				QuestCapePlugin.this.training(run, skill);
			}

			public void manual(String scope, GuideRow row, boolean checked)
			{
				QuestCapePlugin.this.manual(run, scope, row, checked);
			}

			public void source(String url)
			{
				QuestCapePlugin.this.source(run, url);
			}

			public void report()
			{
				QuestCapePlugin.this.report(run);
			}
		};
	}

	private boolean valid(long token)
	{
		return running && generation.get() == token;
	}

	private boolean work(long token, Runnable action)
	{
		if (!valid(token))
		{
			return false;
		}
		try
		{
			worker.execute(() ->
			{
				if (valid(token))
				{
					action.run();
				}
			});
			return true;
		}
		catch (RejectedExecutionException e)
		{
			show(token, "Work queue is busy. Retry shortly.");
			return false;
		}
	}

	private void show(String value)
	{
		show(generation.get(), value);
	}

	private void show(long token, String value)
	{
		show(token, -1, value);
	}

	private void show(long token, long accountToken, String value)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (valid(token) && panel != null && (accountToken == -1 || accountToken == liveGeneration.get()))
			{
				panel.message(value);
			}
		});
	}

	private void render()
	{
		render(generation.get());
	}

	private void render(long token)
	{
		if (!valid(token) || !renderQueued.compareAndSet(0, token))
		{
			return;
		}
		SwingUtilities.invokeLater(() ->
		{
			if (!renderQueued.compareAndSet(token, 0) || !valid(token) || panel == null)
			{
				return;
			}
			long accountToken = liveGeneration.get(), session = progress.token();
			StoredView<AccountProgress> account = progress.view();
			StoredView<GuideSnapshot> route = guide.view();
			OperationOutcome content = contentStatus.get(), local = progressStatus.get();
			SidebarStatus status = new SidebarStatus(
				content != null && content.getRun() == token ? content.getState() : State.IDLE,
				local != null && local.getRun() == token && local.getAccount() == liveGeneration.get() ? local.getState() : State.IDLE,
				route.getSaveState(), account.getSaveState(),
				content != null && content.getRun() == token ? content.getDetail() : "",
				local != null && local.getRun() == token && local.getAccount() == liveGeneration.get() ? local.getDetail() : "");
			if (!valid(token) || accountToken != liveGeneration.get() || session != progress.token())
			{
				render(token);
				return;
			}
			panel.render(route.getData(), account.getData(), status, ready);
		});
	}

	private void outcome(AtomicReference<OperationOutcome> holder, long run, long account, State state, String detail)
	{
		OperationOutcome previous = holder.get();
		if (previous != null && previous.getRun() == run && valid(run)
			&& (account == -1 || account == liveGeneration.get()))
		{
			holder.compareAndSet(previous, new OperationOutcome(run, account, state, detail));
		}
	}

	@Override
	public void refresh()
	{
		refresh(generation.get());
	}

	private void refresh(long token)
	{
		if (!valid(token))
		{
			return;
		}
		outcome(contentStatus, token, -1, State.RUNNING, "Checking wiki…");
		render(token);
		try
		{
			guide.refresh(network, System.currentTimeMillis()).whenComplete((snapshot, failure) ->
			{
				if (!valid(token))
				{
					return;
				}
				outcome(contentStatus, token, -1, failure == null ? State.IDLE : State.FAILED, failure == null ? "Guide is up to date." : "Refresh failed: " + cause(failure));
				render(token);
			});
		}
		catch (RejectedExecutionException e)
		{
			outcome(contentStatus, token, -1, State.FAILED, "Request queue is busy.");
			render(token);
		}
	}

	@Override
	public void syncPlayer()
	{
		syncPlayer(generation.get());
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
				show(life,accountToken,"Log in and wait for your character to be ready before syncing.");
				return;
			}
			captureProgress(life, accountToken, true, expected.getScope());
		});
	}

	@Override
	public void manual(String scope, GuideRow row, boolean checked)
	{
		manual(generation.get(),scope,row,checked);
	}

	private void manual(long token,String scope,GuideRow row,boolean checked)
	{
		long accountToken = liveGeneration.get();
		work(token, () ->
		{
			try
			{
				if (accountToken != liveGeneration.get()) { return; }
				progress.toggleManual(scope, row, checked);
			}
			catch (IOException e)
			{
				show(token,accountToken,e.getMessage());
			}
			render(token);
		});
	}

	@Override
	public void training(String skill)
	{
		training(generation.get(),skill);
	}

	private void training(long token,String skill)
	{
		if (!valid(token)) { return; }
		String url = training.resolve(skill);
		long accountToken = liveGeneration.get();
		show(token,accountToken,"Checking Theoatrix destination…");
		try
		{
			network.execute(() ->
			{
				try
				{
					if (!valid(token) || accountToken!=liveGeneration.get()) { return; }
					String verified = training.verifyDestination(url);
					SwingUtilities.invokeLater(() ->
					{
						if (valid(token) && accountToken == liveGeneration.get())
						{
							show(token,accountToken,browser.open(verified));
						}
					});
				}
				catch (IOException | RuntimeException e)
				{
					if (valid(token) && accountToken == liveGeneration.get())
					{
						show(token,accountToken,e.getMessage() + " Link: " + url
							+ " · Click the training action to retry. Browse Theoatrix guides: "
							+ TrainingGuideResolver.FALLBACK);
					}
				}
			});
		}
		catch (RejectedExecutionException e)
		{
			show(token, accountToken, "Request queue is busy. Retry shortly.");
		}
	}

	@Override
	public void source(String url)
	{
		source(generation.get(),url);
	}

	private void source(long token,String url)
	{
		if (!valid(token)) { return; }
		if (GuideParser.safeWikiLink(url) != null || ApplicationLinks.CONTENT_LICENSE.equals(url))
		{
			show(token,browser.open(url));
		}
	}

	@Override
	public void report()
	{
		report(generation.get());
	}

	private void report(long token)
	{
		if (valid(token)) { show(token,browser.open(ApplicationLinks.ISSUES)); }
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		dirty = true;
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
		{
			ready = false;
			liveGeneration.incrementAndGet();
			progress.logout();
			livePending.set(false);
			progressStatus.set(new OperationOutcome(generation.get(), liveGeneration.get(), State.IDLE, ""));
			show("");
			render();
		}
		if (state == GameState.LOGGED_IN)
		{
			ready = false;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		dirty = true;
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		dirty = true;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!running || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		ready = true;
		if (!dirty)
		{
			return;
		}
		captureProgress(generation.get(), liveGeneration.get(), false, null);
	}

	/** Called on ClientThread. Only the immutable observation crosses to the disk worker. */
	private void captureProgress(long life, long accountToken, boolean explicit, String expectedScope)
	{
		if (!livePending.compareAndSet(false, true))
		{
			if (explicit)
			{
				show(life,accountToken,"A character sync is already running. Retry shortly.");
			}
			return;
		}
		dirty = false;
		long session = progress.token();
		try
		{
			AccountProgress observation = liveReader.read();
			if (observation == null || (expectedScope != null && !expectedScope.equals(observation.getScope())))
			{
				dirty = true;
				livePending.set(false);
				if (explicit)
				{
					show(life,accountToken,"Character changed or is not ready. Wait for the next game tick and retry.");
				}
				return;
			}
			if (explicit)
			{
				outcome(progressStatus, life, accountToken, State.RUNNING, "Resyncing progress…");
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
						show(life,accountToken,"");
					}
					if (explicit || newAccount)
					{
						outcome(progressStatus, life, accountToken, State.IDLE, "");
					}
					render(life);
				}
				catch (IOException e)
				{
					if (valid(life) && accountToken == liveGeneration.get())
					{
						dirty = true;
						outcome(progressStatus, life, accountToken, State.FAILED, "Could not save character progress");
						show(life,accountToken,e.getMessage());
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
			}
		}
		catch (RuntimeException e)
		{
			dirty = true;
			livePending.set(false);
			show(life,accountToken,"Character progress is not ready yet. Retry after the next game tick.");
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
