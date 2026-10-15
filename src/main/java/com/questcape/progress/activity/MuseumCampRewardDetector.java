package com.questcape.progress.activity;

import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/** Verified independent predicate; see docs/activity-detection.md. */
public final class MuseumCampRewardDetector implements ActivityDetector
{
	private static final ActivityKey KEY = ActivityKey.special(ActivityKey.Special.MUSEUM_CAMP_REWARD);

	public Set<ActivityKey> facts()
	{
		return Set.of(KEY);
	}

	public ActivityObservation capture(Client client)
	{
		return new ActivityObservation.Builder()
			.fact(KEY, () -> client.getVarbitValue(VarbitID.FOSSIL_TASKREWARDS_CAMP) == GamePredicates.SET).build();
	}
}
