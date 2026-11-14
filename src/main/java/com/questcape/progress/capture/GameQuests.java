package com.questcape.progress.capture;

import com.questcape.persistence.LegacyKeyCodec;
import com.questcape.progress.model.AccountProgress.QuestStatus;
import com.questcape.progress.model.QuestKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.gameval.DBTableID;

/** ClientThread only. The index contains game data shared across accounts, never their progress. */
final class GameQuests
{
	private static final int IN_PROGRESS = 0, NOT_STARTED = 1, FINISHED = 2;
	private Map<String, Integer> lookups;

	void capture(Client client, Map<String, QuestStatus> quests)
	{
		if (lookups == null)
		{
			List<Integer> rows = client.getDBTableRows(DBTableID.Quest.ID);
			if (rows == null || rows.isEmpty())
			{
				return;
			}
			lookups = index(client, rows);
		}
		lookups.forEach((token, rowId) ->
		{
			client.runScript(ScriptID.QUEST_STATUS_GET, rowId);
			if (client.getIntStackSize() == 1)
			{
				QuestStatus status = status(client.getIntStack()[0]);
				if (status != null)
				{
					quests.put(token, status);
				}
			}
		});
	}

	private static Map<String, Integer> index(Client client, List<Integer> rows)
	{
		Map<String, Integer> candidates = new HashMap<>();
		Set<String> ambiguous = new HashSet<>();
		for (int row : rows)
		{
			Object[] names = client.getDBTableField(row, DBTableID.Quest.COL_DISPLAYNAME, 0);
			if (names == null || names.length != 1 || !(names[0] instanceof String))
			{
				continue;
			}
			QuestKey key = QuestKey.game((String) names[0]);
			if (key == null)
			{
				continue;
			}
			String token = LegacyKeyCodec.encode(key);
			if (LegacyKeyCodec.quest(token).getApi() != null)
			{
				continue;
			}
			Integer previous = candidates.putIfAbsent(token, row);
			if (previous != null && previous != row)
			{
				ambiguous.add(token);
			}
		}
		ambiguous.forEach(candidates::remove);
		candidates.values().removeIf(row ->
		{
			Integer start = intField(client, row, DBTableID.Quest.COL_UNSTARTEDSTATE);
			Integer end = intField(client, row, DBTableID.Quest.COL_ENDSTATE);
			return start == null || start < 0 || end == null || end <= start;
		});
		return candidates;
	}

	private static Integer intField(Client client, int row, int column)
	{
		Object[] values = client.getDBTableField(row, column, 0);
		return values != null && values.length == 1 && values[0] instanceof Integer ? (Integer) values[0] : null;
	}

	private static QuestStatus status(int value)
	{
		switch (value)
		{
			case IN_PROGRESS:
				return QuestStatus.IN_PROGRESS;
			case NOT_STARTED:
				return QuestStatus.INCOMPLETE;
			case FINISHED:
				return QuestStatus.COMPLETE;
			default:
				return null;
		}
	}
}
