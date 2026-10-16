package com.questcape.integration.links;

import com.questcape.integration.lifecycle.PluginRun;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.SwingUtilities;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TrainingLinkOwnershipTest
{
	@Test
	public void accountSwitchDropsBlockedLinkSuccessAndFailureWhileRunStaysActive() throws Exception
	{
		for (boolean fail : new boolean[]
		{ false, true })
		{
			PluginRun run = new PluginRun();
			CompletableFuture<Void> otherEntered = new CompletableFuture<>(), otherRelease = new CompletableFuture<>(),
				entered = new CompletableFuture<>(), release = new CompletableFuture<>(),
				barrier = new CompletableFuture<>();
			try
			{
				run.network(() ->
				{
					otherEntered.complete(null);
					otherRelease.join();
				});
				otherEntered.get(5, TimeUnit.SECONDS);
				TrainingGuideResolver resolver = mock(TrainingGuideResolver.class);
				BrowserLinks browser = mock(BrowserLinks.class);
				String url = TrainingGuideResolver.FALLBACK;
				when(resolver.resolve("MINING")).thenReturn(url);
				when(resolver.verifyDestination(url)).thenAnswer(call ->
				{
					entered.complete(null);
					release.join();
					if (fail)
					{
						throw new IOException("Late provider failure");
					}
					return url;
				});
				AtomicLong account = new AtomicLong(1);
				List<String> messages = Collections.synchronizedList(new ArrayList<>());
				TrainingLinkController controller = new TrainingLinkController(run, resolver, browser, account::get,
					(authority, text) -> messages.add(text));
				controller.training("MINING");
				entered.get(5, TimeUnit.SECONDS);
				account.incrementAndGet();
				assertTrue(run.network(() -> barrier.complete(null)));
				release.complete(null);
				// The other worker stays occupied, so this barrier follows all controller continuations.
				barrier.get(5, TimeUnit.SECONDS);
				SwingUtilities.invokeAndWait(() ->
				{
				});
				assertTrue(run.active());
				verifyNoInteractions(browser);
				assertEquals(List.of("Checking Theoatrix destination…"), messages);
			}
			finally
			{
				release.complete(null);
				otherRelease.complete(null);
				run.close();
			}
		}
	}
}
