package com.questcape.guide;

import com.questcape.progress.*;
import com.questcape.progress.activity.*;
import com.questcape.persistence.LegacyKeyCodec;
import java.util.*;
import java.util.regex.*;
import static com.questcape.guide.SourceTextNormalizer.normalize;

/** Reconciles independent identity hints and explicit requested actions at one boundary. */
final class GuideClassifier
{
	private static final Pattern COMMENT = Pattern.compile(
		"^(?:note|tip|important|comment|reminder|recommendation)\\s*:|^(?:all|these|those|this|the following|from here|from this point|you can|you may|you have|you now|congratulations)\\b");
	private static final Pattern START = Pattern.compile("^(?:start miniquest:|start)\\s+");
	private static final Pattern FINISH = Pattern.compile("^(?:complete|finish)\\s+");

	static GuideRow classify(int position, String title, String wiki, Map<String, String> fields,
		Map<String, String> links, boolean fullWidth)
	{
		return classify(new SourceRow(position, title, wiki, fields, links, null, fullWidth));
	}

	static GuideRow classify(SourceRow source)
	{
		String text = normalize(source.getTitle());
		boolean start = START.matcher(text).find();
		Set<QuestKey> quests = new HashSet<>();
		addQuest(quests, QuestCatalog.quest(source.getWikiTarget()));
		addQuest(quests, QuestCatalog.quest(source.getTitle()));
		addQuest(quests, QuestCatalog.exact(source.attribute(RowEvidence.ROW_ID)));
		boolean reward = text.startsWith("claim ") || text.startsWith("collect ") || text.startsWith("receive ");
		Set<ActivityKey> activities = new HashSet<>();
		add(activities, ActivityCatalog.diary(source.getTitle()));
		add(activities, ActivityCatalog.unlock(source.getTitle()));
		add(activities, ActivityCatalog.handIn(source.getTitle()));
		ActivityKey id = ActivityCatalog.rowId(source.attribute(RowEvidence.ROW_ID), reward);
		if (id != null)
		{
			activities.add(id);
		}
		GuideRow.Kind kind;
		ClassifiedAction action;
		if (COMMENT.matcher(text).find()
			|| source.isFullWidth() && normalize(source.getWikiTarget()).equals("quest point cape"))
		{
			kind = GuideRow.Kind.INFORMATION;
			action = matched(CompletionPolicy.INFORMATION, null, null);
		}
		else if (text.startsWith("train "))
		{
			kind = GuideRow.Kind.TRAINING;
			TrainingInstruction training = reconcileTraining(TrainingTargetInterpreter.interpret(source.getTitle()),
				source);
			action = new ClassifiedAction(
				training.isUnresolved() ? ClassifiedAction.Outcome.UNSUPPORTED : ClassifiedAction.Outcome.MATCHED,
				training.isUnresolved() ? ClassifiedAction.Reason.UNSUPPORTED_TRAINING : ClassifiedAction.Reason.NONE,
				CompletionPolicy.TRAINING, null, null, training);
		}
		else if (quests.size() > 1 || activities.size() > 1 || !quests.isEmpty() && !activities.isEmpty())
		{
			kind = GuideRow.Kind.UNKNOWN;
			action = ClassifiedAction.unknown(ClassifiedAction.Reason.CONFLICTING_IDENTITY);
		}
		else if (!activities.isEmpty())
		{
			ActivityKey key = activities.iterator().next();
			kind = key.getDiary() != null ? GuideRow.Kind.DIARY
				: key.getSpecial() == ActivityKey.Special.HERBI_FLAX ? GuideRow.Kind.ACTIVITY : GuideRow.Kind.UNLOCK;
			boolean explicit = ActivityCatalog.diary(source.getTitle()) != null
				|| ActivityCatalog.unlock(source.getTitle()) != null
				|| ActivityCatalog.handIn(source.getTitle()) != null
				|| id != null && (text.startsWith("unlock ") || FINISH.matcher(text).find() || reward);
			action = explicit ? matched(CompletionPolicy.ACTIVITY, null, key)
				: ClassifiedAction.unknown(ClassifiedAction.Reason.UNSUPPORTED_ACTION);
		}
		else if (!quests.isEmpty())
		{
			QuestKey quest = quests.iterator().next();
			boolean mini = text.contains("(miniquest)") || text.startsWith("start miniquest:")
				|| quest.getExtra() != null;
			kind = mini ? GuideRow.Kind.MINIQUEST : GuideRow.Kind.QUEST;
			// A reviewed bare quest is an explicit whole-quest instruction; unfamiliar verbs do not imply finish.
			boolean bare = QuestCatalog.exact(
				source.getTitle().replaceAll("(?i)\\s*\\(miniquest\\)", "").replaceAll("\\.$", "").trim()) != null;
			boolean canonicalLink = QuestCatalog.exact(source.getWikiTarget()) != null
				&& text.equals(normalize(source.getWikiTarget()));
			action = start || FINISH.matcher(text).find() || bare || canonicalLink
				? matched(start ? CompletionPolicy.QUEST_START : CompletionPolicy.QUEST_FINISH, quest, null)
				: new ClassifiedAction(ClassifiedAction.Outcome.UNSUPPORTED, ClassifiedAction.Reason.UNSUPPORTED_ACTION,
					CompletionPolicy.UNKNOWN, quest, null, TrainingInstruction.empty());
		}
		else if (ActivityCatalog.kudos(source.getTitle()))
		{
			kind = GuideRow.Kind.UNLOCK;
			action = matched(CompletionPolicy.MANUAL_KUDOS, null, null);
		}
		else
		{
			kind = text.contains("(miniquest)") ? GuideRow.Kind.MINIQUEST
				: text.contains("diary") && !text.startsWith("hand in") ? GuideRow.Kind.DIARY
					: text.startsWith("unlock") ? GuideRow.Kind.UNLOCK
						: text.startsWith("hand in") || text.startsWith("claim") ? GuideRow.Kind.ACTIVITY
							: GuideRow.Kind.UNKNOWN;
			action = ClassifiedAction.unknown(ClassifiedAction.Reason.UNSUPPORTED_ACTION);
		}
		String quest = action.getQuest() == null ? null : LegacyKeyCodec.encode(action.getQuest());
		return GuideAssembler.assemble(
			LegacyKeyCodec.row(kind, quest, start, source.getWikiTarget(), source.getTitle()), source, kind, action);
	}

