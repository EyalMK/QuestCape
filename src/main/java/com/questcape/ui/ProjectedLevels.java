package com.questcape.ui;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import net.runelite.api.Skill;

/** Parses stored guide text only; projected levels never become character facts. */
final class ProjectedLevels
{
	private static final Pattern LEVEL = Pattern.compile("(?i)(\\d{1,3})\\s+("
		+ Arrays.stream(Skill.values()).map(s -> s.getName()).sorted(Comparator.comparingInt(String::length).reversed())
			.map(Pattern::quote).collect(Collectors.joining("|")) + ")\\b");

	static final class Entry
	{
		final int level;
		final Skill skill;

		Entry(int level, Skill skill)
		{
			this.level = level;
			this.skill = skill;
		}
	}

	private ProjectedLevels()
	{
	}

	static List<Entry> parse(String text)
	{
		Matcher matcher = LEVEL.matcher(text);
		List<Entry> entries = new ArrayList<>();
		while (matcher.find())
		{
			int level = Integer.parseInt(matcher.group(1));
			if (level < 1 || level > 126)
			{
				return Collections.emptyList();
			}
			for (Skill skill : Skill.values())
			{
				if (skill.getName().equalsIgnoreCase(matcher.group(2)))
				{
					entries.add(new Entry(level, skill));
					break;
				}
			}
		}
		// Preserve unfamiliar/annotated source text verbatim instead of silently dropping it.
		return LEVEL.matcher(text).replaceAll("").replaceAll("[\\s,;]+", "").isEmpty()
			? entries : Collections.emptyList();
	}
}
