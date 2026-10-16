package com.questcape.progress.model;

import lombok.Value;
import net.runelite.api.Quest;

@Value
public class QuestKey
{
	public enum Extra
	{
		NATURAL_HISTORY_QUIZ("NATURAL_HISTORY_QUIZ"), KNIGHT_WAVES("KNIGHT_WAVES_TRAINING_GROUNDS");

		@lombok.Getter
		private final String token;

		Extra(String token)
		{
			this.token = token;
		}
	}

	Quest api;
	Extra extra;

	private QuestKey(Quest api, Extra extra)
	{
		this.api = api;
		this.extra = extra;
	}

	public static QuestKey api(Quest quest)
	{
		return new QuestKey(java.util.Objects.requireNonNull(quest), null);
	}

	public static QuestKey extra(Extra quest)
	{
		return new QuestKey(null, java.util.Objects.requireNonNull(quest));
	}
}
