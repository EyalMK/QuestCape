package com.questcape.progress.tracking;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.persistence.*;
import com.questcape.persistence.JsonStore;
import com.questcape.persistence.StoredView;
import com.questcape.progress.model.AccountProgress;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ManualIntentRecoveryTest
{
	private static GuideRow kudos(String title) throws Exception
	{
		return new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity(title))).get(0);
	}

	@Test
	public void recoveredSavedCheckCannotResurrectClearedIntentAndUnrelatedChecksSurvive() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		GuideRow first = kudos("Unlock: Varrock Museum Kudos"), other = kudos("Unlock: More Varrock Museum Kudos");
		AccountProgress observation = ProgressTest.observation("fixture:A", "Fixture", "STANDARD", Map.of(), Map.of());
		AccountProgress saved = observation.withManual(Set.of(first.getKey(), other.getKey(), "retired:unknown"));
		when(store.read(eq("progress"), eq(observation.getScope()), eq(AccountProgress.class)))
			.thenThrow(new IOException("unreadable")).thenReturn(saved);
		service.acceptLive(observation);
		service.toggleManual(observation.getScope(), first, false);
		service.acceptLive(observation, service.token(), true);
		assertFalse(service.current().getManual().contains(first.getKey()));
		assertTrue(service.current().getManual().containsAll(Set.of(other.getKey(), "retired:unknown")));
		assertEquals(StoredView.SaveState.SAVED, service.view().getSaveState());
	}

	@Test
	public void blockedOldReadCannotReplaceNewSessionsSavedManualIntent() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		GuideRow row = kudos("Unlock: Varrock Museum Kudos");
		AccountProgress account = ProgressTest.observation("fixture:A", "Fixture", "STANDARD", Map.of(), Map.of());
		CompletableFuture<Void> entered = new CompletableFuture<>(), release = new CompletableFuture<>();
		java.util.concurrent.atomic.AtomicInteger reads = new java.util.concurrent.atomic.AtomicInteger();
		when(store.read("progress", account.getScope(), AccountProgress.class)).thenAnswer(call ->
		{
			if (reads.getAndIncrement() == 0)
			{
				entered.complete(null);
				release.get(5, TimeUnit.SECONDS);
				return account.withManual(Set.of(row.getKey()));
			}
			return null;
		});
		ExecutorService worker = Executors.newSingleThreadExecutor();
		long oldSession = service.token();
		try
		{
			Future<Boolean> old = worker.submit(() -> service.acceptLive(account, oldSession, false));
			entered.get(5, TimeUnit.SECONDS);
			service.logout();
			service.acceptLive(account, service.token(), true);
			service.toggleManual(account.getScope(), row, false);
			release.complete(null);
			assertFalse(old.get(5, TimeUnit.SECONDS));
			service.acceptLive(account, service.token(), true);
			assertFalse(service.current().getManual().contains(row.getKey()));
			assertEquals(StoredView.SaveState.SAVED, service.view().getSaveState());
		}
		finally
		{
			release.complete(null);
			worker.shutdownNow();
		}
	}

	@Test
	public void retainedManualActionCannotAdoptANewSessionForTheSameAccount() throws Exception
	{
		ProgressService service = new ProgressService(mock(JsonStore.class));
		GuideRow row = kudos("Unlock: Varrock Museum Kudos");
		AccountProgress account = ProgressTest.observation("fixture:A", "Fixture", "STANDARD", Map.of(), Map.of());
		service.acceptLive(account);
		long old = service.token();
		service.logout();
		service.acceptLive(account);
		service.toggleManual(account.getScope(), row, true, old);
		assertFalse(service.current().getManual().contains(row.getKey()));
		service.toggleManual(account.getScope(), row, true, service.token());
		assertTrue(service.current().getManual().contains(row.getKey()));
	}

	@Test
	public void successfulOldSaveCannotAcknowledgeANewerFailedEdit() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		GuideRow row = kudos("Unlock: Varrock Museum Kudos");
		AccountProgress account = ProgressTest.observation("fixture:A", "Fixture", "STANDARD", Map.of(), Map.of());
		service.acceptLive(account);
		CompletableFuture<Void> entered = new CompletableFuture<>(), release = new CompletableFuture<>();
		java.util.concurrent.atomic.AtomicInteger writes = new java.util.concurrent.atomic.AtomicInteger();
		doAnswer(call ->
		{
			if (writes.getAndIncrement() == 0)
			{
				entered.complete(null);
				release.get(5, TimeUnit.SECONDS);
			}
			else
			{
				throw new IOException("later edit unsaved");
			}
			return null;
		}).when(store).write(anyString(), anyString(), any());
		ExecutorService worker = Executors.newSingleThreadExecutor();
		try
		{
			Future<?> old = worker.submit(() ->
			{
				try
				{
					service.toggleManual(account.getScope(), row, true);
				}
				catch (IOException e)
				{
					throw new CompletionException(e);
				}
			});
			entered.get(5, TimeUnit.SECONDS);
			service.toggleManual(account.getScope(), row, false);
			release.complete(null);
			old.get(5, TimeUnit.SECONDS);
			assertFalse(service.current().getManual().contains(row.getKey()));
			assertEquals(StoredView.SaveState.UNSAVED, service.view().getSaveState());
		}
		finally
		{
			release.complete(null);
			worker.shutdownNow();
		}
	}
}
