package com.optimalquestguide.progress;

import com.optimalquestguide.guide.GuideRow;
import lombok.Value;

@Value
public class Completion
{
    public enum State { COMPLETE, INCOMPLETE, IN_PROGRESS, UNKNOWN, INFORMATION }
    State state;
    boolean manual;
    public String label()
    {
        String label;
        switch (state)
        {
            case COMPLETE: label = "✓ Complete"; break;
            case IN_PROGRESS: label = "◐ In progress"; break;
            case INCOMPLETE: label = "○ Incomplete"; break;
            case INFORMATION: label = "Information"; break;
            default: label = manual ? "○ Not checked" : "? Unknown";
        }
        return label + (manual ? " · manual" : "");
    }
    public static Completion of(GuideRow row, AccountProgress progress)
    {
        if (!row.isActionable()) return new Completion(State.INFORMATION, false);
        if (progress == null) return new Completion(State.UNKNOWN, false);
        if (row.getQuestIdentity() != null)
        {
            AccountProgress.QuestStatus state = progress.getQuests().get(row.getQuestIdentity());
            if (row.getKey().contains(":start") && state == AccountProgress.QuestStatus.IN_PROGRESS)
                return new Completion(State.COMPLETE, false);
            return new Completion(state == null ? State.UNKNOWN : State.valueOf(state.name()), false);
        }
        if (row.getKind() == GuideRow.Kind.TRAINING)
        {
            boolean missing = row.getTargets().isEmpty();
            for (java.util.Map.Entry<String, Integer> target : row.getTargets().entrySet())
            {
                Integer level = progress.getLevels().get(target.getKey());
                if (level == null) missing = true;
                else if (level < target.getValue()) return new Completion(State.INCOMPLETE, false);
            }
            return new Completion(missing ? State.UNKNOWN : State.COMPLETE, false);
        }
        Boolean fact = progress.getActivities().get(row.getKey());
        if (fact != null) return new Completion(fact ? State.COMPLETE : State.INCOMPLETE, false);
        if (row.isManual()) return new Completion(progress.getManual().contains(row.getKey()) ? State.COMPLETE : State.UNKNOWN, true);
        return new Completion(State.UNKNOWN, false);
    }
}
