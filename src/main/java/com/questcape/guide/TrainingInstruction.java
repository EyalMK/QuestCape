package com.questcape.guide;

import java.util.*;
import lombok.Value;

/** Bounded interpretation of source instructions, never a rendered Swing title. */
@Value
public class TrainingInstruction
{
	List<Target> targets;
	List<Token> tokens;
	boolean unresolved;

	@Value
	public static class Target
	{
		String skill;
		Integer base;
		int goal;
		transient SkillTargetKey identity;

		public Target(String skill, Integer base, int goal)
		{
			this.skill = skill;
			this.base = base;
			this.goal = goal;
			this.identity = SkillTargetKey.fromSource(skill);
		}

		public SkillTargetKey key()
		{
			return identity;
		}
	}

	@Value
	public static class Token
	{
		int start;
		int end;
		String skill;
	}

	public static TrainingInstruction empty()
	{
		return new TrainingInstruction(Collections.emptyList(), Collections.emptyList(), true);
	}

	public static TrainingInstruction parse(String source)
	{
		return TrainingTargetInterpreter.interpret(source);
	}
}
