package com.questcape.progress.activity.detectors;

import com.questcape.progress.activity.ActivityDetector;
import com.questcape.progress.activity.ActivityObservation;
import com.questcape.progress.activity.definitions.ActivityKey;
import com.questcape.progress.activity.definitions.BalloonDestination;
import com.questcape.progress.activity.definitions.GamePredicates;
import java.util.*;
import net.runelite.api.Client;

public final class BalloonUnlockDetector implements ActivityDetector
{
	public Set<ActivityKey> facts()
	{
		Set<ActivityKey> keys = new HashSet<>();
		for (BalloonDestination destination : BalloonDestination.values())
		{
			keys.add(ActivityKey.balloon(destination));
		}
		return Collections.unmodifiableSet(keys);
	}

	public ActivityObservation capture(Client client)
	{
		ActivityObservation.Builder result = new ActivityObservation.Builder();
		for (BalloonDestination destination : BalloonDestination.values())
		{
			result.fact(ActivityKey.balloon(destination),
				() -> client.getVarbitValue(destination.getVarbit()) == GamePredicates.SET);
		}
		return result.build();
	}
}