	/** Legacy callers already supply category/operands; prepare them only at this input boundary. */
	static GuideRow legacy(String key, int position, GuideRow.Kind kind, String title, String wiki, String quest,
		Map<String, Integer> targets, Map<String, String> fields, Map<String, String> links, RowEvidence evidence)
	{
		TrainingInstruction training = kind == GuideRow.Kind.TRAINING ? TrainingTargetInterpreter.interpret(title)
			: TrainingInstruction.empty();
		if (kind == GuideRow.Kind.TRAINING && training.getTargets().isEmpty() && !targets.isEmpty())
		{
			List<TrainingInstruction.Target> retained = new ArrayList<>();
			boolean unresolved = false;
			for (Map.Entry<String, Integer> entry : targets.entrySet())
			{
				String skill = SkillCatalog.identity(entry.getKey());
				if (entry.getValue() != null && SkillCatalog.validLevel(skill, entry.getValue()))
				{
					retained.add(new TrainingInstruction.Target(skill, null, entry.getValue()));
				}
				else
				{
					unresolved = true;
				}
			}
			training = new TrainingInstruction(Collections.unmodifiableList(retained), training.getTokens(),
				unresolved || retained.isEmpty());
		}
		QuestKey questKey = LegacyKeyCodec.quest(quest);
		ActivityKey activity = kind == GuideRow.Kind.DIARY ? ActivityCatalog.diary(title)
			: kind == GuideRow.Kind.UNLOCK ? ActivityCatalog.unlock(title)
				: kind == GuideRow.Kind.ACTIVITY ? ActivityCatalog.handIn(title) : null;
		CompletionPolicy policy = kind == GuideRow.Kind.INFORMATION ? CompletionPolicy.INFORMATION
			: questKey != null ? CompletionPolicy.QUEST_FINISH
				: kind == GuideRow.Kind.TRAINING ? CompletionPolicy.TRAINING
					: activity != null ? CompletionPolicy.ACTIVITY
						: kind == GuideRow.Kind.UNLOCK && ActivityCatalog.kudos(title) && !LegacyKeyCodec.repeated(key)
							? CompletionPolicy.MANUAL_KUDOS
							: CompletionPolicy.UNKNOWN;
		ClassifiedAction action = new ClassifiedAction(
			policy == CompletionPolicy.UNKNOWN ? ClassifiedAction.Outcome.UNSUPPORTED
				: ClassifiedAction.Outcome.MATCHED,
			policy == CompletionPolicy.UNKNOWN ? ClassifiedAction.Reason.UNSUPPORTED_ACTION
				: ClassifiedAction.Reason.NONE,
			policy, questKey, activity, training);
		return GuideAssembler.create(key, position, kind, title, wiki, targets, fields, links,
			evidence == null ? new RowEvidence(Collections.emptyMap()) : evidence, action);
	}

	private static TrainingInstruction reconcileTraining(TrainingInstruction parsed, SourceRow source)
	{
		String skill = SkillCatalog.identity(source.attribute(RowEvidence.SKILL)),
			raw = source.attribute(RowEvidence.LEVEL);
		if (skill == null || raw.isBlank() || raw.length() > SkillCatalog.MAX_LEVEL_DIGITS
			|| !raw.chars().allMatch(Character::isDigit))
		{
			return parsed;
		}
		int level = Integer.parseInt(raw);
		if (!SkillCatalog.validLevel(skill, level))
		{
			return parsed;
		}
		boolean compatible = parsed.getTargets().stream()
			.anyMatch(target -> target.getSkill().equals(skill) && target.getGoal() == level);
		return compatible ? parsed : new TrainingInstruction(parsed.getTargets(), parsed.getTokens(), true);
	}

	private static ClassifiedAction matched(CompletionPolicy policy, QuestKey quest, ActivityKey activity)
	{
		return new ClassifiedAction(ClassifiedAction.Outcome.MATCHED, ClassifiedAction.Reason.NONE, policy, quest,
			activity, TrainingInstruction.empty());
	}

	private static void add(Set<ActivityKey> values, ActivityKey key)
	{
		if (key != null)
		{
			values.add(key);
		}
	}

	private static void addQuest(Set<QuestKey> values, String token)
	{
		QuestKey quest = LegacyKeyCodec.quest(token);
		if (quest != null)
		{
			values.add(quest);
		}
	}

	private GuideClassifier()
	{
	}
}
