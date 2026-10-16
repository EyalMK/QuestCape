package com.questcape.progress.activity.detectors;

import com.questcape.progress.activity.ActivityDetector;
import com.questcape.progress.activity.ActivityObservation;
import com.questcape.progress.activity.definitions.GamePredicates;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.model.QuestKey;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/** Verified stage boundaries; see docs/activity-detection.md. */
public final class NaturalHistoryQuizDetector implements ActivityDetector
{
	private static final QuestKey KEY = QuestKey.extra(QuestKey.Extra.NATURAL_HISTORY_QUIZ);

	public Set<QuestKey> quests()
	{
		return Set.of(KEY);
	}

	public ActivityObservation capture(Client client)
	{
		return new ActivityObservation.Builder().quest(KEY, () ->
		{
			return client.getVarbitValue(VarbitID.VM_NATHIS_REWARD_GIVEN) == GamePredicates.SET
				? AccountProgress.QuestStatus.COMPLETE
				: client.getVarbitValue(VarbitID.VM_NATHIS_AGREED) == GamePredicates.SET
					? AccountProgress.QuestStatus.IN_PROGRESS
					: AccountProgress.QuestStatus.INCOMPLETE;
		}).build();
	}
}
