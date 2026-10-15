package com.questcape.guide;

import static com.questcape.guide.SourceTextNormalizer.normalize;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.runelite.api.Skill;
import net.runelite.api.Experience;

/** One local vocabulary for source skill mentions; combat is a derived target. */
public final class SkillCatalog
{
	public static final String COMBAT = "COMBAT";
	private static final String OVERALL = "OVERALL";
	public static final int MIN_LEVEL = 1, MAX_SKILL_LEVEL = Experience.MAX_REAL_LEVEL,
		MAX_COMBAT_LEVEL = Experience.MAX_COMBAT_LEVEL, MAX_LEVEL_DIGITS = 3;
	private static final List<String> NAMES = new ArrayList<>();
	static
	{
		Arrays.stream(Skill.values()).map(Skill::getName).forEach(NAMES::add);
		NAMES.add("Runecrafting");
		NAMES.add("Defense");
	}

	private SkillCatalog()
	{
	}

	public static String identity(String name)
	{
		String normalized = normalize(name);
		if (normalized.equals("combat"))
		{
			return COMBAT;
		}
		if (normalized.equals("runecrafting"))
		{
			normalized = "runecraft";
		}
		if (normalized.equals("defense"))
		{
			normalized = "defence";
		}
		for (Skill skill : Skill.values())
		{
			if (normalize(skill.getName()).equals(normalized))
			{
				return skill.name();
			}
		}
		return null;
	}

	public static String namesPattern()
	{
		return NAMES.stream().sorted(Comparator.comparingInt(String::length).reversed())
			.map(Pattern::quote).collect(Collectors.joining("|"));
	}

	public static List<TrainingInstruction.Token> tokens(String source)
	{
		return SkillMentionInterpreter.interpret(source);
	}

	public static boolean validLevel(String skill, int level)
	{
		String identity = identity(skill);
		return identity != null && !OVERALL.equals(identity) && level >= MIN_LEVEL
			&& level <= (COMBAT.equals(identity) ? MAX_COMBAT_LEVEL : MAX_SKILL_LEVEL);
	}
}
