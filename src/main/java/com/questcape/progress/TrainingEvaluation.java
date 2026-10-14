package com.questcape.progress;

import com.questcape.guide.*;
import java.util.*;
import lombok.Value;
import net.runelite.api.Experience;

/** Shared domain result for cards, filters, totals, navigation and per-skill presentation. */
@Value
public class TrainingEvaluation
{
	private static final int PERCENT = 100;
	private static final List<String> COMBAT_SKILLS = Arrays.asList("ATTACK", "STRENGTH", "DEFENCE", "HITPOINTS",
		"MAGIC", "RANGED", "PRAYER");
	Completion.State state;
	List<Comparison> comparisons;
	boolean partial;

	@Value
	public static class Comparison
	{
		String skill;
		Integer level;
		Integer percent;
	}

	public static TrainingEvaluation of(GuideRow row, AccountProgress account)
	{
		TrainingInstruction instruction = row.training();
		boolean missing = instruction.isUnresolved(), complete = false, incomplete = false, inProgress = false;
		List<Comparison> comparisons = new ArrayList<>();
		for (TrainingInstruction.Target target : instruction.getTargets())
		{
			Integer level = level(account, target.getSkill());
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
			comparisons.add(new Comparison(target.getSkill(), level, percent));
		}
		Completion.State state = !missing && !incomplete ? Completion.State.COMPLETE
			: inProgress || complete && incomplete ? Completion.State.IN_PROGRESS
				: incomplete ? Completion.State.INCOMPLETE : Completion.State.UNKNOWN;
		return new TrainingEvaluation(state, Collections.unmodifiableList(comparisons), missing);
	}

	private static Integer level(AccountProgress account, String skill)
	{
		if (account == null)
		{
			return null;
		}
		Map<String, Integer> levels = account.getLevels();
		if (!SkillCatalog.COMBAT.equals(skill))
		{
			Integer value = levels.get(skill);
			return value != null && SkillCatalog.validLevel(skill, value) ? value : null;
		}
		if (COMBAT_SKILLS.stream()
			.anyMatch(name -> levels.get(name) == null || !SkillCatalog.validLevel(name, levels.get(name))))
		{
			return null;
		}
		return Experience.getCombatLevel(levels.get("ATTACK"), levels.get("STRENGTH"), levels.get("DEFENCE"),
			levels.get("HITPOINTS"), levels.get("MAGIC"), levels.get("RANGED"), levels.get("PRAYER"));
	}
}
