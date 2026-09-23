package com.questcape;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ClientToolbar;
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
	private ConfigManager configManager;
	@Mock
	private QuestCapeConfig config;
	@Mock
	private GuideRepository guide;
	@Mock
	private ProgressService progress;
	@Mock
	private LiveProgressReader liveReader;
	@Mock
	private RuneLitePluginRegistry registry;
	@Mock
	private QuestHelperBridge bridge;
	@Mock
	private QuestHelperSearch questSearch;
	@Mock
	private ResumeCoordinator resume;
	@Mock
	private TrainingGuideResolver training;
	@Mock
	private BoundedHttp http;
	@Mock
	private BrowserLinks browser;
	@Mock
	private SpriteManager spriteManager;
	@InjectMocks
	private QuestCapePlugin plugin;
	private AutoCloseable mocks;
	private final Queue<Runnable> clientQueue = new ConcurrentLinkedQueue<>();
	private boolean started;
	private AccountProgress account;

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
		when(bridge.availability()).thenReturn("Quest Helper test dependency");
		account = ProgressTest.observation("live:rsprofile.a:STANDARD", "Player", "STANDARD", Map.of(), Map.of());
		when(configManager.getRSProfileKey()).thenReturn("rsprofile.a");
		when(liveReader.read()).thenReturn(account);
		when(progress.current()).thenReturn(account);
		when(progress.acceptLive(any(), anyLong(), anyBoolean())).thenReturn(true);
		plugin.startUp();
		started = true;
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

	private GuideRow row() throws Exception
	{
		return new GuideParser()
			.parse(
				GuideParserTest.table(GuideParserTest.activity("<a href='/w/Cook%27s_Assistant'>Cook's Assistant</a>")))
			.get(0);
	}

	@Test
	public void queuedQuestClickCannotCrossLogoutAndAccountSwitch() throws Exception
	{
		plugin.quest(row());
		GameStateChanged logout = new GameStateChanged();
		logout.setGameState(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(logout);
		when(configManager.getRSProfileKey()).thenReturn("rsprofile.b");
		drainClient();
		verify(resume, never()).manual(any(), any(), any());
		verify(progress).logout();
	}

	@Test
	public void resumeReadinessContinuesOnFreshGameTicksWithoutRequiringAProgressChange() throws Exception
	{
		when(resume.needsTick()).thenReturn(true);
		for (int i = 1; i <= 3; i++)
		{
			plugin.onGameTick(null);
			drainClient();
			verify(progress, timeout(2000).times(i)).acceptLive(eq(account), anyLong(), eq(false));
			// A barrier ensures the worker's finally block releases its pending-read flag.
			plugin.manual(account.getScope(), row(), false);
			verify(progress, timeout(2000).times(i)).toggleManual(eq(account.getScope()), any(), eq(false));
		}
		verify(resume, times(3)).tick("rsprofile.a", account);
	}

	private void firstTickAndWorkerBarrier() throws Exception
	{
		plugin.onGameTick(null);
		drainClient();
		plugin.manual(account.getScope(), row(), false);
		verify(progress, timeout(2000)).toggleManual(eq(account.getScope()), any(), eq(false));
	}

	@Test
	public void explicitSyncUsesClientQueueForEveryModeAndNeverUsesHttp() throws Exception
	{
		firstTickAndWorkerBarrier();
		AccountProgress deadman = ProgressTest.observation("live:rsprofile.a:DEADMAN", "Player", "DEADMAN", Map.of(),
			Map.of());
		when(progress.current()).thenReturn(deadman);
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
		verify(progress, timeout(2000)).toggleManual(eq(account.getScope()), any(), eq(true));
		verify(liveReader).read();
		plugin.onVarbitChanged(null);
		plugin.onGameTick(null);
		verify(liveReader, times(2)).read();
	}

	@Test
	public void onlyOwnResumePreferenceIsHandledAndShutdownInvalidatesQueuedActions() throws Exception
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup("questhelper");
		event.setKey("resumeQuestOnLogin");
		plugin.onConfigChanged(event);
		drainClient();
		verify(resume, never()).preferenceChanged();
		event.setGroup(QuestCapeConfig.GROUP);
		plugin.onConfigChanged(event);
		drainClient();
		verify(resume).preferenceChanged();
		plugin.quest(row());
		plugin.shutDown();
		started = false;
		drainClient();
		verify(resume, never()).manual(any(), any(), any());
		verify(http).cancel();
		verify(guide).cancel();
	}

	@Test
	public void latestQuestClickUsesSearchFallbackOnEdtWithoutReplayingAnOlderSelection() throws Exception
	{
		plugin.onGameTick(null);
		drainClient();
		verify(progress, timeout(2000)).acceptLive(eq(account), anyLong(), eq(false));
		when(resume.manual(any(), any(), any()))
			.thenReturn(new QuestHelperBridge.Result(QuestHelperBridge.State.INCOMPATIBLE, "Search instead"));
		when(questSearch.open(any(), any())).thenAnswer(i ->
		{
			org.junit.Assert.assertTrue(SwingUtilities.isEventDispatchThread());
			return new QuestHelperBridge.Result(QuestHelperBridge.State.SEARCH_READY,
				"Select the matching result to start.");
		});
		GuideRow first = row();
		GuideRow latest = new GuideParser()
			.parse(GuideParserTest
				.table(GuideParserTest.activity("<a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a>")))
			.get(0);
		plugin.quest(first);
		plugin.quest(latest);
		drainClient();
		SwingUtilities.invokeAndWait(() ->
		{
		});
		verify(resume, never()).manual(any(), any(), eq(first.getQuestIdentity()));
		verify(questSearch).open(any(), eq(latest));
		verify(questSearch, never()).open(any(), eq(first));
	}
}
