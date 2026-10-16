package com.questcape.progress.activity.detectors;

import com.questcape.progress.activity.ActivityDetector;
import com.questcape.progress.activity.ActivityObservation;
import com.questcape.progress.activity.definitions.ActivityKey;
import com.questcape.progress.activity.definitions.GamePredicates;
import java.util.Set;
import net.runelite.api.Client;

/** Verified independent predicate; see docs/activity-detection.md. */
public final class HerbiFlaxHandInDetector implements ActivityDetector
{
	private static final ActivityKey KEY = ActivityKey.special(ActivityKey.Special.HERBI_FLAX);

	public Set<ActivityKey> facts()
	{
		return Set.of(KEY);
	}

	public ActivityObservation capture(Client client)
	{
		return new ActivityObservation.Builder()
			.fact(KEY, () -> (client.getVarpValue(net.runelite.api.gameval.VarPlayerID.MORTTONMULTI)
				& (1 << GamePredicates.HERBI_FLAX_HAND_IN_BIT)) != 0)
			.build();
	}
}
