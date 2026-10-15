package com.questcape.ui;

final class UiCopy
{
	static final String EXIT_SELECTION_ACTION = "exitTextSelection",
		SELECT_HINT = "Hold Ctrl to select text. Ctrl+C copies; Escape clears selection.",
		QUEST_POINTS = "Quest points", GUIDE_QP = "Guide QP total", LOCATION = "Location",
		AMBIGUOUS_ACTION = "Supported source identities conflict; no completion is assumed.";
	static final String SOURCE_FIELDS_CLOSED = "Source fields ▾", SOURCE_FIELDS_OPEN = "Source fields ▴";
	static final String UNDEFINED = "—", TRAINING_COMPLETE = "Training complete",
		TRAINING_PROGRESS = "Training progress";
	static final String REAL_LEVEL_ACCESSIBLE = "Real level: ";
	static final String COPY_GESTURE = "Shift + right-click to copy link", LINK_COPIED = "✓ Link copied",
		LINK_COPY_FAILED = "Could not copy link";

	static final String TITLE = "QuestCape", REPORT = "Report issues or contribute", RESYNC = "Resync Progress",
		RESYNC_TOOLTIP = "Resync progress from RuneLite", CLEAR = "Clear search", NO_MATCH = "No matching step",
		QUICK_GUIDE = "Wiki quick guide ↗", RELATED = "Related wiki pages", QUEST_PAGE = "Quest page ↗",
		ADVICE = "Route advice", PROJECTIONS = "Guide projected levels", THEOATRIX = "Theoatrix guide ↗",
		FALLBACK = "Browse Theoatrix guides ↗", DETAILS_OPEN = "Details  ▴", DETAILS_CLOSED = "Details  ▾";
	static final String MISSING_ACCOUNT = "Current progress unavailable; log in and resync.",
		MISSING_OBSERVATION = "Required local observation is unavailable; resync when your character is ready.",
		UNSUPPORTED_ACTION = "This action has no supported completion evidence. Resync cannot determine its meaning.",
		UNSUPPORTED_TRAINING = "Some required training meaning is unsupported; no target is assumed complete.",
		MISSING_SKILL = "Some required real levels are unavailable; missing values are not zero.",
		MANUAL_UNCHECKED = "Eligible manual action has not been checked.",
		INTERVAL_TOOLTIP = "Percentage measures the guide's level interval, not XP or time remaining.";

	private UiCopy()
	{
	}
}
