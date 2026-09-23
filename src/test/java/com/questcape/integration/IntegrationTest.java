package com.questcape.integration;

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
	public void browserFailureOffersRecoveryAndCannotChangeProgress() throws Exception
	{
		com.questcape.progress.AccountProgress account = com.questcape.progress.ProgressTest.observation("live:A",
			"Alice", "STANDARD", Map.of(), Map.of("FIREMAKING", 1));
		com.questcape.guide.GuideRow row = new com.questcape.guide.GuideParser()
			.parse(com.questcape.guide.GuideParserTest.table(
				com.questcape.guide.GuideParserTest.activity("Train Firemaking from level 1 to level 40")))
			.get(0);
		BrowserLinks links = new BrowserLinks(url ->
		{
			throw new IllegalStateException("No browser");
		});
		String url = new TrainingGuideResolver(null).resolve("Firemaking");
		assertTrue(links.open(url).contains("copy this link: " + url));
		assertEquals(com.questcape.progress.Completion.State.INCOMPLETE,
			com.questcape.progress.Completion.of(row, account).getState());
	}
}
