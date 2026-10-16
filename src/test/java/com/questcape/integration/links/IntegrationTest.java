package com.questcape.integration.links;

import com.questcape.integration.http.BoundedHttp;
import com.questcape.integration.lifecycle.PluginRun;
import java.io.*;
import java.util.*;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IntegrationTest
{
	@Test
	public void trainingAliasesFallbackAndRedirectsAreConstrained() throws Exception
	{
		BoundedHttp http = mock(BoundedHttp.class);
		TrainingGuideResolver resolver = new TrainingGuideResolver(http);
		resolver.prepare();
		assertEquals(resolver.resolve("Runecrafting"), resolver.resolve("Runecraft"));
		assertEquals(resolver.resolve("Strength"), resolver.resolve("Attack"));
		assertEquals(TrainingGuideResolver.FALLBACK, resolver.resolve("New Skill"));
		for (String unsafe : List.of("http://theoatrix.net/a", "https://theoatrix.net.evil.test/a",
			"https://evil@theoatrix.net/a", "javascript:alert(1)", "https://theoatrix.net:123/a"))
		{
			assertFalse(TrainingGuideResolver.safe(unsafe));
		}
		when(http.get(anyString(), anyMap(), anyInt()))
			.thenReturn(new BoundedHttp.Result(302, "", null, null, "https://evil.test/guide", null));
		try
		{
			resolver.verifyDestination(resolver.resolve("Firemaking"));
			fail();
		}
		catch (IOException expected)
		{
		}
		verify(http, times(1)).get(anyString(), anyMap(), anyInt());
		when(http.get(anyString(), anyMap(), anyInt())).thenReturn(
			new BoundedHttp.Result(302, "", null, null, "/all-guides", null),
			new BoundedHttp.Result(200, "", null, null, null, null));
		assertEquals(TrainingGuideResolver.FALLBACK, resolver.verifyDestination(resolver.resolve("Firemaking")));
	}

	@Test
	public void successfulOpenIsSilentAndExplicitCopyDoesNotOpenOrRequestAnything()
	{
		java.util.concurrent.atomic.AtomicInteger opened = new java.util.concurrent.atomic.AtomicInteger();
		java.util.concurrent.atomic.AtomicReference<String> copied = new java.util.concurrent.atomic.AtomicReference<>();
		BrowserLinks browser = new BrowserLinks(url -> opened.incrementAndGet(), copied::set);
		String url = "https://oldschool.runescape.wiki/w/Mining";
		assertEquals("", browser.open(url));
		assertEquals(1, opened.get());
		PluginRun run = new PluginRun();
		try
		{
			TrainingGuideResolver resolver = mock(TrainingGuideResolver.class);
			TrainingLinkController controller = new TrainingLinkController(run, resolver, browser, () -> 1L,
				(authority, text) -> fail("Copy must not publish a banner"));
			assertTrue(controller.copyLink(url));
			assertEquals(url, copied.get());
			assertEquals(1, opened.get());
			assertFalse(controller.copyLink("javascript:alert(1)"));
			assertFalse(controller.copyLink(null));
			run.close();
			assertFalse(controller.copyLink(url));
			verifyNoInteractions(resolver);
			BrowserLinks unavailable = new BrowserLinks(value -> fail(), value ->
			{
				throw new IllegalStateException("Clipboard busy");
			});
			assertFalse(unavailable.copy(url));
		}
		finally
		{
			run.close();
		}
	}

	@Test
	public void browserFailureOffersRecoveryAndCannotChangeProgress() throws Exception
	{
		com.questcape.progress.model.AccountProgress account = com.questcape.progress.tracking.ProgressTest.observation("live:A",
			"Alice", "STANDARD", Map.of(), Map.of("FIREMAKING", 1));
		com.questcape.guide.model.GuideRow row = new com.questcape.guide.parsing.GuideParser()
			.parse(com.questcape.guide.parsing.GuideParserTest.table(
				com.questcape.guide.parsing.GuideParserTest.activity("Train Firemaking from level 1 to level 40")))
			.get(0);
		BrowserLinks links = new BrowserLinks(url ->
		{
			throw new IllegalStateException("No browser");
		});
		String url = com.questcape.integration.links.TestTrainingLinks.local().resolve("Firemaking");
		assertTrue(links.open(url).contains("copy this link: " + url));
		assertEquals(com.questcape.progress.evaluation.Completion.State.IN_PROGRESS,
			com.questcape.progress.evaluation.Completion.of(row, account).getState());
	}
}
