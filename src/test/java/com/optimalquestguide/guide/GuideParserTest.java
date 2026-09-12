package com.optimalquestguide.guide;

import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GuideParserTest
{
    public static String fixture() throws Exception
    {
        try (java.io.InputStream in = GuideParserTest.class.getResourceAsStream("/fixtures/wiki-guide-15336101.html"))
        { return new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8); }
    }
    @Test public void capturedWikiRetainsEveryRowAndAllFields() throws Exception
    {
        String source = fixture();
        List<GuideRow> rows = new GuideParser().parse(source);
        assertEquals(source.split("<tr[ >]", -1).length - 2, rows.size());
        assertEquals("Learning the Ropes", rows.get(0).getTitle());
        assertTrue(rows.stream().anyMatch(r -> r.getTitle().startsWith("Hand in") && r.isManual()));
        assertTrue(rows.stream().anyMatch(r -> r.getKind() == GuideRow.Kind.TRAINING && r.getTargets().containsKey("FIREMAKING")));
        assertTrue(rows.stream().allMatch(r -> r.getFields().size() == 7));
        System.out.println("Captured guide: " + rows.size() + " rows; kinds " + rows.stream().collect(java.util.stream.Collectors.groupingBy(GuideRow::getKind, java.util.stream.Collectors.counting())));
    }
    public static String table(String rows)
    {
        return "<table><caption>Old School RuneScape Quest Guide</caption><tr><th>Quest/Activity</th><th>Quick Guide</th><th>New levels after quest</th><th>Quest points</th><th>Total QP</th><th>Additional info</th><th>Location</th></tr>" + rows + "</table>";
    }
    public static String activity(String text) { return "<tr><th colspan='7'>" + text + "</th></tr>"; }
    @Test public void stagesUnknownRowsAndCompoundTargetsSurviveReordering() throws Exception
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
        assertEquals(2, duplicates.size()); assertNotEquals(duplicates.get(0).getKey(), duplicates.get(1).getKey());
        assertFalse("Indistinguishable checks must not transfer", duplicates.get(0).isManual());
    }
    @Test public void rowspansAndUnsafeMarkup() throws Exception
    {
        String row = "<tr><td rowspan='2'>Unlock: boat</td><td>N/A</td><td>1</td><td>0</td><td>1</td><td><script>alert(1)</script><a href='javascript:alert(2)'>unsafe</a>Notes</td><td>Port</td></tr>";
        String second = "<tr><td>N/A</td><td>2</td><td>0</td><td>2</td><td>Second visit</td><td>Port</td></tr>";
        List<GuideRow> rows = new GuideParser().parse(table(row + second));
        assertEquals(2, rows.size()); assertFalse(rows.get(0).getFields().toString().contains("alert(1)"));
        assertTrue(rows.get(0).getLinks().isEmpty()); assertNull(GuideParser.safeWikiLink("https://oldschool.runescape.wiki.evil.test/w/X"));
    }
    @Test public void missingAmbiguousOrMalformedTablesFail() throws Exception
    {
        String valid = table(activity("Unknown new row"));
        for (String input : Arrays.asList("<p>No table</p>", valid + valid, valid.replace("colspan='7'", "colspan='2'"), valid.replace("Quest points", "Changed header")))
        {
            try { new GuideParser().parse(input); fail("Expected invalid table to fail"); } catch (java.io.IOException expected) { }
        }
    }
}
