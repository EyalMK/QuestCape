package com.questcape.progress.evaluation;

import com.questcape.guide.model.ClassifiedAction;
import com.questcape.progress.model.AccountProgress;

/** Closed domain policies own evaluation. Persisted key syntax and Swing do not belong here. */
public enum CompletionPolicy
{
	INFORMATION
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			return Completion.State.INFORMATION;
		}
	},
	QUEST_START
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			AccountProgress.QuestStatus value = facts.quest(action.getQuest());
			return value == AccountProgress.QuestStatus.IN_PROGRESS ? Completion.State.COMPLETE : questState(value);
		}
	},
	QUEST_FINISH
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			return questState(facts.quest(action.getQuest()));
		}
	},
	TRAINING
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			return training.getState();
		}
	},
	ACTIVITY
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			Boolean value = facts.fact(action.getActivity());
			return value == null ? Completion.State.UNKNOWN
				: value ? Completion.State.COMPLETE : Completion.State.INCOMPLETE;
		}
	},
	MANUAL_KUDOS
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			return facts.getManual().contains(key) ? Completion.State.COMPLETE : Completion.State.UNKNOWN;
		}
	},
	UNKNOWN
	{
		public Completion.State evaluate(ClassifiedAction action, AccountProgress facts, TrainingEvaluation training,
			String key)
		{
			return Completion.State.UNKNOWN;
		}
	};

	public abstract Completion.State evaluate(ClassifiedAction action, AccountProgress facts,
		TrainingEvaluation training, String manualKey);

	private static Completion.State questState(AccountProgress.QuestStatus status)
	{
		if (status == null)
		{
			return Completion.State.UNKNOWN;
		}
		switch (status)
		{
		case COMPLETE:
			return Completion.State.COMPLETE;
		case IN_PROGRESS:
			return Completion.State.IN_PROGRESS;
		case INCOMPLETE:
			return Completion.State.INCOMPLETE;
		default:
			throw new IllegalArgumentException("Unmapped quest state");
		}
	}
}
