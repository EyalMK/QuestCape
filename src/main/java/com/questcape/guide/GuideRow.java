package com.questcape.guide;

import java.util.*;
import lombok.Value;
import com.questcape.progress.ActivityProgress;

@Value
public class GuideRow
{
	public enum Kind
	{
		QUEST, MINIQUEST, TRAINING, DIARY, UNLOCK, ACTIVITY, UNKNOWN, INFORMATION
	}

	String key;
	int position;
	Kind kind;
	String title;
	String wikiTarget;
	String questIdentity;
	String activityIdentity;
	Map<String, Integer> targets;
	Map<String, String> fields;
	Map<String, String> links;
	RowEvidence evidence;
	transient TrainingInstruction instruction;
	transient List<Field> sourceFields;

	@Value
	public static class Field { GuideFieldRole role; String label; String value; }

	public GuideRow(String key, int position, Kind kind, String title, String wikiTarget,
		String questIdentity, Map<String, Integer> targets, Map<String, String> fields, Map<String, String> links)
	{
		this(key, position, kind, title, wikiTarget, questIdentity, targets, fields, links, null);
	}

	public GuideRow(String key, int position, Kind kind, String title, String wikiTarget,
		String questIdentity, Map<String, Integer> targets, Map<String, String> fields, Map<String, String> links,
		RowEvidence evidence)
	{
		this.key = key;
		this.position = position;
		this.kind = kind;
		this.title = title;
		this.wikiTarget = wikiTarget;
		this.questIdentity = questIdentity;
		this.activityIdentity = kind == Kind.DIARY ? ActivityProgress.diary(title)
			: kind == Kind.UNLOCK ? ActivityProgress.unlock(title)
			: kind == Kind.ACTIVITY ? ActivityProgress.handIn(title) : null;
		this.targets = Collections.unmodifiableMap(new LinkedHashMap<>(targets));
		this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
		this.sourceFields = interpretFields(fields);
		this.links = Collections.unmodifiableMap(new LinkedHashMap<>(links));
		this.evidence = evidence == null ? new RowEvidence(Collections.emptyMap()) : evidence;
		TrainingInstruction parsed = TrainingInstruction.parse(kind == Kind.TRAINING ? title : "");
		if (kind == Kind.TRAINING && parsed.getTargets().isEmpty() && !targets.isEmpty())
		{
			List<TrainingInstruction.Target> retained = new ArrayList<>();
			boolean unresolved = false;
			for (Map.Entry<String, Integer> target : targets.entrySet())
			{
				if (target.getValue() != null && SkillCatalog.validLevel(SkillCatalog.identity(target.getKey()), target.getValue()))
				{ retained.add(new TrainingInstruction.Target(SkillCatalog.identity(target.getKey()), null, target.getValue())); }
				else { unresolved = true; }
			}
			parsed = new TrainingInstruction(Collections.unmodifiableList(retained), parsed.getTokens(), unresolved || retained.isEmpty());
		}
		this.instruction = parsed;
	}

	public TrainingInstruction training()
	{
		return instruction == null ? TrainingInstruction.parse(kind == Kind.TRAINING ? title : "") : instruction;
	}

	private static List<Field> interpretFields(Map<String, String> values)
	{
		List<Field> result = new ArrayList<>();
		values.forEach((label, value) -> result.add(new Field(GuideFieldRole.of(label), label, value)));
		return Collections.unmodifiableList(result);
	}
	public List<Field> sourceFields() { return sourceFields == null ? interpretFields(fields) : sourceFields; }
	public Map<String, String> links(GuideFieldRole role)
	{
		return RowEvidence.resolve(role, this);
	}

	public GuideRow withEvidence(RowEvidence value)
	{
		return new GuideRow(key, position, kind, title, wikiTarget, questIdentity, targets, fields, links, value);
	}

	public boolean isManual()
	{
		return !key.contains(":repeated:") && kind == Kind.UNLOCK && ActivityProgress.isKudos(title);
	}

	public boolean isActionable()
	{
		return kind != Kind.INFORMATION;
	}
}
