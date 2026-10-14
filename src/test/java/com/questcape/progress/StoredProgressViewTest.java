package com.questcape.progress;

import com.questcape.persistence.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StoredProgressViewTest
{
	@Test
	public void observationAndPendingSaveArePairedAndOldCompletionCannotRestoreAfterLogout() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		CompletableFuture<Void> entered = new CompletableFuture<>(), release = new CompletableFuture<>();
		doAnswer(call ->
		{
			entered.complete(null);
			release.get(5, TimeUnit.SECONDS);
			return null;
		}).when(store).write(anyString(), anyString(), any());
		ExecutorService worker = Executors.newSingleThreadExecutor();
		try
		{
			AccountProgress account = ProgressTest.observation("fixture:A", "Fixture", "STANDARD", Map.of(),
				Map.of("MINING", 54));
			long session = service.token();
			Future<Boolean> result = worker.submit(() -> service.acceptLive(account, session, true));
			entered.get(5, TimeUnit.SECONDS);
			StoredView<AccountProgress> pending = service.view();
			assertEquals(account.getScope(), pending.getData().getScope());
			assertEquals(StoredView.SaveState.PENDING, pending.getSaveState());
			service.logout();
			assertNull(service.view().getData());
			assertEquals(StoredView.SaveState.NONE, service.view().getSaveState());
			release.complete(null);
			assertFalse(result.get(5, TimeUnit.SECONDS));
			assertNull(service.view().getData());
			assertEquals(StoredView.SaveState.PENDING, pending.getSaveState());
		}
		finally
		{
			release.complete(null);
			worker.shutdownNow();
		}
	}

	@Test
	public void failedSaveAndForcedRecoveryDescribeTheirOwnObservation() throws Exception
	{
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		doThrow(new IOException("fixture denied")).doNothing().when(store).write(anyString(), anyString(), any());
		AccountProgress account = ProgressTest.observation("fixture:A", "Fixture", "STANDARD", Map.of(),
			Map.of("MINING", 54));
		service.acceptLive(account, service.token(), true);
		StoredView<AccountProgress> failed = service.view();
		assertEquals(StoredView.SaveState.UNSAVED, failed.getSaveState());
		assertEquals(Integer.valueOf(54), failed.getData().getLevels().get("MINING"));
		service.acceptLive(account, service.token(), true);
		assertEquals(StoredView.SaveState.SAVED, service.view().getSaveState());
		assertEquals(StoredView.SaveState.UNSAVED, failed.getSaveState());
	}
}
