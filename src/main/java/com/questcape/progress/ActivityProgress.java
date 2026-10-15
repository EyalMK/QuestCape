package com.questcape.progress;

import com.questcape.guide.ActivityCatalog;
import com.questcape.progress.activity.*;
import com.questcape.persistence.LegacyKeyCodec;
import java.util.*;
import net.runelite.api.Client;

/** Legacy API adapter. Source semantics and live predicates now have independent owners. */
public final class ActivityProgress
{
	private static String encoded(ActivityKey key)
	{
		return key == null ? null : LegacyKeyCodec.encode(key);
	}

	public static String diary(String title)
	{
		return encoded(ActivityCatalog.diary(title));
	}

	public static String unlock(String title)
	{
		return encoded(ActivityCatalog.unlock(title));
	}

	public static String handIn(String title)
	{
		return encoded(ActivityCatalog.handIn(title));
	}

	public static boolean isKudos(String title)
	{
		return ActivityCatalog.kudos(title);
	}

	public static Map<String, Boolean> readDiaries(Client client)
	{
		return legacy(new DiaryTierDetector().capture(client));
	}

	public static Map<String, Boolean> readActivities(Client client)
	{
		return legacy(ActivityRegistry.STANDARD.capture(client));
	}

	public static void readMiniquests(Client client, Map<String, AccountProgress.QuestStatus> quests)
	{
		ActivityRegistry.STANDARD.capture(client).getQuests()
			.forEach((key, value) -> quests.put(LegacyKeyCodec.encode(key), value));
	}

	private static Map<String, Boolean> legacy(ActivityObservation result)
	{
		Map<String, Boolean> facts = new HashMap<>();
		result.getFacts().forEach((key, value) -> facts.put(LegacyKeyCodec.encode(key), value));
		return facts;
	}

	private ActivityProgress()
	{
	}
}
