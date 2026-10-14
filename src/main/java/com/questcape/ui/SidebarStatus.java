package com.questcape.ui;

import com.questcape.persistence.StoredView.SaveState;
import lombok.Value;
import com.questcape.integration.OperationOutcome.State;

/** Presentation consumes typed outcomes, never English message protocols. */
@Value
public class SidebarStatus
{
	State guideOperation;
	State progressOperation;
	SaveState guideSave;
	SaveState progressSave;
	String guideDetail;
	String progressDetail;

	public static SidebarStatus idle(String guide, String progress)
	{
		return new SidebarStatus(State.IDLE, State.IDLE, SaveState.SAVED, SaveState.SAVED, guide, progress);
	}
}
