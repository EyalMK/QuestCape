package com.questcape.progress.evaluation;

import com.questcape.guide.interpretation.SkillCatalog;
import com.questcape.guide.interpretation.SkillTargetKey;
import com.questcape.guide.interpretation.TrainingInstruction;
import com.questcape.guide.model.GuideRow;
import com.questcape.progress.model.AccountProgress;
import java.util.*;
import lombok.Value;
import net.runelite.api.Experience;

/** Shared domain result for cards, filters, totals, navigation and per-skill presentation. */
@Value
public class TrainingEvaluation
{
	private static final int PERCENT = 100;
	private static final List<net.runelite.api.Skill> COMBAT_SKILLS = List.of(net.runelite.api.Skill.ATTACK,
		net.runelite.api.Skill.STRENGTH, net.runelite.api.Skill.DEFENCE, net.runelite.api.Skill.HITPOINTS,
		net.runelite.api.Skill.MAGIC, net.runelite.api.Skill.RANGED, net.runelite.api.Skill.PRAYER);
	Completion.State state;
	List<Comparison> comparisons;
	boolean partial;

	@Value
	public static class Comparison
	{
		String skill;
		Integer level;
		Integer percent;
		boolean complete;
	}

	public static TrainingEvaluation of(GuideRow row, AccountProgress account)
	{
		TrainingInstruction instruction = row.training();
		boolean missing = instruction.isUnresolved(), complete = false, incomplete = false, inProgress = false;
		List<Comparison> comparisons = new ArrayList<>();
		for (TrainingInstruction.Target target : instruction.getTargets())
		{
			Integer level = level(account, target.key());
			Integer percent = null;
			if (level == null || !SkillCatalog.validLevel(target.getSkill(), target.getGoal()))
			{
				missing = true;
			}
			else
			{
				complete |= level >= target.getGoal();
				incomplete |= level < target.getGoal();
				inProgress |= target.getBase() != null && level >= target.getBase() && level < target.getGoal();
				if (target.getBase() != null)
				{
					percent = (int)Math.floor(PERCENT * Math.max(0, Math.min(1,
						(double)(level - target.getBase()) / (target.getGoal() - target.getBase()))));
				}
			}
			comparisons.add(new Comparison(target.getSkill(), level, percent,
				level != null && SkillCatalog.validLevel(target.getSkill(), target.getGoal())
					&& level >= target.getGoal()));
		}
		Completion.State state = !missing && !incomplete ? Completion.State.COMPLETE
			: inProgress || complete && incomplete ? Completion.State.IN_PROGRESS
				: incomplete ? Completion.State.INCOMPLETE : Completion.State.UNKNOWN;
		return new TrainingEvaluation(state, Collections.unmodifiableList(comparisons), missing);
	}

	private static Integer level(AccountProgress account, SkillTargetKey target)
	{
		if (account == null || target == null)
		{
			return null;
		}
		if (!target.combat())
		{
			return account.realLevel(target.getSkill());
		}
		if (COMBAT_SKILLS.stream().anyMatch(skill -> account.realLevel(skill) == null))
		{
			return null;
		}
		return Experience.getCombatLevel(account.realLevel(net.runelite.api.Skill.ATTACK),
			account.realLevel(net.runelite.api.Skill.STRENGTH), account.realLevel(net.runelite.api.Skill.DEFENCE),
			account.realLevel(net.runelite.api.Skill.HITPOINTS), account.realLevel(net.runelite.api.Skill.MAGIC),
			account.realLevel(net.runelite.api.Skill.RANGED), account.realLevel(net.runelite.api.Skill.PRAYER));
	}
}
