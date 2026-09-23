package com.questcape.guide;

import com.questcape.progress.Identities;
import java.util.*;
import java.util.regex.*;

/** Classifies the guide's instructions independently from whether their completion can be observed. */
final class GuideClassifier
{
	private static final Pattern TRAIN = Pattern.compile(
		"(?i)\\b([a-z]++(?:\\s*(?:,|and|&)\\s*[a-z]++)*+)\\s+"
			+ "(?:from\\s+level\\s+\\d+\\s+)?(?:to\\s+(?:level\\s+)?|level\\s+to\\s+)(\\d+)");
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
		else if (normalized.contains("(miniquest)") || normalized.startsWith("start miniquest:"))
		{
			kind = GuideRow.Kind.MINIQUEST;
		}
		else if (quest != null)
		{
			kind = GuideRow.Kind.QUEST;
		}
		else if (normalized.contains("diary"))
		{
			kind = GuideRow.Kind.DIARY;
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
		// Parenthetical recommendations are not requirements for completing the training step.
		Matcher match = TRAIN.matcher(title.replaceAll("\\([^)]*\\)", ""));
		while (match.find())
		{
			int target;
			try
			{
				target = Integer.parseInt(match.group(2));
			}
			catch (NumberFormatException e)
			{
				targets.clear();
				return;
			}
			for (String name : match.group(1).split("(?i)\\s*(?:,|and|&)\\s*"))
			{
				String skill = Identities.normalize(name).equals("combat") ? "COMBAT" : Identities.skill(name);
				if (skill == null || target < 1 || target > 126)
				{
					targets.clear();
					return;
				}
				targets.put(skill, target);
			}
		}
	}
}
