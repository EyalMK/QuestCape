package com.questcape;

import com.questcape.guide.classification.GuideAssembler;
import com.questcape.guide.model.GuideRow;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.guide.repository.GuideRepository;
import com.questcape.integration.http.BoundedHttp;
import com.questcape.integration.links.BrowserLinks;
import com.questcape.integration.links.TrainingGuideResolver;
import com.questcape.progress.capture.LiveProgressReader;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressService;
import com.questcape.progress.tracking.ProgressTest;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyleConstants;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.junit.*;
import org.mockito.*;

import static org.mockito.Mockito.*;

public class PluginLifecycleTest
{
	@Mock
	private ClientThread clientThread;
	@Mock
	private Client client;
	@Mock
	private ClientToolbar toolbar;
	@Mock
	private GuideRepository guide;
	@Mock
	private ProgressService progress;
	@Mock
	private LiveProgressReader liveReader;
	@Mock
	private TrainingGuideResolver training;
	@Mock
	private BoundedHttp http;
	@Mock
	private BrowserLinks browser;
	@Mock
	private SpriteManager spriteManager;
	@Mock
	private SkillIconManager skillIconManager;
	@InjectMocks
	private QuestCapePlugin plugin;
	private AutoCloseable mocks;
	private final Queue<Runnable> clientQueue = new ConcurrentLinkedQueue<>();
	private boolean started;
	private AccountProgress account;
	private final AtomicReference<AccountProgress> currentAccount = new AtomicReference<>();

	@Before
	public void start() throws Exception
	{
		mocks = MockitoAnnotations.openMocks(this);
		doAnswer(i ->
		{
			clientQueue.add(i.getArgument(0));
			return null;
		}).when(clientThread).invokeLater(any(Runnable.class));
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		account = ProgressTest.observation("live:rsprofile.a:STANDARD", "Player", "STANDARD", Map.of(), Map.of());
		when(liveReader.read()).thenReturn(account);
		currentAccount.set(account);
		when(progress.current()).thenAnswer(i -> currentAccount.get());
		when(progress.view()).thenAnswer(i -> new com.questcape.persistence.StoredView<>(currentAccount.get(), com.questcape.persistence.StoredView.SaveState.SAVED));
		when(guide.view()).thenAnswer(i -> new com.questcape.persistence.StoredView<>(guide.current(), com.questcape.persistence.StoredView.SaveState.SAVED));
		when(progress.acceptLive(any(), anyLong(), anyBoolean())).thenReturn(true);
		SkillIconManager bundledIcons = new SkillIconManager();
		for (Skill skill : List.of(Skill.COOKING, Skill.HERBLORE))
		{
			when(skillIconManager.getSkillImage(skill, false)).thenReturn(bundledIcons.getSkillImage(skill, false));
		}
		plugin.startUp();
		started = true;
		verify(toolbar, timeout(2000)).addNavigation(any());
		drainClient();
		SwingUtilities.invokeAndWait(() ->
		{
		});
	}

	@After
	public void stop() throws Exception
	{
		if (started)
		{
			plugin.shutDown();
		}
		drainClient();
		SwingUtilities.invokeAndWait(() ->
		{
		});
		mocks.close();
	}

	private void drainClient()
	{
		Runnable next;
		while ((next = clientQueue.poll()) != null)
		{
			next.run();
		}
	}

	@Test
	public void oldStartupCompletionCannotRefreshIntoARestartedRun() throws Exception
	{
		firstTickAndWorkerBarrier();
		plugin.shutDown();
		started = false;
		clearInvocations(guide, toolbar);
		java.util.concurrent.CompletableFuture<Void> entered = new java.util.concurrent.CompletableFuture<>(),
			release = new java.util.concurrent.CompletableFuture<>();
		AtomicReference<Thread> oldWorker = new AtomicReference<>();
		java.util.concurrent.atomic.AtomicInteger loads = new java.util.concurrent.atomic.AtomicInteger();
		when(guide.load(anyLong())).thenAnswer(call ->
		{
			if (loads.getAndIncrement()==0)
			{
				oldWorker.set(Thread.currentThread()); entered.complete(null);
				long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
				boolean complete=false;
				while (!complete)
				{
					try { release.get(Math.max(1,deadline-System.nanoTime()),java.util.concurrent.TimeUnit.NANOSECONDS); complete=true; }
					catch (InterruptedException ignored) { /* Simulate an IO provider finishing after cancellation. */ }
				}
			}
			return null;
		});
		try
		{
			plugin.startUp();
			started = true;
			entered.get(5, java.util.concurrent.TimeUnit.SECONDS);
			plugin.shutDown();
			started = false;
			plugin.startUp();
			started = true;
			verify(guide, timeout(2000).times(2)).load(anyLong());
			release.complete(null);
			oldWorker.get().join(5000);
			org.junit.Assert.assertFalse(oldWorker.get().isAlive());
			plugin.manual(account.getScope(), row(), false);
			verify(progress, timeout(2000).atLeast(2)).toggleManual(eq(account.getScope()), any(), eq(false),
				anyLong());
			verify(guide, times(1)).isStale(anyLong());
			verify(guide, never()).refresh(any(), anyLong(), anyLong());
		}
		finally { release.complete(null); }
	}

