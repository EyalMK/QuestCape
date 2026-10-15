package com.questcape.guide;

import java.util.*;
import lombok.Value;

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
	transient ClassifiedAction prepared;
	transient List<Field> sourceFields;

	@Value
	public static class Field
	{
		GuideFieldRole role;
		String label;
		String value;
		List<TrainingInstruction.Token> tokens;
		List<ProjectedLevelInterpreter.Entry> projections;
	}

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
		this.activityIdentity = null;
		this.targets = Collections.unmodifiableMap(new LinkedHashMap<>(targets));
		this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
		this.sourceFields = Collections.emptyList();
		this.links = Collections.unmodifiableMap(new LinkedHashMap<>(links));
		this.evidence = evidence == null ? new RowEvidence(Collections.emptyMap()) : evidence;
		this.prepared = ClassifiedAction.unknown(ClassifiedAction.Reason.UNSUPPORTED_ACTION);
	}

	GuideRow(String key, int position, Kind kind, String title, String wikiTarget, String questIdentity,
		String activityIdentity, Map<String, Integer> targets,
		Map<String, String> fields, Map<String, String> links, RowEvidence evidence, List<Field> roles,
		ClassifiedAction action)
	{
		if (kind == Kind.INFORMATION && action.getPolicy() != com.questcape.progress.CompletionPolicy.INFORMATION
			|| action.getPolicy() == com.questcape.progress.CompletionPolicy.TRAINING && kind != Kind.TRAINING
			|| action.manual() && kind != Kind.UNLOCK
			|| action.getQuest() != null && kind != Kind.QUEST && kind != Kind.MINIQUEST)
		{
			throw new IllegalArgumentException("Incompatible row category and prepared action");
		}
		this.key = key;
		this.position = position;
		this.kind = kind;
		this.title = title;
		this.wikiTarget = wikiTarget;
		this.questIdentity = questIdentity;
		this.activityIdentity = activityIdentity;
		this.targets = Collections.unmodifiableMap(new LinkedHashMap<>(targets));
		this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
		this.links = Collections.unmodifiableMap(new LinkedHashMap<>(links));
		this.evidence = evidence;
		this.sourceFields = List.copyOf(roles);
		this.prepared = Objects.requireNonNull(action);
	}

	public ClassifiedAction action()
	{
		return prepared == null ? ClassifiedAction.unknown(ClassifiedAction.Reason.UNSUPPORTED_ACTION) : prepared;
	}

	public TrainingInstruction training()
	{
		return action().getTraining();
	}

	public List<Field> sourceFields()
	{
		return sourceFields == null ? Collections.emptyList() : sourceFields;
	}

	public Map<String, String> links(GuideFieldRole role)
	{
		return RowEvidence.resolve(role, this);
	}

	public GuideRow withEvidence(RowEvidence value)
	{
		return new GuideRow(key, position, kind, title, wikiTarget, questIdentity, activityIdentity, targets, fields,
			links, value, sourceFields(), action());
	}

	public boolean isManual()
	{
		return action().manual();
	}

	public boolean isActionable()
	{
		return kind != Kind.INFORMATION;
	}
}
