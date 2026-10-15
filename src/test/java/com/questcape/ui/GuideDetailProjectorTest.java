package com.questcape.ui;

import com.google.gson.*;
import com.questcape.guide.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GuideDetailProjectorTest
{
	@Test
	public void cellRolesPutAdviceFirstAndKeepDistinctDestinations() throws Exception
	{
		GuideRow row = new GuideParser()
			.parse(GuideParserTest.table("<tr><td><a href='/w/Lunar_Diplomacy'>Lunar Diplomacy</a></td>"
				+ "<td><a href='/w/Lunar_Diplomacy/Quick_guide'>QG</a></td><td>65 Magic 40 Runecraft</td><td>2</td><td>279</td>"
				+ "<td>Unlocks the Lunar spellbook. <a href='/w/Lunar_spellbook#Spells'>Spells</a><a href='/w/Lunar_spellbook#Other'>Other</a></td>"
				+ "<td><a href='/w/Lunar_Isle'>Lunar Isle</a></td></tr>"))
			.get(0);
		GuideDetailProjector detail = GuideDetailProjector.of(row);
		assertEquals("Wiki quick guide ↗", detail.getPrimary().getLabel());
		assertEquals(GuideFieldRole.ADVICE, detail.getFields().get(0).getRole());
		assertEquals(GuideFieldRole.PROJECTIONS, detail.getFields().get(1).getRole());
		assertEquals(1, detail.getLocation().size());
		assertEquals(3, detail.getRelated().size());
		assertEquals("Quest page ↗", detail.getRelated().get(0).getLabel());
		assertTrue(detail.getRelated().stream().anyMatch(link -> link.getUrl().endsWith("#Spells")));
		assertTrue(detail.getRelated().stream().anyMatch(link -> link.getUrl().endsWith("#Other")));
	}

	@Test
	public void naUnknownFieldsUnsafeAndAmbiguousLinksRemainHonest()
	{
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("Quick Guide", "N/A");
		fields.put("Unexpected future field", "Preserve this text");
		GuideRow row = com.questcape.guide.GuideAssembler.row("fixture", 0, GuideRow.Kind.MINIQUEST, "New miniquest",
			"", null, Map.of(), fields,
			Map.of("https://evil.invalid/w/Quick_guide", "QG", "https://oldschool.runescape.wiki/w/X#A", "One",
				"https://oldschool.runescape.wiki/w/X#B", "Two"));
		GuideDetailProjector detail = GuideDetailProjector.of(row);
		assertNull(detail.getPrimary());
		assertEquals(2, detail.getRaw().size());
		assertEquals(2, detail.getRelated().size());
		GuideRow ambiguous = com.questcape.guide.GuideAssembler.row("fixture", 0, GuideRow.Kind.QUEST, "A", "A", null,
			Map.of(),
			Map.of("Quick Guide", "QG"),
			Map.of("https://oldschool.runescape.wiki/w/A/Quick_guide#One", "QG",
				"https://oldschool.runescape.wiki/w/A/Quick_guide#Two", "QG"));
		assertNull(GuideDetailProjector.of(ambiguous).getPrimary());
		assertEquals(2, GuideDetailProjector.of(ambiguous).getRelated().size());
	}

	@Test
	public void additiveCacheEnrichmentIsOptionalOfflineAndLegacyReadable() throws Exception
	{
		GuideParser parser = new GuideParser();
		GuideRow current = parser
			.parse(GuideParserTest.table(GuideParserTest.activity("Train Woodcutting from level 52 to level 55")))
			.get(0);
		GuideSnapshot snapshot = new GuideSnapshot("fixture", 1, 1, null, null, List.of(current));
		Gson gson = new Gson();
		JsonObject saved = gson.toJsonTree(snapshot).getAsJsonObject();
		JsonObject stored = saved.getAsJsonArray("rows").get(0).getAsJsonObject();
		assertFalse(stored.has("instruction"));
		LegacySnapshot legacy = gson.fromJson(saved, LegacySnapshot.class);
		assertEquals(1, legacy.schema);
		assertEquals(current.getTargets(), legacy.rows.get(0).targets);
		assertEquals(current.getFields(), legacy.rows.get(0).fields);
		stored.remove("evidence");
		GuideRow adapted = parser.reclassify(gson.fromJson(saved, GuideSnapshot.class)).getRows().get(0);
		assertEquals(current.getKey(), adapted.getKey());
		assertEquals(current.training(), adapted.training());
		JsonObject unknown = new JsonObject();
		unknown.addProperty("version", 999);
		stored.add("evidence", unknown);
		assertEquals(current.training(),
			parser.reclassify(gson.fromJson(saved, GuideSnapshot.class)).getRows().get(0).training());
	}

	private static class LegacySnapshot
	{
		int schema;
		List<LegacyRow> rows;
	}

	private static class LegacyRow
	{
		String title;
		Map<String, Integer> targets;
		Map<String, String> fields;
		Map<String, String> links;
	}

	@Test
	public void contradictoryEnrichmentCannotOverrideNaAndNormalizationRetainsQueriesAndAnchors()
	{
		String quick = "https://oldschool.runescape.wiki/w/A/Quick_guide";
		GuideRow row = com.questcape.guide.GuideAssembler.row("fixture", 0, GuideRow.Kind.QUEST, "A", "A", null,
			Map.of(),
			Map.of("Quick Guide", "N/A"), Map.of(quick, "QG"),
			new RowEvidence(Map.of(GuideFieldRole.QUICK_GUIDE, Map.of(quick, "QG"))));
		assertNull(GuideDetailProjector.of(row).getPrimary());
		assertEquals(1, GuideDetailProjector.of(row).getRelated().size());
		assertEquals("https://oldschool.runescape.wiki/w/A?oldid=42#Part",
			GuideParser.safeWikiLink("HTTPS://OLDSCHOOL.RUNESCAPE.WIKI:443/w/A?oldid=42#Part"));
		assertEquals("https://oldschool.runescape.wiki?oldid=42#Part",
			GuideParser.safeWikiLink("https://oldschool.runescape.wiki?oldid=42#Part"));
	}
}
