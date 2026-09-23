package com.questcape.progress;

import java.text.Normalizer;
import java.util.*;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public final class Identities
{
	private static final Map<String, String> QUESTS = new HashMap<>();
	static
	{
		for (Quest quest : Quest.values())
		{
			QUESTS.put(normalize(quest.getName()), quest.name());
		}
		alias("Recipe for Disaster/Freeing the Mountain Dwarf", "Recipe for Disaster - Mountain Dwarf");
		alias("Recipe for Disaster/Freeing the Goblin generals", "Recipe for Disaster - Wartface & Bentnoze");
		alias("Recipe for Disaster/Freeing Pirate Pete", "Recipe for Disaster - Pirate Pete");
		alias("Recipe for Disaster/Freeing the Lumbridge Guide", "Recipe for Disaster - Lumbridge Guide");
		alias("Recipe for Disaster/Freeing Evil Dave", "Recipe for Disaster - Evil Dave");
		alias("Recipe for Disaster/Freeing Skrach Uglogwee", "Recipe for Disaster - Skrach Uglogwee");
		alias("Recipe for Disaster/Freeing Sir Amik Varze", "Recipe for Disaster - Sir Amik Varze");
		alias("Recipe for Disaster/Freeing King Awowogei", "Recipe for Disaster - King Awowogei");
		alias("Recipe for Disaster/Another Cook's Quest", "Recipe for Disaster - Another Cook's Quest");
		alias("Recipe for Disaster/Defeating the Culinaromancer", "Recipe for Disaster - Culinaromancer");
	}

	private Identities()
	{
	}

	private static void alias(String source, String target)
	{
		String id = QUESTS.get(normalize(target));
		if (id != null)
		{
			QUESTS.put(normalize(source), id);
		}
	}

	public static String normalize(String text)
	{
		return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKC)
			.replace('_', ' ').replace('\u00a0', ' ').replace('\u2019', '\'')
			.replace('\u2013', '-').replace('\u2014', '-').trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

	public static String quest(String title)
	{
		return QUESTS.get(normalize(title));
	}

	public static String skill(String name)
	{
		String normalized = normalize(name);
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
}
