package com.questcape.ui.interaction;

import javax.swing.JComponent;

/** Typed component ownership: read-only content must not inherit parent disclosure. */
public enum InteractionRole
{
	DISCLOSURE, CONTROL, READ_ONLY;

	public static void readOnly(JComponent component)
	{
		component.putClientProperty(InteractionRole.class, READ_ONLY);
	}

	static InteractionRole of(JComponent component)
	{
		return (InteractionRole)component.getClientProperty(InteractionRole.class);
	}
}
