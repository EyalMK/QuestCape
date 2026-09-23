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
		account = ProgressTest.observation("live:rsprofile.a:STANDARD", "Player", "STANDARD", Map.of(), Map.of());
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
}
