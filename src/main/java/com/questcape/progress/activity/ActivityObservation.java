package com.questcape.progress.activity;

import com.questcape.progress.*;
import java.util.*;
import java.util.function.*;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
public class ActivityObservation
{
	Map<ActivityKey, Boolean> facts;
	Map<QuestKey, AccountProgress.QuestStatus> quests;

	public ActivityObservation(Map<ActivityKey, Boolean> facts, Map<QuestKey, AccountProgress.QuestStatus> quests)
	{
		this.facts = Collections.unmodifiableMap(new HashMap<>(facts));
		this.quests = Collections.unmodifiableMap(new HashMap<>(quests));
	}

	@Slf4j
	public static class Builder
	{
		private final Map<ActivityKey, Boolean> facts = new HashMap<>();
		private final Map<QuestKey, AccountProgress.QuestStatus> quests = new HashMap<>();

		public Builder fact(ActivityKey key, BooleanSupplier read)
		{
			try
			{
				facts.put(key, read.getAsBoolean());
			}
			catch (RuntimeException failure)
			{
				log.debug("Activity observation unavailable ({})", failure.getClass().getSimpleName());
			}
			return this;
		}

		public Builder quest(QuestKey key, Supplier<AccountProgress.QuestStatus> read)
		{
			try
			{
				AccountProgress.QuestStatus value = read.get();
				if (value != null)
				{
					quests.put(key, value);
				}
			}
			catch (RuntimeException failure)
			{
				log.debug("Miniquest observation unavailable ({})", failure.getClass().getSimpleName());
			}
			return this;
		}

		public ActivityObservation build()
		{
			return new ActivityObservation(facts, quests);
		}
	}
}
