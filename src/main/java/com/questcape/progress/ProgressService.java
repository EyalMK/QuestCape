package com.questcape.progress;

import com.questcape.guide.GuideRow;
import com.questcape.persistence.JsonStore;
import java.io.*;
import java.util.*;
import javax.inject.*;

/** All mutations run on the plugin's serial worker. View generation also gates lookup writes. */
@Singleton
public class ProgressService
{
    private final JsonStore store;
    private final Map<String, AccountProgress> records = new HashMap<>();
    private AccountProgress live;
    private long selection;
    @Inject public ProgressService(JsonStore store) { this.store = store; }
    public synchronized long useCurrent()
    { return ++selection; }
    private void load(String scope) throws IOException
    {
        if (records.containsKey(scope)) return;
        AccountProgress saved = store.read("progress", scope, AccountProgress.class);
        if (saved != null)
        {
            if (!scope.equals(saved.getScope()) || saved.getQuests() == null || saved.getLevels() == null
                || saved.getManual() == null || saved.getActivities() == null || saved.getUsername() == null || saved.getMode() == null)
                throw new IOException("Invalid account cache; original retained");
            records.put(scope, saved);
        }
    }
    public synchronized void acceptLive(AccountProgress observation) throws IOException
    {
        if (observation == null) return;
        load(observation.getScope());
        // Varbits can change every tick. Persist only changed observations, or a freshness checkpoint.
        boolean unchanged = live != null && live.getScope().equals(observation.getScope())
            && live.getUsername().equals(observation.getUsername()) && live.getQuests().equals(observation.getQuests())
            && live.getLevels().equals(observation.getLevels()) && live.getActivities().equals(observation.getActivities());
        if (!unchanged || observation.getRetrievedAt() - live.getRetrievedAt() >= 60_000) live = saveWithManual(observation);
    }
    private AccountProgress saveWithManual(AccountProgress observation) throws IOException
    {
        AccountProgress previous = records.get(observation.getScope());
        AccountProgress next = observation.withManual(previous == null ? Collections.emptySet() : previous.getManual());
        store.write("progress", next.getScope(), next);
        records.put(next.getScope(), next);
        return next;
    }
    public synchronized void logout() { live = null; selection++; }
    public synchronized AccountProgress current() { return live; }
    public synchronized AccountProgress viewed()
    {
        return live;
    }
    public synchronized AccountProgress remoteViewed()
    { return live == null || !"STANDARD".equals(live.getMode()) ? null : records.get(WikiSyncProgressProvider.scope(live.getUsername())); }
    public synchronized void loadRemoteCurrent() throws IOException
    { if (live != null && "STANDARD".equals(live.getMode())) load(WikiSyncProgressProvider.scope(live.getUsername())); }
    public synchronized boolean acceptCurrentRemote(long expectedSelection, String expectedLiveScope, AccountProgress observation) throws IOException
    {
        if (selection != expectedSelection || live == null || !live.getScope().equals(expectedLiveScope)
            || !"STANDARD".equals(live.getMode()) || !WikiSyncProgressProvider.scope(live.getUsername()).equals(observation.getScope())) return false;
        saveWithManual(observation); return true;
    }
    public synchronized long token() { return selection; }
    public synchronized void toggleManual(String expectedScope, GuideRow row, boolean checked) throws IOException
    {
        AccountProgress record = viewed();
        if (record == null || !record.getScope().equals(expectedScope) || !row.isManual()) return;
        Set<String> checks = new HashSet<>(record.getManual());
        if (checked) checks.add(row.getKey()); else checks.remove(row.getKey());
        AccountProgress changed = record.withManual(checks);
        store.write("progress", changed.getScope(), changed);
        records.put(changed.getScope(), changed);
        if (live != null && live.getScope().equals(changed.getScope())) live = changed;
    }
}
