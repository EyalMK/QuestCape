package com.questcape.guide;

import com.google.gson.Gson;
import com.questcape.progress.*;
import com.questcape.progress.activity.*;
import com.questcape.persistence.LegacyKeyCodec;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DomainBoundaryTest
{
	private GuideRow source(String title, String wiki, Map<String, String> attributes)
	{
		return GuideAssembler.source(new SourceRow(0, title, wiki, Map.of("Quest/Activity", title), Map.of(),
			new RowEvidence(Map.of(), attributes), false));
	}

	@Test
	public void metadataIdentitySurvivesWordingButDoesNotInventStageOrIgnoreContradiction()
	{
		GuideRow renamed = source("Complete this reviewed quest milestone", "",
			Map.of(RowEvidence.ROW_ID, "Cook's Assistant"));
		assertEquals("COOKS_ASSISTANT", renamed.getQuestIdentity());
		assertEquals(CompletionPolicy.QUEST_FINISH, renamed.action().getPolicy());
		GuideRow unknownAction = source("Visit this quest's location", "Cook's Assistant", Map.of());
		assertEquals(CompletionPolicy.UNKNOWN, unknownAction.action().getPolicy());
		GuideRow conflict = source("Cook's Assistant", "Cook's Assistant", Map.of(RowEvidence.ROW_ID, "Doric's Quest"));
		assertEquals(ClassifiedAction.Outcome.AMBIGUOUS, conflict.action().getOutcome());
		GuideRow conflictTraining = source("Train Prayer from level 33 to level 37 for Protect from Magic", "",
			Map.of(RowEvidence.SKILL, "prayer", RowEvidence.LEVEL, "40"));
		assertTrue(conflictTraining.training().isUnresolved());
		GuideRow handIn = source("Hand in the diary of Herbi Flax to the Apothecary", "",
			Map.of(RowEvidence.SKILL, "herblore", RowEvidence.LEVEL, "25"));
		assertEquals(CompletionPolicy.ACTIVITY, handIn.action().getPolicy());
	}

	@Test
	public void rowsArePassiveAndLegacyEvidenceIsAdaptedAtTheBoundary() throws Exception
	{
		GuideRow raw = new GuideRow("unchanged", 0, GuideRow.Kind.UNLOCK, "Unlock: Varrock Museum Kudos", "", null,
			Map.of(), Map.of(), Map.of());
		assertFalse(raw.isManual());
		assertNull(raw.getActivityIdentity());
		GuideRow prepared = new GuideParser().reclassify(new GuideSnapshot("fixture", 1, 1, null, null, List.of(raw)))
			.getRows().get(0);
		assertTrue(prepared.isManual());
		Gson gson = new Gson();
		GuideSnapshot restored = new GuideParser().reclassify(gson.fromJson(
			gson.toJson(new GuideSnapshot("fixture", 1, 1, null, null, List.of(prepared))), GuideSnapshot.class));
		assertEquals(prepared.getKey(), restored.getRows().get(0).getKey());
		assertEquals(prepared.action(), restored.getRows().get(0).action());
	}

	@Test
	public void detectorFailureKeepsOtherKnownFactsAndDuplicateDefinitionsFail()
	{
		net.runelite.api.Client client = mock(net.runelite.api.Client.class);
		when(client.getVarbitValue(net.runelite.api.gameval.VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE))
			.thenThrow(new IllegalStateException("unavailable"));
		when(client.getVarbitValue(net.runelite.api.gameval.VarbitID.ARDOUGNE_EASY_REWARD)).thenReturn(1);
		ActivityObservation facts = new DiaryTierDetector().capture(client);
		assertFalse(facts.getFacts().containsKey(LegacyKeyCodec.activity("DIARY:ARDOUGNE_EASY:complete")));
		assertEquals(Boolean.TRUE, facts.getFacts().get(LegacyKeyCodec.activity("DIARY:ARDOUGNE_EASY:reward")));
		assertThrows(IllegalArgumentException.class,
			() -> new ActivityRegistry(List.of(new DiaryTierDetector(), new DiaryTierDetector())));
	}

	@Test
	public void sharedTokenizerRetainsUnicodeOffsetsAndUnknownProjectionText()
	{
		String text = "🪓 Defence and Runecrafting — keep every word";
		for (TrainingInstruction.Token token : SkillMentionInterpreter.interpret(text))
		{
			assertNotNull(SkillCatalog.identity(text.substring(token.getStart(), token.getEnd())));
		}
		assertEquals(2, SkillMentionInterpreter.interpret(text).size());
		assertEquals(2, ProjectedLevelInterpreter.interpret("65 Magic, 40 Runecrafting").size());
		assertTrue(ProjectedLevelInterpreter.interpret("65 Magic (boost advised)").isEmpty());
	}
}
