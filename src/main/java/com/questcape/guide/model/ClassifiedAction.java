package com.questcape.guide.model;

import com.questcape.guide.interpretation.TrainingInstruction;
import com.questcape.progress.activity.definitions.ActivityKey;
import com.questcape.progress.evaluation.CompletionPolicy;
import com.questcape.progress.model.QuestKey;
import java.util.*;
import lombok.Value;

/** Prepared domain meaning. No title parsing or mutable services are allowed here. */
@Value
public class ClassifiedAction
{
	public enum Outcome
	{
		MATCHED, UNSUPPORTED, AMBIGUOUS
	}

	public enum Reason
	{
		NONE, UNSUPPORTED_ACTION, UNSUPPORTED_TRAINING, CONFLICTING_IDENTITY, MISSING_OBSERVATION
	}

	Outcome outcome;
	Reason reason;
	CompletionPolicy policy;
	QuestKey quest;
	ActivityKey activity;
	TrainingInstruction training;

	public ClassifiedAction(Outcome outcome, Reason reason, CompletionPolicy policy, QuestKey quest,
		ActivityKey activity, TrainingInstruction training)
	{
		if ((policy == CompletionPolicy.QUEST_START || policy == CompletionPolicy.QUEST_FINISH) && quest == null
			|| quest != null && policy != CompletionPolicy.QUEST_START && policy != CompletionPolicy.QUEST_FINISH
				&& policy != CompletionPolicy.UNKNOWN
			|| (policy == CompletionPolicy.ACTIVITY) != (activity != null))
		{
			throw new IllegalArgumentException("Incompatible action operands");
		}
		this.outcome = Objects.requireNonNull(outcome);
		this.reason = Objects.requireNonNull(reason);
		this.policy = Objects.requireNonNull(policy);
		this.quest = quest;
		this.activity = activity;
		this.training = Objects.requireNonNull(training);
	}

	public static ClassifiedAction unknown(Reason reason)
	{
		return new ClassifiedAction(reason == Reason.CONFLICTING_IDENTITY ? Outcome.AMBIGUOUS : Outcome.UNSUPPORTED,
			reason, CompletionPolicy.UNKNOWN, null, null, TrainingInstruction.empty());
	}

	public boolean manual()
	{
		return policy == CompletionPolicy.MANUAL_KUDOS;
	}
}
