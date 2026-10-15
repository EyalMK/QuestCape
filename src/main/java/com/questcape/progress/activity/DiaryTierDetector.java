package com.questcape.progress.activity;

import java.util.*;
import net.runelite.api.Client;

/** Task and reward facts are independent, including the first three Karamja tier encodings. */
public final class DiaryTierDetector implements ActivityDetector
{
	public Set<ActivityKey> facts()
	{
		Set<ActivityKey> keys = new HashSet<>();
		DiaryDefinitions.ALL.forEach(d ->
		{
			for (DiaryKey.Action action : DiaryKey.Action.values())
			{
				keys.add(ActivityKey.diary(d.key(action)));
			}
		});
		return Collections.unmodifiableSet(keys);
	}

	public ActivityObservation capture(Client client)
	{
		ActivityObservation.Builder result = new ActivityObservation.Builder();
		DiaryDefinitions.ALL.forEach(d -> result
			.fact(ActivityKey.diary(d.key(DiaryKey.Action.TASKS)),
				() -> client.getVarbitValue(d.getTasks()) == d.getExpected())
			.fact(ActivityKey.diary(d.key(DiaryKey.Action.REWARD)),
				() -> client.getVarbitValue(d.getReward()) == GamePredicates.SET));
		return result.build();
	}
}
