package com.questcape.progress;

import com.questcape.guide.*;

/** Compatibility facade. New domain code uses the focused source catalogs. */
public final class Identities
{
	public static String normalize(String text)
	{
		return SourceTextNormalizer.normalize(text);
	}

	public static String quest(String text)
	{
		return QuestCatalog.quest(text);
	}

	public static String skill(String text)
	{
		String key = SkillCatalog.identity(text);
		return SkillCatalog.COMBAT.equals(key) ? null : key;
	}

	private Identities()
	{
	}
}
