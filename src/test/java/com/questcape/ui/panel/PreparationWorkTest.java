package com.questcape.ui.panel;

import com.questcape.guide.model.ClassifiedAction;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressTest;
import java.util.*;
import org.junit.Test;

import static org.junit.Assert.*;

/** Informational isolated timings, never a flaky wall-clock assertion or in-game performance claim. */
public class PreparationWorkTest
{
	@Test
	public void preparedRouteSupportsRepeatedPureProjectionWithoutReclassification() throws Exception
	{
		String fixture = GuideParserTest.fixture();
		long start = System.nanoTime();
		GuideSnapshot route = new GuideSnapshot("fixture", 1, 1, null, null, new GuideParser().parse(fixture));
		long prepared = System.nanoTime();
		AccountProgress account = ProgressTest.observation("synthetic:timing", "Fixture", "STANDARD", Map.of(),
			Map.of("PRAYER", 50, "FARMING", 50));
		ClassifiedAction original = route.getRows().get(0).action();
		int repeats = 20;
		for (int index = 0; index < repeats; index++)
		{
			GuideProjection projection = GuideProjection.of(route, account, null, null);
			assertEquals(349, projection.getEntries().size());
			assertSame(original, route.getRows().get(0).action());
		}
		long end = System.nanoTime();
		System.out.printf(java.util.Locale.ROOT,
			"Prepared 351-row fixture: %.2f ms; mean pure projection: %.2f ms (%d runs). No game/runtime claim.%n",
			(prepared - start) / 1_000_000.0, (end - prepared) / 1_000_000.0 / repeats, repeats);
	}
}
