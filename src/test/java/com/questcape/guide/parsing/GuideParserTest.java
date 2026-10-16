package com.questcape.guide.parsing;

import com.questcape.guide.classification.GuideAssembler;
import com.questcape.guide.model.GuideRow;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.source.SourceRow;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class GuideParserTest
{
	public static String fixture() throws Exception
	{
		try (java.io.InputStream in = GuideParserTest.class.getResourceAsStream("/fixtures/wiki-guide-15336101.html"))
		{
			return new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	public void balloonDestinationsSurviveFullGuideParsing() throws Exception
	{
		List<String> titles = new GuideParser().parse(fixture()).stream()
			.map(GuideRow::getTitle).filter(t -> t.startsWith("Unlock: Balloon transport system"))
			.collect(java.util.stream.Collectors.toList());
		assertEquals(List.of("Unlock: Balloon transport system to Crafting Guild",
			"Unlock: Balloon transport system to Varrock", "Unlock: Balloon transport system to Castle Wars",
			"Unlock: Balloon transport system to Grand Tree"), titles);
	}

	@Test
	public void truncatedCachedBalloonsRequireOneSafeDestination() throws Exception
	{
		GuideParser parser = new GuideParser();
		String truncated = "Unlock: Balloon transport system to";
		String balloon = "https://oldschool.runescape.wiki/w/Balloon_transport_system";
		String guild = "https://oldschool.runescape.wiki/w/Crafting_Guild";
		for (Map<String, String> links : List.of(Map.of(balloon, "Balloon transport system", guild, "Crafting Guild"),
			Map.of(guild, "Crafting Guild", "https://oldschool.runescape.wiki/w/Varrock", "Varrock"),
			Map.of("https://evil.invalid/w/Crafting_Guild", "Crafting Guild"), Map.<String, String>of()))
		{
			GuideRow old = new GuideRow("old", 0, GuideRow.Kind.UNLOCK, truncated, "Balloon transport system",
				null, Map.of(), Map.of("Quest/Activity", truncated), links);
			GuideSnapshot saved = new GuideSnapshot("old", 1, 1, null, null, List.of(old));
			GuideRow repaired = parser.reclassify(saved).getRows().get(0);
			boolean recoverable = links.containsKey(balloon);
			assertEquals(recoverable ? truncated + " Crafting Guild" : truncated, repaired.getTitle());
			assertEquals(repaired.getTitle(), repaired.getFields().get("Quest/Activity"));
			assertEquals(recoverable ? "UNLOCK:BALLOON:CRAFTING_GUILD" : null, repaired.getActivityIdentity());
			assertFalse(repaired.isManual());
			assertEquals(repaired, parser.reclassify(parser.reclassify(saved)).getRows().get(0));
		}
	}

	@Test
	public void capturedWikiRetainsEveryRowAndAllFields() throws Exception
	{
		String source = fixture();
		List<GuideRow> rows = new GuideParser().parse(source);
		assertEquals(source.split("<tr[ >]", -1).length - 2, rows.size());
		assertEquals("Learning the Ropes", rows.get(0).getTitle());
		assertEquals(2, rows.stream().filter(GuideRow::isManual).count());
		assertTrue(rows.stream().filter(GuideRow::isManual).allMatch(r -> r.getTitle().endsWith("Kudos")));
		assertTrue(rows.stream()
			.anyMatch(r -> r.getKind() == GuideRow.Kind.TRAINING && r.getTargets().containsKey("FIREMAKING")));
		assertTrue(rows.stream().allMatch(r -> r.getFields().size() == 7));
		System.out.println("Captured guide: " + rows.size() + " rows; kinds " + rows.stream().collect(
			java.util.stream.Collectors.groupingBy(GuideRow::getKind, java.util.stream.Collectors.counting())));
	}

	public static String table(String rows)
	{
		return "<table><caption>Old School RuneScape Quest Guide</caption><tr><th>Quest/Activity</th><th>Quick Guide</th><th>New levels after quest</th><th>Quest points</th><th>Total QP</th><th>Additional info</th><th>Location</th></tr>"
			+ rows + "</table>";
	}

	public static String activity(String text)
	{
		return "<tr><th colspan='7'>" + text + "</th></tr>";
	}

	@Test
	public void stagesUnknownRowsAndCompoundTargetsSurviveReordering() throws Exception
	{
		String start = activity("Start miniquest: <a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a>");
		String end = activity("<a href='/w/In_Search_of_Knowledge'>In Search of Knowledge</a> (miniquest)");
		String training = activity("Train Attack and Strength from level 10 to level 20");
		String unknown = activity("Visit the brand new celestial gate");
		GuideParser parser = new GuideParser();
		List<GuideRow> rows = parser.parse(table(start + training + unknown + end));
		assertNotEquals(rows.get(0).getKey(), rows.get(3).getKey());
		assertEquals(Map.of("ATTACK", 20, "STRENGTH", 20), rows.get(1).getTargets());
		assertEquals(GuideRow.Kind.UNKNOWN, rows.get(2).getKind());
		List<GuideRow> reordered = parser.parse(table(end + unknown + start + training));
		assertEquals(rows.get(0).getKey(), reordered.get(2).getKey());
		List<GuideRow> duplicates = parser.parse(table(activity("Unlock: gate") + activity("Unlock: gate")));
		assertEquals(2, duplicates.size());
		assertNotEquals(duplicates.get(0).getKey(), duplicates.get(1).getKey());
		assertFalse("Indistinguishable checks must not transfer", duplicates.get(0).isManual());
	}

	@Test
	public void rowspansAndUnsafeMarkup() throws Exception
	{
		String row = "<tr><td rowspan='2'>Unlock: boat</td><td>N/A</td><td>1</td><td>0</td><td>1</td><td><script>alert(1)</script><a href='javascript:alert(2)'>unsafe</a>Notes</td><td>Port</td></tr>";
		String second = "<tr><td>N/A</td><td>2</td><td>0</td><td>2</td><td>Second visit</td><td>Port</td></tr>";
		List<GuideRow> rows = new GuideParser().parse(table(row + second));
		assertEquals(2, rows.size());
		assertFalse(rows.get(0).getFields().toString().contains("alert(1)"));
		assertTrue(rows.get(0).getLinks().isEmpty());
		assertNull(GuideParser.safeWikiLink("https://oldschool.runescape.wiki.evil.test/w/X"));
	}

	@Test
	public void currentGuideClassifiesEveryStepAndKeepsNotesOutsideTheStepCount() throws Exception
	{
		List<GuideRow> rows = new GuideParser().parse(fixture());
		for (String title : Arrays.asList("Natural history quiz", "Knight Waves Training Grounds"))
		{
			GuideRow miniquest = rows.stream().filter(row -> row.getTitle().startsWith(title)).findFirst().orElseThrow();
			assertEquals(GuideRow.Kind.MINIQUEST, miniquest.getKind());
			assertFalse("Miniquests use automatic state", miniquest.isManual());
			assertNotNull(miniquest.getQuestIdentity());
		}
		GuideRow combat = rows.stream().filter(row -> row.getTitle().startsWith("Train Combat")).findFirst().orElseThrow();
		assertEquals(GuideRow.Kind.TRAINING, combat.getKind());
		assertEquals(Map.of("COMBAT", 85), combat.getTargets());
		assertEquals(2, rows.stream().filter(row -> row.getKind() == GuideRow.Kind.INFORMATION).count());
		assertEquals(349, rows.stream().filter(GuideRow::isActionable).count());
		assertTrue(rows.stream().noneMatch(row -> row.getKind() == GuideRow.Kind.UNKNOWN));
		assertEquals(GuideRow.Kind.INFORMATION, rows.get(rows.size() - 1).getKind());
	}

	@Test
	public void trainingVariantsKeepOptionalRecommendationsSeparateAndUnresolvedTargetsUnknown() throws Exception
	{
		List<GuideRow> rows = new GuideParser().parse(table(
			activity("Train Combat level to 91 (Recommended: also train Slayer)")
				+ activity("Train Attack to level 40 (Recommended: train Defence to level 60)")
				+ activity("Train Strength to 50")
				+ activity("Train Necromancy and Attack to level 20")
				+ activity("Train Slayer until ready")
				+ activity("Train Magic to level 9999999999999999999999")));
		assertTrue(rows.stream().allMatch(row -> row.getKind() == GuideRow.Kind.TRAINING));
		assertEquals(Map.of("COMBAT", 91), rows.get(0).getTargets());
		assertEquals(Map.of("ATTACK", 40), rows.get(1).getTargets());
		assertEquals(Map.of("STRENGTH", 50), rows.get(2).getTargets());
		assertTrue(rows.subList(3, 6).stream().allMatch(row -> row.getTargets().isEmpty()));
	}

	@Test
	public void commentsUseLayoutAndLanguageRatherThanSpecificTitlesOrPositions() throws Exception
	{
		List<GuideRow> rows = new GuideParser().parse(table(
			activity("Congratulations on reaching this milestone!")
				+ activity("All remaining rewards can be collected in any order")
				+ activity("Note: <a href='/w/Cooks_Assistant'>Cook's Assistant</a> has optional dialogue.")
				+ activity("A completely different cape!")
				+ activity("Visit the newly discovered gate!")
				+ activity("Mystery activity")
				+ "<tr><td>Unmapped destination!</td><td>N/A</td><td>1</td><td>0</td><td>1</td><td></td><td>Port</td></tr>"));
		assertTrue(rows.subList(0, 3).stream().noneMatch(GuideRow::isActionable));
		assertTrue(rows.subList(3, 7).stream().allMatch(row -> row.getKind() == GuideRow.Kind.UNKNOWN));
	}

	@Test(timeout = 2000)
	public void malformedTrainingCannotCauseExponentialBacktracking() throws Exception
	{
		GuideRow malformed = GuideAssembler.source(new SourceRow(0, "Train ba," + "aand".repeat(20000), "",
			Map.of(), Map.of(), null, true));
		assertEquals(GuideRow.Kind.TRAINING, malformed.getKind());
		assertTrue(malformed.getTargets().isEmpty());
		List<GuideRow> rows = new GuideParser().parse(table(
			activity("Train Attack, Strength and Defence to level 40")
				+ activity("Train Magic &amp; Ranged from level 10 to level 50")
				+ activity("Train Attack to 40 and Strength to 50")));
		assertEquals(Map.of("ATTACK", 40, "STRENGTH", 40, "DEFENCE", 40), rows.get(0).getTargets());
		assertEquals(Map.of("MAGIC", 50, "RANGED", 50), rows.get(1).getTargets());
		assertEquals(Map.of("ATTACK", 40, "STRENGTH", 50), rows.get(2).getTargets());
	}

	@Test
	public void normalizedReorderedHeadersPreserveTitleAndValidatedWikiTarget() throws Exception
	{
		String row = "<tr><td>Quick guide</td><td><a href='javascript:alert(1)'>Unsafe</a> "
			+ "<a href='/w/Cook%27s_Assistant'>Cook's Assistant</a></td><td>1</td><td>1</td>"
			+ "<td>1</td><td>Notes</td><td>Lumbridge</td></tr>";
		String source = table(row).replace("<th>Quest/Activity</th><th>Quick Guide</th>",
			"<th>Quick Guide</th><th> quest/activity </th>");
		GuideRow parsed = new GuideParser().parse(source).get(0);
		assertEquals("Unsafe Cook's Assistant", parsed.getTitle());
		assertEquals("Cook's Assistant", parsed.getWikiTarget());
		assertEquals("COOKS_ASSISTANT", parsed.getQuestIdentity());
		assertEquals(7, parsed.getFields().size());
		assertEquals(1, parsed.getLinks().size());
	}

	@Test
	public void missingAmbiguousOrMalformedTablesFail() throws Exception
	{
		String valid = table(activity("Unknown new row"));
		for (String input : Arrays.asList("<p>No table</p>", valid + valid, valid.replace("colspan='7'", "colspan='2'"),
			valid.replace("Quest points", "Changed header")))
		{
			try
			{
				new GuideParser().parse(input);
				fail("Expected invalid table to fail");
			}
			catch (java.io.IOException expected)
			{
			}
		}
	}
}
