package com.questcape.progress.model;

import java.util.*;
import lombok.Value;

@Value
public class AccountProgress
{
	public static final int SCHEMA = 1;

	public enum QuestStatus
	{
		INCOMPLETE, IN_PROGRESS, COMPLETE
	}

	int schema = SCHEMA;
	String scope;
	String username;
	String mode;
	String source;
	long retrievedAt;
	Long observedAt;
	Map<String, QuestStatus> quests;
	Map<String, Integer> levels;
	Map<String, Boolean> activities;
	Set<String> manual;
	transient Map<QuestKey, QuestStatus> questFacts;
	transient Map<net.runelite.api.Skill, Integer> skillFacts;
	transient Map<com.questcape.progress.activity.definitions.ActivityKey, Boolean> activityFacts;

	public AccountProgress(String scope, String username, String mode, String source, long retrievedAt,
		Long observedAt, Map<String, QuestStatus> quests, Map<String, Integer> levels,
		Map<String, Boolean> activities, Set<String> manual)
	{
		this.scope = scope;
		this.username = username;
		this.mode = mode;
		this.source = source;
		this.retrievedAt = retrievedAt;
		this.observedAt = observedAt;
		this.quests = Collections.unmodifiableMap(new HashMap<>(quests));
		this.levels = Collections.unmodifiableMap(new HashMap<>(levels));
		this.activities = Collections.unmodifiableMap(new HashMap<>(activities));
		this.manual = Collections.unmodifiableSet(new HashSet<>(manual));
		this.questFacts = com.questcape.persistence.LegacyKeyCodec.quests(quests);
		Map<net.runelite.api.Skill, Integer> decoded = new EnumMap<>(net.runelite.api.Skill.class);
		for (net.runelite.api.Skill skill : net.runelite.api.Skill.values())
		{
			Integer value = levels.get(skill.name());
			if (value != null && com.questcape.guide.interpretation.SkillCatalog.validLevel(skill.name(), value))
			{
				decoded.put(skill, value);
			}
		}
		this.skillFacts = Collections.unmodifiableMap(decoded);
		this.activityFacts = com.questcape.persistence.LegacyKeyCodec.activities(activities);
	}

	public Integer realLevel(net.runelite.api.Skill skill)
	{
		return skillFacts == null ? levels.get(skill.name()) : skillFacts.get(skill);
	}

	public QuestStatus quest(QuestKey key)
	{
		return (questFacts == null ? com.questcape.persistence.LegacyKeyCodec.quests(quests) : questFacts).get(key);
	}

	public Boolean fact(com.questcape.progress.activity.definitions.ActivityKey key)
	{
		return (activityFacts == null ? com.questcape.persistence.LegacyKeyCodec.activities(activities) : activityFacts)
			.get(key);
	}

	public AccountProgress withManual(Set<String> checks)
	{
		return new AccountProgress(scope, username, mode, source, retrievedAt, observedAt, quests, levels, activities,
			checks);
	}
}
