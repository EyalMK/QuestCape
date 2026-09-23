package com.questcape;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import com.questcape.ui.GuidePanel;
import java.io.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;

@PluginDescriptor(name = "QuestCape", description = "The ordered OSRS Wiki route with local character progress", tags =
{ "quest", "questcape", "guide", "wiki" })
public class QuestCapePlugin extends Plugin implements GuidePanel.Actions
{
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
	private ThreadPoolExecutor worker, network;
	private GuidePanel panel;
	private NavigationButton navigation;
	private volatile boolean running;
	private final AtomicLong generation = new AtomicLong();
	private final AtomicLong liveGeneration = new AtomicLong();
	private volatile String contentStatus = "", progressStatus = "";
	private volatile boolean dirty = true, ready;
	private final AtomicBoolean livePending = new AtomicBoolean();
	private final AtomicBoolean renderQueued = new AtomicBoolean();

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
		ready = false;
		dirty = true;
		progressStatus = "";
		worker = executor("questcape-state", 1);
		network = executor("questcape-http", 2);
		java.awt.image.BufferedImage icon;
		try (InputStream stream = getClass().getResourceAsStream("/quest-route-icon.png"))
		{
			if (stream == null)
			{
				throw new IOException("Missing navigation icon");
			}
			icon = ImageIO.read(stream);
		}
		final java.awt.image.BufferedImage loadedIcon = icon;
		SwingUtilities.invokeLater(() ->
		{
			if (!valid(token))
			{
				return;
			}
			panel = new GuidePanel(this, training);
			navigation = NavigationButton.builder().tooltip("QuestCape").icon(loadedIcon).priority(6).panel(panel)
				.build();
			toolbar.addNavigation(navigation);
			loadStepIcons(token);
			render();
		});
		work(() ->
		{
			try
			{
				guide.load();
				contentStatus = "Cached guide loaded.";
			}
			catch (IOException e)
			{
				contentStatus = e.getMessage();
			}
			render();
			if (guide.isStale(System.currentTimeMillis()))
			{
				refresh();
			}
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
			SwingUtilities.invokeLater(() ->
			{
				if (valid(token) && panel != null)
				{
					panel.setStepSprite(kind, image);
				}
			});
		}));
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
		SwingUtilities.invokeLater(() ->
		{
			if (old != null)
			{
				toolbar.removeNavigation(old);
			}
		});
		navigation = null;
		panel = null;
	}

	private boolean valid(long token)
	{
		return running && generation.get() == token;
	}

	private boolean work(Runnable action)
	{
		long token = generation.get();
		if (!running)
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
			show("Work queue is busy. Retry shortly.");
			return false;
		}
	}

	private void show(String value)
	{
		long token = generation.get();
		SwingUtilities.invokeLater(() ->
		{
			if (valid(token) && panel != null)
			{
				panel.message(value);
			}
		});
	}

	private void render()
	{
		if (!running || !renderQueued.compareAndSet(false, true))
		{
			return;
		}
		long token = generation.get();
		SwingUtilities.invokeLater(() ->
		{
			renderQueued.set(false);
			if (!valid(token) || panel == null)
			{
				return;
			}
			AccountProgress account = progress.current();
			panel.render(guide.current(), account, contentStatus, progressStatus, ready);
		});
	}

	@Override
	public void refresh()
	{
		if (!running)
		{
			return;
		}
		long token = generation.get();
		contentStatus = "Checking wiki…";
		render();
		try
		{
			guide.refresh(network, System.currentTimeMillis()).whenComplete((snapshot, failure) ->
			{
				if (!valid(token))
				{
					return;
				}
				contentStatus = failure == null ? "Guide is up to date." : "Refresh failed: " + cause(failure);
				render();
			});
		}
		catch (RejectedExecutionException e)
		{
			contentStatus = "Request queue is busy. Retry shortly.";
			render();
		}
	}

	@Override
	public void syncPlayer()
	{
		if (!running)
		{
			return;
		}
		long life = generation.get(), accountToken = liveGeneration.get();
		AccountProgress expected = progress.current();
		clientThread.invokeLater(() ->
		{
			if (!valid(life) || accountToken != liveGeneration.get())
			{
				return;
			}
			if (!ready || expected == null || client.getGameState() != GameState.LOGGED_IN)
			{
				show("Log in and wait for your character to be ready before syncing.");
				return;
			}
			captureProgress(true, expected.getScope());
		});
	}

	@Override
	public void manual(String scope, GuideRow row, boolean checked)
	{
		work(() ->
		{
			try
			{
				progress.toggleManual(scope, row, checked);
			}
			catch (IOException e)
			{
				show(e.getMessage());
			}
			render();
		});
	}

	@Override
	public void training(String skill)
	{
		String url = training.resolve(skill);
		long token = generation.get();
		show("Checking Theoatrix destination…");
		try
		{
			network.execute(() ->
			{
				try
				{
					String verified = training.verifyDestination(url);
					SwingUtilities.invokeLater(() ->
					{
						if (valid(token))
						{
							show(browser.open(verified));
						}
					});
				}
				catch (IOException | RuntimeException e)
				{
					if (valid(token))
					{
						show(e.getMessage() + " Link: " + url
							+ " · Click the training action to retry. Browse Theoatrix guides: "
							+ TrainingGuideResolver.FALLBACK);
					}
				}
			});
		}
		catch (RejectedExecutionException e)
		{
			show("Request queue is busy. Retry shortly.");
		}
	}

	@Override
	public void source(String url)
	{
		if (GuideParser.safeWikiLink(url) != null || "https://creativecommons.org/licenses/by-nc-sa/3.0/".equals(url))
		{
			show(browser.open(url));
		}
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
			progressStatus = "";
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
		captureProgress(false, null);
	}

	/** Called on ClientThread. Only the immutable observation crosses to the disk worker. */
	private void captureProgress(boolean explicit, String expectedScope)
	{
		if (!livePending.compareAndSet(false, true))
		{
			if (explicit)
			{
				show("A character sync is already running. Retry shortly.");
			}
			return;
		}
		dirty = false;
		long life = generation.get(), accountToken = liveGeneration.get(), session = progress.token();
		try
		{
			AccountProgress observation = liveReader.read();
			if (observation == null || (expectedScope != null && !expectedScope.equals(observation.getScope())))
			{
				dirty = true;
				livePending.set(false);
				if (explicit)
				{
					show("Character changed or is not ready. Wait for the next game tick and retry.");
				}
				return;
			}
			if (explicit)
			{
				progressStatus = "Syncing character…";
				render();
			}
			if (!work(() ->
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
						show("");
					}
					if (explicit || newAccount)
					{
						progressStatus = "Character synced locally";
					}
					render();
				}
				catch (IOException e)
				{
					if (valid(life) && accountToken == liveGeneration.get())
					{
						dirty = true;
						progressStatus = "Could not save character progress";
						show(e.getMessage());
						render();
					}
				}
				finally
				{
					if (valid(life))
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
			show("Character progress is not ready yet. Retry after the next game tick.");
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
