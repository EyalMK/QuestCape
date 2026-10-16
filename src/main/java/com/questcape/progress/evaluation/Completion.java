package com.questcape.progress.evaluation;

import com.questcape.guide.model.GuideRow;
import com.questcape.progress.model.AccountProgress;
import lombok.Value;

@Value
public class Completion
{
	public enum State
	{
		COMPLETE("✓ Complete"), INCOMPLETE("○ Incomplete"), IN_PROGRESS("◐ In progress"), UNKNOWN("? Unknown"),
		INFORMATION("Information");

		private final String label;

		State(String label)
		{
			this.label = label;
		}

		public String label()
		{
			return label;
		}
	}

	State state;
	boolean manual;

	public String label()
	{
		return (state == State.UNKNOWN && manual ? "○ Not checked" : state.label()) + (manual ? " · manual" : "");
	}

	public static Completion of(GuideRow row, AccountProgress progress)
	{
		return of(row, progress, row.getKind() == GuideRow.Kind.TRAINING ? TrainingEvaluation.of(row, progress) : null);
	}

	public static Completion of(GuideRow row, AccountProgress progress, TrainingEvaluation training)
	{
		com.questcape.guide.model.ClassifiedAction action = row.action();
		if (action.getPolicy() == CompletionPolicy.INFORMATION)
		{
			return new Completion(State.INFORMATION, false);
		}
		if (progress == null)
		{
			return new Completion(State.UNKNOWN, false);
		}
		return new Completion(action.getPolicy().evaluate(action, progress, training, row.getKey()), action.manual());
	}
}
