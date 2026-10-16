package com.questcape.progress.evaluation;

import com.questcape.guide.interpretation.TrainingInstruction;
import com.questcape.guide.model.GuideRow;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.progress.tracking.ProgressTest;
import java.util.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class TrainingEvaluationTest
{
	@Test
	public void explainedPrayerAndFarmingMilestonesRemainTrainingInFreshAndLegacyRows() throws Exception
	{
		List<GuideRow> rows = new GuideParser().parse(GuideParserTest.fixture());
		List<GuideRow> reported = new ArrayList<>();
		for (GuideRow row : rows)
		{
			if (row.getTitle().startsWith("Train Prayer") && row.getTitle().contains("for Protect from")
				|| row.getTitle().startsWith("Train Farming") && row.getTitle().contains("letting crops grow"))
			{
				reported.add(row);
			}
		}
		assertEquals(4, reported.size());
		for (GuideRow row : reported)
		{
			GuideRow legacy = new GuideParser().reclassify(new GuideSnapshot("fixture", 1, 1, null, null,
				List.of(new GuideRow(row.getKey(), 0, row.getKind(), row.getTitle(), row.getWikiTarget(), null,
					row.getTargets(), row.getFields(), row.getLinks()))))
				.getRows().get(0);
			for (GuideRow candidate : List.of(row, legacy))
			{
				assertFalse(candidate.training().isUnresolved());
				assertEquals(1, candidate.training().getTargets().size());
				TrainingEvaluation actual = TrainingEvaluation.of(candidate,
					ProgressTest.observation("synthetic", "Fixture", "STANDARD", Map.of(),
						Map.of("PRAYER", 50, "FARMING", 50)));
				assertEquals(candidate.getTitle(), Completion.State.COMPLETE, actual.getState());
				assertEquals(Integer.valueOf(100), actual.getComparisons().get(0).getPercent());
			}
		}
		String farming = reported.stream().filter(row -> row.getTitle().startsWith("Train Farming")).findFirst()
			.orElseThrow().getTitle();
		int[] levels =
		{ 40, 41, 44, 46, 50 }, percentages =
		{ 0, 0, 60, 100, 100 };
		for (int i = 0; i < levels.length; i++)
		{
			TrainingEvaluation result = evaluate(farming, Map.of("FARMING", levels[i]));
			assertEquals(levels[i] < 41 ? Completion.State.INCOMPLETE
				: levels[i] < 46 ? Completion.State.IN_PROGRESS : Completion.State.COMPLETE, result.getState());
			assertEquals(Integer.valueOf(percentages[i]), result.getComparisons().get(0).getPercent());
		}
		assertEquals(Completion.State.UNKNOWN,
			evaluate("Train Prayer from level 33 to level 37 for Protect from Magic and hand in a diary",
				Map.of("PRAYER", 50)).getState());
	}

	private static GuideRow row(String title) throws Exception
	{
		return new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity(title))).get(0);
	}

	private static TrainingEvaluation evaluate(String title, Map<String, Integer> levels) throws Exception
	{
		return TrainingEvaluation.of(row(title),
			ProgressTest.observation("synthetic", "Fixture", "STANDARD", Map.of(), levels));
	}

	@Test
	public void explicitIntervalsUseRealLevelsAndFlooredClampedPercentages() throws Exception
	{
		for (int level : List.of(51, 52, 54, 55, 70))
		{
			TrainingEvaluation result = evaluate("Train Woodcutting from level 52 to level 55",
				Map.of("WOODCUTTING", level));
			assertEquals(level < 52 ? Completion.State.INCOMPLETE
				: level < 55 ? Completion.State.IN_PROGRESS : Completion.State.COMPLETE, result.getState());
			assertEquals(Integer.valueOf(level < 52 ? 0 : level == 52 ? 0 : level == 54 ? 66 : 100),
				result.getComparisons().get(0).getPercent());
			assertEquals(Integer.valueOf(level), result.getComparisons().get(0).getLevel());
		}
		assertEquals(Integer.valueOf(100), evaluate("Train Defence from level 60 to level 65", Map.of("DEFENCE", 65))
			.getComparisons().get(0).getPercent());
	}

	@Test
	public void targetOnlyAndDegenerateIntervalsNeverInventABase() throws Exception
	{
		for (String source : List.of("Train Mining to level 60", "Train Mining from level 60 to level 60",
			"Train Mining from level 70 to level 60"))
		{
			TrainingEvaluation result = evaluate(source, Map.of("MINING", 59));
			assertEquals(Completion.State.INCOMPLETE, result.getState());
			assertNull(result.getComparisons().get(0).getPercent());
			assertEquals(Completion.State.COMPLETE, evaluate(source, Map.of("MINING", 60)).getState());
		}
	}

	@Test
	public void missingOrInvalidLevelsAreNotZeroAndGoalsRemainDistinctFromAdvice() throws Exception
	{
		for (Map<String, Integer> levels : List.of(Map.<String, Integer>of(), Map.of("MINING", 0),
			Map.of("MINING", -1)))
		{
			TrainingEvaluation result = evaluate("Train Mining from level 55 to level 60", levels);
			assertEquals(Completion.State.UNKNOWN, result.getState());
			assertNull(result.getComparisons().get(0).getLevel());
			assertNull(result.getComparisons().get(0).getPercent());
		}
		GuideRow goal = row("Train Mining to level 60 (Recommended: train Strength from level 10 to level 40)");
		assertEquals(Map.of("MINING", 60), goal.getTargets());
		assertEquals(1, goal.training().getTargets().size());
		assertNull(goal.training().getTargets().get(0).getBase());
		GuideRow handIn = row("Hand in Herbi Flax diary, from level 25 to level 25");
		assertEquals(GuideRow.Kind.ACTIVITY, handIn.getKind());
		assertEquals(Completion.State.UNKNOWN,
			Completion
				.of(handIn,
					ProgressTest.observation("fixture", "Fixture", "STANDARD", Map.of(), Map.of("HERBLORE", 99)))
				.getState());
	}

	@Test
	public void compoundPrecedenceRetainsUsefulFactsWithoutFalseCompletion() throws Exception
	{
		String source = "Train Attack and Strength from level 10 to level 20";
		assertEquals(Completion.State.COMPLETE, evaluate(source, Map.of("ATTACK", 20, "STRENGTH", 20)).getState());
		assertEquals(Completion.State.IN_PROGRESS, evaluate(source, Map.of("ATTACK", 20, "STRENGTH", 9)).getState());
		assertEquals(Completion.State.IN_PROGRESS, evaluate(source, Map.of("ATTACK", 15)).getState());
		assertEquals(Completion.State.UNKNOWN, evaluate(source, Map.of("ATTACK", 20)).getState());
		assertEquals(Completion.State.INCOMPLETE, evaluate(source, Map.of("ATTACK", 9)).getState());
		assertEquals(Completion.State.UNKNOWN, evaluate(source, Map.of()).getState());
		TrainingEvaluation unknown = evaluate("Train Attack and Unrecognized from level 10 to level 20",
			Map.of("ATTACK", 20));
		assertTrue(unknown.isPartial());
		assertEquals(Completion.State.UNKNOWN, unknown.getState());
		assertEquals("ATTACK", unknown.getComparisons().get(0).getSkill());
	}

	@Test
	public void contradictoryOrUnsupportedTargetsDoNotComplete() throws Exception
	{
		for (String source : List.of("Train Attack to level 20 and Attack to level 30", "Train Mining to level 100",
			"Train Mystery to level 20"))
		{
			assertEquals(Completion.State.UNKNOWN, evaluate(source, Map.of("ATTACK", 99, "MINING", 99)).getState());
		}
		TrainingInstruction aliases = row("Train Defense and Runecrafting from level 10 to level 20").training();
		assertEquals(List.of("DEFENCE", "RUNECRAFT"),
			List.of(aliases.getTargets().get(0).getSkill(), aliases.getTargets().get(1).getSkill()));
	}

	@Test
	public void unsupportedRequirementsRemainNamedAndExtraActionsPreventFalseCompletion() throws Exception
	{
		TrainingEvaluation result = evaluate("Train Attack and Mystery from level 10 to level 20",
			Map.of("ATTACK", 20, "Mystery", 99));
		assertEquals(2, result.getComparisons().size());
		assertEquals("Mystery", result.getComparisons().get(1).getSkill());
		assertNull(result.getComparisons().get(1).getLevel());
		assertEquals(Completion.State.UNKNOWN, result.getState());
		assertEquals(Completion.State.UNKNOWN,
			evaluate("Train Mining from level 55 to level 60 and hand in a diary", Map.of("MINING", 60)).getState());
	}
}
