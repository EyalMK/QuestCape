package com.questcape.ui.style;

import com.questcape.guide.model.GuideRow;
import com.questcape.progress.evaluation.Completion;
import java.awt.Color;
import java.util.Map;

public final class UiTheme
{
	public static final Color BACKGROUND = new Color(0x171C23), CARD = new Color(0x232B35), TEXT = new Color(0xECF1F7),
		MUTED = new Color(0xA5B3C4), GREEN = new Color(0x75D9A2), BLUE = new Color(0x84BFFF),
		AMBER = new Color(0xF2C479), BORDER = new Color(0x374555), GOLD = new Color(0xF2CC60),
		PURPLE = new Color(0xBC9CE8), TEAL = new Color(0x64C7C2), ORANGE = new Color(0xE6A36A),
		QUIET_BORDER = new Color(0x2E3946), TOOLTIP_BORDER = new Color(0x29333F), COMPLETE_CARD = new Color(0x21372F);
	private static final Map<GuideRow.Kind, Color> TYPES = Map.of(GuideRow.Kind.UNLOCK, PURPLE, GuideRow.Kind.UNKNOWN,
		MUTED,
		GuideRow.Kind.DIARY, GREEN, GuideRow.Kind.TRAINING, TEAL, GuideRow.Kind.ACTIVITY, ORANGE);

	public static Color kindColor(GuideRow.Kind kind)
	{
		return TYPES.getOrDefault(kind, BORDER);
	}

	public static Color stateColor(Completion.State state)
	{
		return state == Completion.State.COMPLETE ? GREEN : state == Completion.State.IN_PROGRESS ? GOLD : MUTED;
	}

	private UiTheme()
	{
	}
}
