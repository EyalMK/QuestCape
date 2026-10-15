package com.questcape.progress.activity;

import lombok.Value;

/** Explicit region, tier and requested diary fact; wire spellings live in definitions. */
@Value
public class DiaryKey
{
	public enum Region
	{
		ARDOUGNE("ARDOUGNE", "ardougne"),
		DESERT("DESERT", "desert"),
		FALADOR("FALADOR", "falador"),
		FREMENNIK("FREMENNIK", "fremennik"),
		KANDARIN("KANDARIN", "kandarin"),
		KARAMJA("KARAMJA", "karamja"),
		KOUREND("KOUREND", "kourend"),
		LUMBRIDGE("LUMBRIDGE", "lumbridge"),
		MORYTANIA("MORYTANIA", "morytania"),
		VARROCK("VARROCK", "varrock"),
		WESTERN("WESTERN", "western"),
		WILDERNESS("WILDERNESS", "wilderness");

		@lombok.Getter
		private final String token, source;

		Region(String token, String source)
		{
			this.token = token;
			this.source = source;
		}
	}

	public enum Tier
	{
		EASY("EASY", "easy"), MEDIUM("MEDIUM", "medium"), HARD("HARD", "hard"), ELITE("ELITE", "elite");

		@lombok.Getter
		private final String token, source;

		Tier(String token, String source)
		{
			this.token = token;
			this.source = source;
		}
	}

	public enum Action
	{
		TASKS("complete"), REWARD("reward");

		@lombok.Getter
		private final String token;

		Action(String token)
		{
			this.token = token;
		}
	}

	Region region;
	Tier tier;
	Action action;
}
