package com.questcape.guide.classification;

import java.util.*;
import net.runelite.api.Quest;

import static com.questcape.guide.interpretation.SourceTextNormalizer.normalize;

public final class QuestCatalog
{
	private static final Map<String, String> QUESTS = new HashMap<>();
	static
	{
		for (Quest quest : Quest.values())
		{
			register(quest.getName(), quest.name());
		}
		register("natural history quiz", com.questcape.progress.model.QuestKey.Extra.NATURAL_HISTORY_QUIZ.getToken());
		register("knight waves training grounds", com.questcape.progress.model.QuestKey.Extra.KNIGHT_WAVES.getToken());
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

	private QuestCatalog()
	{
	}

	private static void alias(String source, String target)
	{
		String id = QUESTS.get(normalize(target));
		if (id != null)
		{
			register(source, id);
		}
	}

	public static String quest(String title)
	{
		String exact = QUESTS.get(normalize(title));
		if (exact != null)
		{
			return exact;
		}
		String name = normalize(title).replaceAll("^(?:start miniquest:|start|complete|finish)\\s+", "")
			.replace("(miniquest)", "").trim();
		String identity = QUESTS.get(name);
		return identity == null ? QUESTS.get(name.replaceAll("\\.$", "")) : identity;
	}

	public static String exact(String source)
	{
		return QUESTS.get(normalize(source));
	}

	private static void register(String alias, String identity)
	{
		String previous = QUESTS.putIfAbsent(normalize(alias), identity);
		if (previous != null && !previous.equals(identity))
		{
			throw new IllegalStateException("Conflicting quest alias");
		}
	}
}
