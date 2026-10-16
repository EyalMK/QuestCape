package com.questcape.progress.activity.detectors;

import com.questcape.progress.activity.ActivityDetector;
import com.questcape.progress.activity.ActivityObservation;
import com.questcape.progress.activity.definitions.ActivityKey;
import com.questcape.progress.activity.definitions.GamePredicates;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/** Verified independent predicate; see docs/activity-detection.md. */
public final class StrongholdCoinsDetector implements ActivityDetector
{
	private static final ActivityKey KEY = ActivityKey.special(ActivityKey.Special.STRONGHOLD_COINS);

	public Set<ActivityKey> facts()
	{
		return Set.of(KEY);
	}

	public ActivityObservation capture(Client client)
	{
		return new ActivityObservation.Builder().fact(KEY,
			() -> client.getVarbitValue(VarbitID.SOS_EMOTE_FLAP) == GamePredicates.SET
				&& client.getVarbitValue(VarbitID.SOS_EMOTE_DOH) == GamePredicates.SET
				&& client.getVarbitValue(VarbitID.SOS_EMOTE_IDEA) == GamePredicates.SET)
			.build();
	}
}
