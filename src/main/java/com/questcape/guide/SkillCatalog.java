package com.questcape.guide;

import com.questcape.progress.Identities;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.runelite.api.Skill;
import net.runelite.api.Experience;

/** One local vocabulary for source skill mentions; combat is a derived target. */
public final class SkillCatalog
{
	public static final String COMBAT = "COMBAT";
	private static final String OVERALL = "OVERALL";
	public static final int MIN_LEVEL = 1, MAX_SKILL_LEVEL = Experience.MAX_REAL_LEVEL,
		MAX_COMBAT_LEVEL = Experience.MAX_COMBAT_LEVEL;
	private static final List<String> NAMES = new ArrayList<>();
	static
	{
		Arrays.stream(Skill.values()).map(Skill::getName).forEach(NAMES::add);
		NAMES.add("Runecrafting");
		NAMES.add("Defense");
	}
	private static final Pattern MENTIONS = Pattern
		.compile("(?i)(?<![\\p{L}\\p{N}_])(" + namesPattern() + ")(?![\\p{L}\\p{N}_])");

	private SkillCatalog()
	{
	}

	public static String identity(String name)
	{
		return Identities.normalize(name).equals("combat") ? COMBAT : Identities.skill(name);
	}

	public static String namesPattern()
	{
		return NAMES.stream().sorted(Comparator.comparingInt(String::length).reversed())
			.map(Pattern::quote).collect(Collectors.joining("|"));
	}

	public static List<TrainingInstruction.Token> tokens(String source)
	{
		Matcher matcher = MENTIONS.matcher(source);
		List<TrainingInstruction.Token> tokens = new ArrayList<>();
		while (matcher.find())
		{
			tokens.add(new TrainingInstruction.Token(matcher.start(), matcher.end(), identity(matcher.group())));
		}
		return Collections.unmodifiableList(tokens);
	}

	public static boolean validLevel(String skill, int level)
	{
		String identity = identity(skill);
		return identity != null && !OVERALL.equals(identity) && level >= MIN_LEVEL
			&& level <= (COMBAT.equals(identity) ? MAX_COMBAT_LEVEL : MAX_SKILL_LEVEL);
	}
}
