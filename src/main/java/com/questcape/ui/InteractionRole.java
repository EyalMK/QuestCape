package com.questcape.ui;

import javax.swing.JComponent;

/** Typed component ownership: read-only content must not inherit parent disclosure. */
enum InteractionRole
{
	DISCLOSURE, CONTROL, READ_ONLY;

	static void readOnly(JComponent component)
	{
		component.putClientProperty(InteractionRole.class, READ_ONLY);
	}

	static InteractionRole of(JComponent component)
	{
		return (InteractionRole)component.getClientProperty(InteractionRole.class);
	}
}
