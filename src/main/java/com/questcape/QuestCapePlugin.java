package com.questcape;

import com.google.inject.Provides;
import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import com.questcape.ui.GuidePanel;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.*;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;

@PluginDescriptor(name = "QuestCape", description = "The ordered OSRS Wiki route with WikiSync and live progress", tags = {"quest", "questcape", "guide", "wiki"})
public class QuestCapePlugin extends Plugin implements GuidePanel.Actions
{
    @Inject private ClientThread clientThread;
    @Inject private Client client;
    @Inject private ClientToolbar toolbar;
    @Inject private ConfigManager configManager;
    @Inject private QuestCapeConfig config;
    @Inject private GuideRepository guide;
    @Inject private ProgressService progress;
    @Inject private WikiSyncProgressProvider wikiSync;
    @Inject private LiveProgressReader liveReader;
    @Inject private RuneLitePluginRegistry registry;
    @Inject private QuestHelperBridge bridge;
    @Inject private QuestHelperSearch questSearch;
    @Inject private ResumeCoordinator resume;
    @Inject private TrainingGuideResolver training;
    @Inject private BoundedHttp http;
    @Inject private BrowserLinks browser;
    private ThreadPoolExecutor worker, network;
    private GuidePanel panel;
    private NavigationButton navigation;
    private volatile boolean running;
    private final AtomicLong generation = new AtomicLong();
    private final AtomicLong liveGeneration = new AtomicLong();
    private final AtomicLong questRequest = new AtomicLong();
    private volatile String contentStatus = "", progressStatus = "";
    private boolean dirty = true, ready;
    private Future<?> lookupFuture;
    private final AtomicBoolean livePending = new AtomicBoolean();
    private final AtomicBoolean renderQueued = new AtomicBoolean();
    private volatile String syncedScope;

