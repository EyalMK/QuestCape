package com.questcape.progress.activity;

import lombok.Value;

/** A small checked union, not a display string or a serialized strategy. */
@Value
public class ActivityKey
{
	public enum Special
	{
		STRONGHOLD_COINS("UNLOCK:STRONGHOLD_COINS"), MUSEUM_CAMP_REWARD("UNLOCK:MUSEUM_CAMP_REWARD"),
		HERBI_FLAX("HAND_IN:HERBI_FLAX");

		@lombok.Getter
		private final String token;

		Special(String token)
		{
			this.token = token;
		}
	}

	DiaryKey diary;
	BalloonDestination balloon;
	Special special;

	private ActivityKey(DiaryKey diary, BalloonDestination balloon, Special special)
	{
		this.diary = diary;
		this.balloon = balloon;
		this.special = special;
	}

	public static ActivityKey diary(DiaryKey key)
	{
		return new ActivityKey(java.util.Objects.requireNonNull(key), null, null);
	}

	public static ActivityKey balloon(BalloonDestination destination)
	{
		return new ActivityKey(null, java.util.Objects.requireNonNull(destination), null);
	}

	public static ActivityKey special(Special key)
	{
		return new ActivityKey(null, null, java.util.Objects.requireNonNull(key));
	}
}
