package com.questcape.guide;

import net.runelite.api.Skill;
import lombok.Value;

/** Ordinary API skills or derived combat; no invented Skill.COMBAT or duplicated skill enumeration. */
@Value
public class SkillTargetKey
{
	Skill skill;

	private SkillTargetKey(Skill skill)
	{
		this.skill = skill;
	}

	public static SkillTargetKey fromSource(String value)
	{
		String key = SkillCatalog.identity(value);
		return key == null ? null : new SkillTargetKey(SkillCatalog.COMBAT.equals(key) ? null : Skill.valueOf(key));
	}

	public boolean combat()
	{
		return skill == null;
	}
}
