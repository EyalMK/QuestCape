package com.questcape.progress;

import com.questcape.guide.GuideRow;
import lombok.Value;

@Value
public class Completion
{
	public enum State
	{
		COMPLETE, INCOMPLETE, IN_PROGRESS, UNKNOWN, INFORMATION
	}

	State state;
	boolean manual;

	public String label()
	{
		String label;
		switch (state)
		{
		case COMPLETE:
			label = "✓ Complete";
			break;
		case IN_PROGRESS:
			label = "◐ In progress";
			break;
		case INCOMPLETE:
			label = "○ Incomplete";
			break;
		case INFORMATION:
			label = "Information";
			break;
		default:
			label = manual ? "○ Not checked" : "? Unknown";
		}
		return label + (manual ? " · manual" : "");
	}

	public static Completion of(GuideRow row, AccountProgress progress)
	{
		return of(row, progress, row.getKind() == GuideRow.Kind.TRAINING ? TrainingEvaluation.of(row, progress) : null);
	}

	public static Completion of(GuideRow row, AccountProgress progress, TrainingEvaluation training)
	{
		if (!row.isActionable())
		{
			return new Completion(State.INFORMATION, false);
		}
		if (progress == null)
		{
			return new Completion(State.UNKNOWN, false);
		}
		if (row.getQuestIdentity() != null)
		{
			AccountProgress.QuestStatus state = progress.getQuests().get(row.getQuestIdentity());
			if (row.getKey().contains(":start") && state == AccountProgress.QuestStatus.IN_PROGRESS)
			{
				return new Completion(State.COMPLETE, false);
			}
			return new Completion(state == null ? State.UNKNOWN : State.valueOf(state.name()), false);
		}
		if (row.getKind() == GuideRow.Kind.TRAINING)
		{
			return new Completion(training.getState(), false);
		}
		if (row.getKind() == GuideRow.Kind.DIARY && row.getActivityIdentity() == null
			|| row.getKind() == GuideRow.Kind.MINIQUEST)
		{
			return new Completion(State.UNKNOWN, false);
		}
		Boolean fact = progress.getActivities().get(row.getActivityIdentity() == null
			? row.getKey() : row.getActivityIdentity());
		if (fact != null)
		{
			return new Completion(fact ? State.COMPLETE : State.INCOMPLETE, false);
		}
		if (row.isManual())
		{
			return new Completion(progress.getManual().contains(row.getKey()) ? State.COMPLETE : State.UNKNOWN, true);
		}
		return new Completion(State.UNKNOWN, false);
	}

}
