package com.questcape.guide.classification;

import com.questcape.guide.interpretation.ProjectedLevelInterpreter;
import com.questcape.guide.interpretation.SkillMentionInterpreter;
import com.questcape.guide.model.ClassifiedAction;
import com.questcape.guide.model.GuideRow;
import com.questcape.guide.source.GuideFieldRole;
import com.questcape.guide.source.RowEvidence;
import com.questcape.guide.source.SourceRow;
import com.questcape.persistence.LegacyKeyCodec;
import java.util.*;

/** Producer/cache boundary: semantic work precedes immutable row construction. */
public final class GuideAssembler
{
	public static GuideRow source(SourceRow source)
	{
		return GuideClassifier.classify(source);
	}

	static GuideRow assemble(String key, SourceRow source, GuideRow.Kind kind, ClassifiedAction action)
	{
		Map<String, Integer> targets = new LinkedHashMap<>();
		if (!action.getTraining().isUnresolved())
		{
			action.getTraining().getTargets().forEach(target -> targets.put(target.getSkill(), target.getGoal()));
		}
		return create(key, source.getPosition(), kind, source.getTitle(), source.getWikiTarget(), targets,
			source.getFields(), source.getLinks(), source.getEvidence(), action);
	}

	public static GuideRow row(String key, int position, GuideRow.Kind kind, String title, String wiki, String quest,
		Map<String, Integer> targets, Map<String, String> fields, Map<String, String> links)
	{
		return row(key, position, kind, title, wiki, quest, targets, fields, links, null);
	}

	/** Explicit data-producer adapter; semantic preparation still has one central owner. */
	public static GuideRow row(String key, int position, GuideRow.Kind kind, String title, String wiki, String quest,
		Map<String, Integer> targets, Map<String, String> fields, Map<String, String> links, RowEvidence evidence)
	{
		return GuideClassifier.legacy(key, position, kind, title, wiki, quest, targets, fields, links, evidence);
	}

	public static GuideRow repeated(GuideRow original, String key, int position)
	{
		ClassifiedAction action = original.isManual()
			? ClassifiedAction.unknown(ClassifiedAction.Reason.UNSUPPORTED_ACTION)
			: original.action();
		return create(key, position, original.getKind(), original.getTitle(), original.getWikiTarget(),
			original.getTargets(), original.getFields(), original.getLinks(), original.getEvidence(), action);
	}

	static GuideRow create(String key, int position, GuideRow.Kind kind, String title, String wiki,
		Map<String, Integer> targets,
		Map<String, String> fields, Map<String, String> links, RowEvidence evidence, ClassifiedAction action)
	{
		List<GuideRow.Field> roles = new ArrayList<>();
		fields.forEach((label, value) ->
		{
			GuideFieldRole role = GuideFieldRole.of(label);
			roles.add(new GuideRow.Field(role, label, value, SkillMentionInterpreter.interpret(value),
				role == GuideFieldRole.PROJECTIONS ? ProjectedLevelInterpreter.interpret(value, evidence.spans(role))
					: List.of()));
		});
		return new GuideRow(key, position, kind, title, wiki,
			action.getQuest() == null ? null : LegacyKeyCodec.encode(action.getQuest()),
			action.getActivity() == null ? null : LegacyKeyCodec.encode(action.getActivity()), targets, fields, links,
			evidence, roles, action);
	}

	private GuideAssembler()
	{
	}
}
