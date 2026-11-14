package com.questcape.progress.model;

import com.questcape.guide.interpretation.SourceTextNormalizer;
import java.util.Objects;
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
	String gameTitle;

	private QuestKey(Quest api, Extra extra, String gameTitle)
	{
		this.api = api;
		this.extra = extra;
		this.gameTitle = gameTitle;
	}

	public static QuestKey api(Quest quest)
	{
		return new QuestKey(Objects.requireNonNull(quest), null, null);
	}

	public static QuestKey extra(Extra quest)
	{
		return new QuestKey(null, Objects.requireNonNull(quest), null);
	}

	public static QuestKey game(String title)
	{
		String normalized = SourceTextNormalizer.normalize(title);
		return normalized.isBlank() ? null : new QuestKey(null, null, normalized);
	}
}
