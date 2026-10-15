package com.questcape.persistence;

import com.questcape.progress.*;
import com.questcape.progress.activity.*;
import java.util.*;
import net.runelite.api.Quest;

/** All legacy syntax is confined to this compatibility boundary. Unknown tokens stay in stored DTOs. */
public final class LegacyKeyCodec
{
	private static final String DIARY = "DIARY:", BALLOON = "UNLOCK:BALLOON:", SEPARATOR = ":",
		START = ":start", FINISH = ":finish", REPEATED = ":repeated:";
	private static final Map<com.questcape.guide.GuideRow.Kind, String> ROW_KINDS = Map.of(
		com.questcape.guide.GuideRow.Kind.QUEST, "QUEST", com.questcape.guide.GuideRow.Kind.MINIQUEST, "MINIQUEST",
		com.questcape.guide.GuideRow.Kind.TRAINING, "TRAINING", com.questcape.guide.GuideRow.Kind.DIARY, "DIARY",
		com.questcape.guide.GuideRow.Kind.UNLOCK, "UNLOCK", com.questcape.guide.GuideRow.Kind.ACTIVITY, "ACTIVITY",
		com.questcape.guide.GuideRow.Kind.UNKNOWN, "UNKNOWN", com.questcape.guide.GuideRow.Kind.INFORMATION,
		"INFORMATION");
	private static final Map<String, ActivityKey> ACTIVITIES = new HashMap<>();
	private static final Map<String, QuestKey> QUESTS = new HashMap<>();
	static
	{
		if (ROW_KINDS.size() != com.questcape.guide.GuideRow.Kind.values().length)
		{
			throw new IllegalStateException("Every row category needs an explicit compatible wire token");
		}
		for (DiaryDefinitions.Definition definition : DiaryDefinitions.ALL)
		{
			for (DiaryKey.Action action : DiaryKey.Action.values())
			{
				ActivityKey key = ActivityKey.diary(definition.key(action));
				ACTIVITIES.put(encode(key), key);
			}
		}
		for (BalloonDestination destination : BalloonDestination.values())
		{
			ActivityKey key = ActivityKey.balloon(destination);
			ACTIVITIES.put(encode(key), key);
		}
		for (ActivityKey.Special special : ActivityKey.Special.values())
		{
			ActivityKey key = ActivityKey.special(special);
			ACTIVITIES.put(encode(key), key);
		}
		for (Quest quest : Quest.values())
		{
			QuestKey key = QuestKey.api(quest);
			QUESTS.put(encode(key), key);
		}
		for (QuestKey.Extra quest : QuestKey.Extra.values())
		{
			QuestKey key = QuestKey.extra(quest);
			QUESTS.put(encode(key), key);
		}
	}

	public static String encode(QuestKey key)
	{
		return key.getApi() != null ? key.getApi().name() : key.getExtra().getToken();
	}

	public static String encode(ActivityKey key)
	{
		if (key.getDiary() != null)
		{
			DiaryKey d = key.getDiary();
			return DIARY + d.getRegion().getToken() + "_" + d.getTier().getToken() + SEPARATOR
				+ d.getAction().getToken();
		}
		return key.getBalloon() != null ? BALLOON + key.getBalloon().getToken() : key.getSpecial().getToken();
	}

	public static ActivityKey activity(String token)
	{
		return ACTIVITIES.get(token);
	}

	public static QuestKey quest(String token)
	{
		return QUESTS.get(token);
	}

	public static Map<QuestKey, AccountProgress.QuestStatus> quests(Map<String, AccountProgress.QuestStatus> stored)
	{
		Map<QuestKey, AccountProgress.QuestStatus> result = new HashMap<>();
		stored.forEach((token, value) ->
		{
			QuestKey key = quest(token);
			if (key != null && value != null)
			{
				result.put(key, value);
			}
		});
		return Collections.unmodifiableMap(result);
	}

	public static Map<ActivityKey, Boolean> activities(Map<String, Boolean> stored)
	{
		Map<ActivityKey, Boolean> result = new HashMap<>();
		stored.forEach((token, value) ->
		{
			ActivityKey key = activity(token);
			if (key != null && value != null)
			{
				result.put(key, value);
			}
		});
		return Collections.unmodifiableMap(result);
	}

	public static String row(com.questcape.guide.GuideRow.Kind kind, String quest, boolean start, String wiki,
		String title)
	{
		return ROW_KINDS.get(Objects.requireNonNull(kind)) + SEPARATOR
			+ (quest == null
				? com.questcape.guide.GuideParser.digest(com.questcape.guide.SourceTextNormalizer.normalize(wiki) + "|"
					+ com.questcape.guide.SourceTextNormalizer.normalize(title))
				: quest + (start ? START : FINISH));
	}

	public static String repeated(String key, int occurrence)
	{
		return key + REPEATED + occurrence;
	}

	public static boolean repeated(String key)
	{
		return key.contains(REPEATED);
	}

	private LegacyKeyCodec()
	{
	}
}
