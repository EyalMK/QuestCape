package com.questcape.integration;

import com.questcape.progress.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RunOwnershipTest
{
	@Test
	public void blockedWorkDoesNotBlockCloseAndOwnedCleanupRunsOnce() throws Exception
	{
		PluginRun run = new PluginRun();
		AtomicInteger releases = new AtomicInteger(), queued = new AtomicInteger();
		CompletableFuture<Void> entered = new CompletableFuture<>(), unblock = new CompletableFuture<>();
		run.own(releases::incrementAndGet);
		run.work(() ->
		{
			entered.complete(null);
			try
			{
				unblock.get(5, TimeUnit.SECONDS);
			}
			catch (Exception ignored)
			{
			}
		});
		entered.get(5, TimeUnit.SECONDS);
		run.work(queued::incrementAndGet);
		try
		{
			CompletableFuture.runAsync(run::close).get(1, TimeUnit.SECONDS);
			run.close();
			assertFalse(run.active());
			assertEquals(1, releases.get());
			assertFalse(run.work(queued::incrementAndGet));
			assertEquals(0, queued.get());
			run.own(releases::incrementAndGet);
			assertEquals(2, releases.get());
		}
		finally
		{
			unblock.complete(null);
			run.close();
		}
	}

	@Test
	public void oldSessionInvalidationCannotClearTheNextAccount() throws Exception
	{
		ProgressService service = new ProgressService(mock(com.questcape.persistence.JsonStore.class));
		long old = service.token();
		assertTrue(service.logoutIfSession(old));
		AccountProgress next = ProgressTest.observation("fixture:B", "Fixture", "STANDARD", Map.of(),
			Map.of("MINING", 54));
		service.acceptLive(next);
		assertFalse(service.logoutIfSession(old));
		assertNotNull(service.current());
		assertEquals("fixture:B", service.current().getScope());
	}
}