    @Provides QuestCapeConfig provideConfig(ConfigManager manager) { return manager.getConfig(QuestCapeConfig.class); }
    private static ThreadPoolExecutor executor(String name, int threads)
    {
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(16), r ->
        { Thread t = new Thread(r, name); t.setDaemon(true); return t; }, new ThreadPoolExecutor.AbortPolicy());
    }
    @Override protected void startUp() throws Exception
    {
        running = true; long token = generation.incrementAndGet(); ready = false; dirty = true; syncedScope = null;
        clientThread.invokeLater(() -> { if (valid(token)) resume.logout(); });
        worker = executor("questcape-state", 1); network = executor("questcape-http", 2); registry.refresh();
        java.awt.image.BufferedImage icon;
        try (InputStream stream = getClass().getResourceAsStream("/quest-route-icon.png"))
        { if (stream == null) throw new IOException("Missing navigation icon"); icon = ImageIO.read(stream); }
        final java.awt.image.BufferedImage loadedIcon = icon;
        SwingUtilities.invokeLater(() ->
        {
            if (!valid(token)) return;
            panel = new GuidePanel(this, training);
            navigation = NavigationButton.builder().tooltip("QuestCape").icon(loadedIcon).priority(6).panel(panel).build();
            toolbar.addNavigation(navigation); render();
        });
        work(() ->
        {
            try { guide.load(); contentStatus = "Cached guide loaded."; }
            catch (IOException e) { contentStatus = e.getMessage(); }
            render(); if (guide.isStale(System.currentTimeMillis())) refresh();
        });
    }
    @Override protected void shutDown()
    {
        running = false; generation.incrementAndGet(); liveGeneration.incrementAndGet();
        long stopped = generation.get();
        clientThread.invokeLater(() -> { if (!running && generation.get() == stopped) resume.logout(); });
        guide.cancel(); http.cancel(); if (lookupFuture != null) lookupFuture.cancel(true);
        if (worker != null) worker.shutdownNow(); if (network != null) network.shutdownNow();
        progress.logout(); livePending.set(false);
        NavigationButton old = navigation;
        SwingUtilities.invokeLater(() -> { if (old != null) toolbar.removeNavigation(old); });
        navigation = null; panel = null;
    }
    private boolean valid(long token) { return running && generation.get() == token; }
    private boolean work(Runnable action)
    {
        long token = generation.get();
        if (!running) return false;
        try { worker.execute(() -> { if (valid(token)) action.run(); }); return true; }
        catch (RejectedExecutionException e) { show("Work queue is busy. Retry shortly."); return false; }
    }
    private void show(String value)
    {
        long token = generation.get();
        SwingUtilities.invokeLater(() -> { if (valid(token) && panel != null) panel.message(value); });
    }
    private void render()
    {
        if (!running || !renderQueued.compareAndSet(false, true)) return;
        long token = generation.get();
        SwingUtilities.invokeLater(() ->
        {
            renderQueued.set(false);
            if (!valid(token) || panel == null) return;
            AccountProgress account = progress.current();
            panel.setQuestHelperIcon(questSearch.sidebarIcon(panel));
            panel.render(guide.current(), account, progress.remoteViewed(), contentStatus, progressStatus,
                registry.wikiSyncMessage() + "\n" + bridge.availability(),
                registry.wikiSync() == RuneLitePluginRegistry.State.ACTIVE && account != null && "STANDARD".equals(account.getMode()));
        });
    }
    @Override public void refresh()
    {
        if (!running) return;
        long token = generation.get(); contentStatus = "Checking wiki…"; render();
        try
        {
            guide.refresh(network, System.currentTimeMillis()).whenComplete((snapshot, failure) ->
            {
                if (!valid(token)) return;
                contentStatus = failure == null ? "Guide is up to date." : "Refresh failed: " + cause(failure);
                render();
            });
        }
        catch (RejectedExecutionException e) { contentStatus = "Request queue is busy. Retry shortly."; render(); }
    }
    @Override public void syncPlayer()
    {
        AccountProgress current = progress.current();
        if (current == null) { show("Log in to sync your progress."); return; }
        if (!"STANDARD".equals(current.getMode())) { show("WikiSync lookup uses STANDARD mode. Your live progress remains isolated in " + current.getMode() + "."); return; }
        final String normalized;
        try { normalized = WikiSyncProgressProvider.normalizeName(current.getUsername()); }
        catch (IllegalArgumentException e) { show(e.getMessage()); return; }
        if (registry.wikiSync() != RuneLitePluginRegistry.State.ACTIVE) { show(registry.wikiSyncMessage()); return; }
        String liveScope = current.getScope();
        long token = progress.useCurrent(), life = generation.get();
        syncedScope = liveScope;
        progressStatus = "Syncing WikiSync…"; render();
        work(() ->
        {
            try { progress.loadRemoteCurrent(); } catch (IOException e) { show(e.getMessage()); }
            render();
        });
        if (lookupFuture != null) lookupFuture.cancel(true);
        try
        {
            lookupFuture = network.submit(() ->
            {
                try
                {
                    AccountProgress data = wikiSync.lookup(normalized);
                    work(() ->
                    {
                        if (!valid(life) || registry.wikiSync() != RuneLitePluginRegistry.State.ACTIVE) return;
                        try
                        {
                            if (!progress.acceptCurrentRemote(token, liveScope, data)) return;
                            progressStatus = data.getObservedAt() == null ? "Synced · server time unknown" : System.currentTimeMillis() - data.getObservedAt() > GuideRepository.MAX_AGE
                                ? "Synced · WikiSync data is stale" : "WikiSync synced";
                        }
                        catch (IOException e) { progressStatus = e.getMessage(); }
                        render();
                    });
                }
                catch (IOException | RuntimeException e)
                {
                    if (valid(life) && progress.token() == token)
                    { progressStatus = "Sync failed · cached data retained"; show(e.getMessage()); render(); }
                }
            });
        }
        catch (RejectedExecutionException e) { progressStatus = "Request queue is busy. Retry shortly."; render(); }
    }
    @Override public void manual(String scope, GuideRow row, boolean checked)
    { work(() -> { try { progress.toggleManual(scope, row, checked); } catch (IOException e) { show(e.getMessage()); } render(); }); }
    @Override public void quest(GuideRow row)
    {
        long token = generation.get(), accountToken = liveGeneration.get();
        long request = questRequest.incrementAndGet();
        AccountProgress expected = progress.current();
        clientThread.invokeLater(() ->
        {
            if (!valid(token) || accountToken != liveGeneration.get() || request != questRequest.get()) return;
            registry.refresh();
            AccountProgress live = ready && expected != null ? liveReader.read() : null;
            if (live != null && !live.getScope().equals(expected.getScope())) return;
            QuestHelperBridge.Result result = resume.manual(configManager.getRSProfileKey(), live, row.getQuestIdentity());
            SwingUtilities.invokeLater(() ->
            {
                if (!valid(token) || accountToken != liveGeneration.get() || request != questRequest.get() || panel == null) return;
                QuestHelperBridge.Result feedback = result;
                if (live != null && (result.getState() == QuestHelperBridge.State.INCOMPATIBLE || result.getState() == QuestHelperBridge.State.UNSUPPORTED))
                {
                    try { feedback = questSearch.open(panel, row); }
                    catch (RuntimeException e) { feedback = new QuestHelperBridge.Result(QuestHelperBridge.State.FAILED, "Quest Helper search could not open. Open its sidebar tab and retry."); }
                }
                panel.questMessage(row.getKey(), feedback.getMessage());
            });
        });
    }
    @Override public void clearQuest()
    {
        long token = generation.get(), accountToken = liveGeneration.get();
        clientThread.invokeLater(() ->
        {
            if (!valid(token) || accountToken != liveGeneration.get()) return;
            AccountProgress live = ready ? liveReader.read() : null;
            if (live == null) { show("Log in before clearing the remembered quest."); return; }
            resume.clear(configManager.getRSProfileKey()); show("Remembered quest cleared for the current account.");
        });
    }
    @Override public void training(String skill)
    {
        String url = training.resolve(skill); long token = generation.get();
        show("Checking Theoatrix destination…");
        try { network.execute(() ->
        {
            try
            {
                String verified = training.verifyDestination(url);
                SwingUtilities.invokeLater(() -> { if (valid(token)) show(browser.open(verified)); });
            }
            catch (IOException | RuntimeException e) { if (valid(token)) show(e.getMessage() + " Link: " + url + " · Click the training action to retry. Browse Theoatrix guides: " + TrainingGuideResolver.FALLBACK); }
        }); } catch (RejectedExecutionException e) { show("Request queue is busy. Retry shortly."); }
    }
    @Override public void source(String url)
    {
        if (GuideParser.safeWikiLink(url) != null || "https://creativecommons.org/licenses/by-nc-sa/3.0/".equals(url)) show(browser.open(url));
    }
    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        dirty = true;
        if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
            ready = false; resume.logout(); liveGeneration.incrementAndGet(); progress.logout(); syncedScope = null; progressStatus = "";
            if (lookupFuture != null) lookupFuture.cancel(true); show(""); render();
        }
        if (event.getGameState() == GameState.LOGGED_IN) { ready = false; resume.login(); }
    }
    @Subscribe public void onVarbitChanged(VarbitChanged event) { dirty = true; }
    @Subscribe public void onStatChanged(StatChanged event) { dirty = true; }
    @Subscribe public void onGameTick(GameTick event)
    {
        ready = true;
        if ((!dirty && !resume.needsTick()) || !running || !livePending.compareAndSet(false, true)) return;
        dirty = false; long token = generation.get(), accountToken = liveGeneration.get();
        clientThread.invokeLater(() ->
        {
            if (!valid(token) || accountToken != liveGeneration.get() || client.getGameState() != GameState.LOGGED_IN) { livePending.set(false); return; }
            try
            {
                AccountProgress observation = liveReader.read();
                String resumeMessage = resume.tick(configManager.getRSProfileKey(), observation);
                if (resumeMessage != null) show(resumeMessage);
                if (observation == null) { dirty = true; livePending.set(false); return; }
                if (!work(() ->
                {
                    try
                    {
                        if (accountToken == liveGeneration.get())
                        {
                            boolean newAccount = progress.current() == null || !progress.current().getScope().equals(observation.getScope());
                            progress.acceptLive(observation); if (newAccount) show(""); render();
                            if (!observation.getScope().equals(syncedScope) && registry.wikiSync() == RuneLitePluginRegistry.State.ACTIVE && "STANDARD".equals(observation.getMode())) syncPlayer();
                        }
                    }
                    catch (IOException e) { show(e.getMessage()); }
                    finally { livePending.set(false); }
                })) { dirty = true; livePending.set(false); }
            }
            catch (RuntimeException e) { dirty = true; livePending.set(false); show("Live progress is not ready yet."); }
        });
    }
    private void dependenciesChanged()
    {
        registry.refresh(); render();
        if (registry.wikiSync() != RuneLitePluginRegistry.State.ACTIVE)
        {
            if (lookupFuture != null) lookupFuture.cancel(true);
            syncedScope = null; progress.useCurrent(); return;
        }
        AccountProgress current = progress.current();
        if (current != null && !current.getScope().equals(syncedScope) && registry.wikiSync() == RuneLitePluginRegistry.State.ACTIVE) syncPlayer();
    }
    @Subscribe public void onPluginChanged(PluginChanged event) { dependenciesChanged(); }
    @Subscribe public void onExternalPluginsChanged(ExternalPluginsChanged event) { dependenciesChanged(); }
    @Subscribe public void onConfigChanged(ConfigChanged event)
    {
        if (QuestCapeConfig.GROUP.equals(event.getGroup()) && "resumeQuestOnLogin".equals(event.getKey()))
        {
            long token = generation.get();
            clientThread.invokeLater(() -> { if (valid(token)) resume.preferenceChanged(); });
            show(config.resumeQuestOnLogin() ? "Resume preference saved for the next login. A compatible integration is required." : "Automatic quest resume is off. Manual selection remains available.");
        }
    }
    private static String cause(Throwable failure)
    { while (failure.getCause() != null) failure = failure.getCause(); return failure.getMessage(); }
}
