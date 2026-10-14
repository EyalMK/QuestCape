package com.questcape.guide;

import java.util.*;
import java.util.regex.*;
import lombok.Value;

/** Bounded interpretation of source instructions, never a rendered Swing title. */
@Value
public class TrainingInstruction
{
	private static final Pattern RECOMMENDATION = Pattern.compile("\\([^)]*\\)");
	private static final Pattern CONNECTORS = Pattern.compile("(?i)\\b(?:train|and)\\b|[,&.;]+|\\s+");
	private static final Pattern TARGET = Pattern.compile("(?i)\\b([a-z]++(?:\\s*(?:,|and|&)\\s*[a-z]++)*+)\\s+"
		+ "(?:from\\s+level\\s+(\\d{1,3})\\s+)?(?:to\\s+(?:level\\s+)?|level\\s+to\\s+)(\\d{1,3})\\b");
	List<Target> targets;
	List<Token> tokens;
	boolean unresolved;

	@Value
	public static class Target
	{
		String skill;
		Integer base;
		int goal;
	}

	@Value
	public static class Token
	{
		int start;
		int end;
		String skill;
	}

	public static TrainingInstruction parse(String source)
	{
		Map<String, Target> targets = new LinkedHashMap<>();
		String required = RECOMMENDATION.matcher(source).replaceAll("");
		Matcher match = TARGET.matcher(required);
		boolean unresolved = false;
		while (match.find())
		{
			int goal = Integer.parseInt(match.group(3));
			Integer base = match.group(2) == null ? null : Integer.valueOf(match.group(2));
			for (String name : match.group(1).split("(?i)\\s*(?:,|and|&)\\s*"))
			{
				String skill = SkillCatalog.identity(name);
				if (!SkillCatalog.validLevel(skill, goal))
				{
					unresolved = true;
					String unsupported = skill == null ? name.trim() : skill;
					targets.putIfAbsent(unsupported, new Target(unsupported, null, goal));
					continue;
				}
				Integer lower = base != null && SkillCatalog.validLevel(skill, base) && base < goal ? base : null;
				Target target = new Target(skill, lower, goal);
				Target previous = targets.putIfAbsent(skill, target);
				unresolved |= previous != null && !previous.equals(target);
			}
		}
		unresolved |= !CONNECTORS.matcher(TARGET.matcher(required).replaceAll(" ")).replaceAll("").isEmpty();
		return new TrainingInstruction(Collections.unmodifiableList(new ArrayList<>(targets.values())),
			SkillCatalog.tokens(source), unresolved || targets.isEmpty());
	}
}
