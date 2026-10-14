package com.questcape.guide;

import com.questcape.progress.Identities;
import com.questcape.progress.ActivityProgress;
import java.util.*;
import java.util.regex.*;

/** Classifies the guide's instructions independently from whether their completion can be observed. */
final class GuideClassifier
{
	private static final Pattern ACTION = Pattern.compile(
		"^(?:train|start|complete|finish|do|visit|talk|speak|unlock|claim|hand|gain|reach|obtain|buy|equip|"
			+ "kill|defeat|enter|travel|collect|use|make|build|return|read|give|bring|learn|earn)\\b");
	private static final Pattern COMMENT = Pattern.compile(
		"^(?:note|tip|important|comment|reminder|recommendation)\\s*:|"
			+ "^(?:all|these|those|this|the following|from here|from this point|you can|you may|you have|"
			+ "you now|congratulations)\\b");

	private GuideClassifier()
	{
	}

	static GuideRow classify(int position, String title, String wikiTarget,
		Map<String, String> fields, Map<String, String> links, boolean fullWidth)
	{
		String normalized = Identities.normalize(title);
		String quest = Identities.quest(wikiTarget);
		if (quest == null)
		{
			quest = Identities.quest(title.replaceAll("(?i)\\(miniquest\\)", "").trim());
		}
		Map<String, Integer> targets = new LinkedHashMap<>();
		GuideRow.Kind kind;
		if (normalized.startsWith("note:") || fullWidth && isComment(normalized, quest == null))
		{
			kind = GuideRow.Kind.INFORMATION;
			quest = null;
		}
		else if (normalized.startsWith("train "))
		{
			kind = GuideRow.Kind.TRAINING;
			readTargets(title, targets);
			quest = null;
		}
		else if (normalized.contains("(miniquest)") || normalized.startsWith("start miniquest:"))
		{
			kind = GuideRow.Kind.MINIQUEST;
		}
		else if (ActivityProgress.diary(title) != null
			|| normalized.contains("diary") && !normalized.startsWith("hand in"))
		{
			kind = GuideRow.Kind.DIARY;
			quest = null;
		}
		else if (normalized.startsWith("hand in") || normalized.startsWith("claim "))
		{
			kind = GuideRow.Kind.ACTIVITY;
			quest = null;
		}
		else if (normalized.startsWith("unlock:") || normalized.startsWith("unlock "))
		{
			kind = GuideRow.Kind.UNLOCK;
			quest = null;
		}
		else if (quest != null)
		{
			kind = GuideRow.Kind.QUEST;
		}
		else
		{
			kind = GuideRow.Kind.UNKNOWN;
		}
		String stage = normalized.startsWith("start ") ? ":start" : ":finish";
		String key = kind.name() + ":" + (quest == null
			? GuideParser.digest(Identities.normalize(wikiTarget) + "|" + normalized) : quest + stage);
		return new GuideRow(key, position, kind, title, wikiTarget, quest, targets, fields, links);
	}

	private static boolean isComment(String title, boolean noQuestIdentity)
	{
		return !ACTION.matcher(title).find()
			&& (COMMENT.matcher(title).find() || noQuestIdentity && (title.endsWith(".") || title.endsWith("!")));
	}

	private static void readTargets(String title, Map<String, Integer> targets)
	{
		TrainingInstruction instruction = TrainingInstruction.parse(title);
		if (!instruction.isUnresolved())
		{
			instruction.getTargets().forEach(target -> targets.put(target.getSkill(), target.getGoal()));
		}
	}
}
