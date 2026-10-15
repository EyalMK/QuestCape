package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.progress.*;
import java.util.*;
import lombok.Value;
import static com.questcape.ui.UiComponents.human;

/** One pure projection owns whole-route totals, filtered eligibility and card facts. */
@Value
final class GuideProjection
{
	@Value
	static class Entry
	{
		GuideRow row;
		Completion completion;
		TrainingEvaluation training;
		boolean visible;
		String comparison;
		String explanation;
	}

	List<Entry> entries;
	int complete;
	int unknown;

	static GuideProjection of(GuideSnapshot guide, AccountProgress account, GuideRow.Kind type, Completion.State state)
	{
		List<Entry> entries = new ArrayList<>();
		int complete = 0, unknown = 0;
		for (GuideRow row : guide == null ? Collections.<GuideRow>emptyList() : guide.getRows())
		{
			if (!row.isActionable())
			{
				continue;
			}
			TrainingEvaluation training = row.getKind() == GuideRow.Kind.TRAINING ? TrainingEvaluation.of(row, account)
				: null;
			Completion completion = Completion.of(row, account, training);
			if (completion.getState() == Completion.State.COMPLETE)
			{
				complete++;
			}
			if (completion.getState() == Completion.State.UNKNOWN)
			{
				unknown++;
			}
			entries.add(new Entry(row, completion, training,
				(type == null || type == row.getKind()) && (state == null || state == completion.getState()),
				comparison(training), explanation(row, account, completion, training)));
		}
		return new GuideProjection(Collections.unmodifiableList(entries), complete, unknown);
	}

	private static String comparison(TrainingEvaluation result)
	{
		if (result == null)
		{
			return "";
		}
		StringJoiner text = new StringJoiner("\n");
		for (TrainingEvaluation.Comparison item : result.getComparisons())
		{
			String prefix = result.getComparisons().size() > 1 ? human(item.getSkill()) + ": " : "";
			text.add(prefix + UiCopy.CURRENT_LEVEL + (item.getLevel() == null ? UiCopy.UNAVAILABLE : item.getLevel())
				+ (item.getPercent() == null ? "" : " - " + item.getPercent() + "%"));
		}
		return result.getComparisons().isEmpty() ? UiCopy.CURRENT_LEVEL + UiCopy.UNAVAILABLE : text.toString();
	}

	private static String explanation(GuideRow row, AccountProgress account, Completion completion,
		TrainingEvaluation training)
	{
		if (account == null)
		{
			return UiCopy.MISSING_ACCOUNT;
		}
		if (row.action().getReason() == ClassifiedAction.Reason.CONFLICTING_IDENTITY)
		{
			return UiCopy.AMBIGUOUS_ACTION;
		}
		if (training != null && training.isPartial())
		{
			return row.training().isUnresolved() ? UiCopy.UNSUPPORTED_TRAINING : UiCopy.MISSING_SKILL;
		}
		if (completion.getState() != Completion.State.UNKNOWN)
		{
			return "";
		}
		if (row.isManual())
		{
			return UiCopy.MANUAL_UNCHECKED;
		}
		return row.getQuestIdentity() != null || row.getActivityIdentity() != null ? UiCopy.MISSING_OBSERVATION
			: UiCopy.UNSUPPORTED_ACTION;
	}
}
