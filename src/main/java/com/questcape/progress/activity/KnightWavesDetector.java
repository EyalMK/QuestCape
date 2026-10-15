package com.questcape.progress.activity;

import com.questcape.progress.*;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/** Verified stage boundaries; see docs/activity-detection.md. */
public final class KnightWavesDetector implements ActivityDetector
{
	private static final QuestKey KEY = QuestKey.extra(QuestKey.Extra.KNIGHT_WAVES);

	public Set<QuestKey> quests()
	{
		return Set.of(KEY);
	}

	public ActivityObservation capture(Client client)
	{
		return new ActivityObservation.Builder().quest(KEY, () ->
		{
			int waves = client.getVarbitValue(VarbitID.KR_KNIGHTWAVES_STATE);
			return waves >= GamePredicates.KNIGHT_WAVES_FINISHED ? AccountProgress.QuestStatus.COMPLETE
				: waves > 0 ? AccountProgress.QuestStatus.IN_PROGRESS : AccountProgress.QuestStatus.INCOMPLETE;
		}).build();
	}
}