	private GuideRow row() throws Exception
	{
		return new GuideParser()
			.parse(
				GuideParserTest.table(GuideParserTest.activity("<a href='/w/Cook%27s_Assistant'>Cook's Assistant</a>")))
			.get(0);
	}

	@Test
	public void retainedOldImageCallbackCannotChangeRestartedSidebar() throws Exception
	{
		ArgumentCaptor<java.util.function.Consumer<java.awt.image.BufferedImage>> callback =
			ArgumentCaptor.forClass(java.util.function.Consumer.class);
		verify(spriteManager).getSpriteAsync(eq(net.runelite.api.gameval.SpriteID.AchievementDiaryIcons.BLUE_QUESTS),
			eq(0), callback.capture());
		plugin.shutDown(); started = false;
		clearInvocations(toolbar, progress);
		plugin.startUp(); started = true;
		ArgumentCaptor<NavigationButton> navigation = ArgumentCaptor.forClass(NavigationButton.class);
		verify(toolbar, timeout(2000)).addNavigation(navigation.capture());
		com.questcape.ui.panel.GuidePanel sidebar = (com.questcape.ui.panel.GuidePanel)navigation.getValue().getPanel();
		GuideRow quest = row();
		GuideSnapshot route = new GuideSnapshot("image-fixture", 1, 1, null, null, List.of(quest));
		when(guide.current()).thenReturn(route);
		AtomicReference<javax.swing.Icon> original = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() ->
		{
			sidebar.render(route, account, "", "", true);
			original.set(questIcon(sidebar).getIcon());
		});
		callback.getValue().accept(new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB));
		// A current run's serial-worker action is a barrier after any incorrectly adopted image task.
		plugin.manual(account.getScope(), quest, false);
		verify(progress, timeout(2000)).toggleManual(eq(account.getScope()), any(), eq(false), anyLong());
		SwingUtilities.invokeAndWait(() -> org.junit.Assert.assertSame(original.get(), questIcon(sidebar).getIcon()));
	}

	private static javax.swing.JLabel questIcon(java.awt.Container parent)
	{
		for (java.awt.Component child : parent.getComponents())
		{
			if (child instanceof javax.swing.JLabel && "Quest".equals(((javax.swing.JLabel)child).getToolTipText()))
			{
				return (javax.swing.JLabel)child;
			}
			if (child instanceof java.awt.Container)
			{
				javax.swing.JLabel found = questIcon((java.awt.Container)child);
				if (found != null) { return found; }
			}
		}
		return null;
	}

	@Test
	public void retainedOldSidebarActionCannotOpenBrowserAfterRestart() throws Exception
	{
		ArgumentCaptor<NavigationButton> navigation = ArgumentCaptor.forClass(NavigationButton.class);
		verify(toolbar).addNavigation(navigation.capture());
		javax.swing.JButton staleReport = reportButton((java.awt.Container)navigation.getValue().getPanel());
		org.junit.Assert.assertNotNull(staleReport);
		plugin.shutDown();
		started = false;
		clearInvocations(toolbar, browser);
		plugin.startUp();
		started = true;
		verify(toolbar, timeout(2000)).addNavigation(any());
		// Synthetic Swing event on the isolated fixture, never input to RuneScape.
		SwingUtilities.invokeAndWait(() -> staleReport.doClick(0));
		verifyNoInteractions(browser);
	}

	private static javax.swing.JButton reportButton(java.awt.Container parent)
	{
		for (java.awt.Component child : parent.getComponents())
		{
			if (child instanceof javax.swing.JButton
				&& "Report issues or contribute".equals(((javax.swing.JButton)child).getToolTipText()))
			{
				return (javax.swing.JButton)child;
			}
			if (child instanceof java.awt.Container)
			{
				javax.swing.JButton found = reportButton((java.awt.Container)child);
				if (found != null) { return found; }
			}
		}
		return null;
	}

	private void firstTickAndWorkerBarrier() throws Exception
	{
		plugin.onGameTick(null);
		drainClient();
		plugin.manual(account.getScope(), row(), false);
		verify(progress, timeout(2000)).toggleManual(eq(account.getScope()), any(), eq(false), anyLong());
	}

	@Test
	public void realSkillImagesAreReadyBeforeSidebarPublication() throws Exception
	{
		InOrder order = inOrder(skillIconManager, toolbar);
		order.verify(skillIconManager).getSkillImage(Skill.HERBLORE, false);
		ArgumentCaptor<NavigationButton> navigation = ArgumentCaptor.forClass(NavigationButton.class);
		order.verify(toolbar).addNavigation(navigation.capture());
		SwingUtilities.invokeAndWait(() ->
		{
			com.questcape.ui.panel.GuidePanel sidebar = (com.questcape.ui.panel.GuidePanel)navigation.getValue().getPanel();
			GuideRow row = GuideAssembler.row("skills", 0, GuideRow.Kind.ACTIVITY, "Synthetic skill note", "", null,
				Map.of(), Map.of("Additional info", "Herblore and Cooking"), Map.of());
			sidebar.render(new GuideSnapshot("skills", 1, 1, null, null, List.of(row)), account, "", "", true);
			JTextPane note = findSkillNote(sidebar);
			org.junit.Assert.assertNotNull(note);
			for (Skill skill : List.of(Skill.HERBLORE, Skill.COOKING))
			{
				int position = note.getText().indexOf(skill.getName()) - 2;
				javax.swing.Icon icon = StyleConstants.getIcon(note.getStyledDocument().getCharacterElement(position).getAttributes());
				org.junit.Assert.assertTrue(icon instanceof ImageIcon);
				java.awt.image.BufferedImage actual = (java.awt.image.BufferedImage)((ImageIcon)icon).getImage();
				java.awt.image.BufferedImage expected = net.runelite.client.util.ImageUtil.resizeImage(
					new SkillIconManager().getSkillImage(skill, false), 16, 16);
				org.junit.Assert.assertArrayEquals(expected.getRGB(0, 0, 16, 16, null, 0, 16),
					actual.getRGB(0, 0, 16, 16, null, 0, 16));
			}
		});
	}

	private static JTextPane findSkillNote(java.awt.Container parent)
	{
		for (java.awt.Component child : parent.getComponents())
		{
			if (child instanceof JTextPane)
			{
				return (JTextPane)child;
			}
			if (child instanceof java.awt.Container)
			{
				JTextPane found = findSkillNote((java.awt.Container)child);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	@Test
	public void explicitSyncUsesClientQueueForEveryModeAndNeverUsesHttp() throws Exception
	{
		firstTickAndWorkerBarrier();
		AccountProgress deadman = ProgressTest.observation("live:rsprofile.a:DEADMAN", "Player", "DEADMAN", Map.of(),
			Map.of());
		currentAccount.set(deadman);
		when(liveReader.read()).thenReturn(deadman);
		clearInvocations(liveReader);
		plugin.syncPlayer();
		verifyNoInteractions(liveReader);
		drainClient();
		verify(progress, timeout(2000)).acceptLive(eq(deadman), anyLong(), eq(true));
		verifyNoInteractions(http);
	}

	@Test
	public void queuedSyncIsDiscardedAfterLogoutHopOrShutdown() throws Exception
	{
		firstTickAndWorkerBarrier();
		for (GameState state : List.of(GameState.LOGIN_SCREEN, GameState.HOPPING, GameState.CONNECTION_LOST))
		{
			plugin.syncPlayer();
			GameStateChanged event = new GameStateChanged();
			event.setGameState(state);
			plugin.onGameStateChanged(event);
			drainClient();
		}
		plugin.syncPlayer();
		plugin.shutDown();
		started = false;
		drainClient();
		verify(progress, never()).acceptLive(any(), anyLong(), eq(true));
	}

	@Test
	public void syncWaitsForReadinessAndRejectsChangedProfiles() throws Exception
	{
		plugin.syncPlayer();
		drainClient();
		verifyNoInteractions(liveReader);
		firstTickAndWorkerBarrier();
		when(liveReader.read())
			.thenReturn(ProgressTest.observation("live:rsprofile.b:STANDARD", "Bob", "STANDARD", Map.of(), Map.of()));
		plugin.syncPlayer();
		drainClient();
		verify(progress, never()).acceptLive(any(), anyLong(), eq(true));
	}

	@Test
	public void statAndVarbitEventsRefreshWhileIdleTicksDoNot() throws Exception
	{
		firstTickAndWorkerBarrier();
		clearInvocations(liveReader);
		plugin.onGameTick(null);
		verifyNoInteractions(liveReader);
		plugin.onStatChanged(null);
		plugin.onGameTick(null);
		plugin.manual(account.getScope(), row(), true);
		verify(progress, timeout(2000)).toggleManual(eq(account.getScope()), any(), eq(true), anyLong());
		verify(liveReader).read();
		plugin.onVarbitChanged(null);
		plugin.onGameTick(null);
		verify(liveReader, times(2)).read();
	}

	@Test
	public void shutdownCancelsRequestsAndRemovesOwnNavigation() throws Exception
	{
		plugin.shutDown();
		started = false;
		SwingUtilities.invokeAndWait(() ->
		{
		});
		verify(http).cancel();
		verify(guide).cancel();
		verify(toolbar).removeNavigation(any());
	}

	@Test
	public void failedBackgroundStartupReleasesPartialRunWithoutPublishingSidebar() throws Exception
	{
		plugin.shutDown();
		started = false;
		SwingUtilities.invokeAndWait(() ->
		{
		});
		clearInvocations(toolbar, http, guide);
		when(skillIconManager.getSkillImage(any(), eq(false)))
			.thenThrow(new IllegalStateException("asset provider failed"));
		plugin.startUp();
		started = true;
		verify(http, timeout(2000)).cancel();
		verify(guide, timeout(2000)).cancel();
		SwingUtilities.invokeAndWait(() ->
		{
		});
		verify(toolbar, never()).addNavigation(any());
		plugin.shutDown();
		started = false;
		verify(http, times(1)).cancel();
	}
}
