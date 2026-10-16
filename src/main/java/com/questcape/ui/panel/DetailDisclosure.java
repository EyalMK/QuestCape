package com.questcape.ui.panel;

import com.questcape.ui.interaction.InteractionRole;
import com.questcape.ui.style.UiMetrics;
import javax.accessibility.*;
import javax.swing.*;

import static com.questcape.ui.style.UiComponents.*;

/** Independent subsection state lives with its content, surviving parent folds/live renders. */
final class DetailDisclosure extends JPanel
{
	private final JButton toggle;
	private final JPanel content;
	private final String title;

	DetailDisclosure(String title, JPanel content, boolean open, Runnable reflow)
	{
		this.title = title;
		this.content = content;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setAlignmentX(LEFT_ALIGNMENT);
		InteractionRole.readOnly(this);
		toggle = textButton(title);
		toggle.setFont(font(UiMetrics.META, true));
		toggle.addActionListener(e ->
		{
			reflow.run();
			expanded(!content.isVisible());
		});
		add(toggle);
		add(content);
		expanded(open);
	}

	private void expanded(boolean open)
	{
		content.setVisible(open);
		toggle.setText(title + (open ? " ▴" : " ▾"));
		toggle.getAccessibleContext().setAccessibleDescription(open ? "Expanded" : "Collapsed");
		revalidate();
		repaint();
	}

	@Override
	public AccessibleContext getAccessibleContext()
	{
		if (accessibleContext == null)
		{
			accessibleContext = new AccessibleDisclosure();
		}
		return accessibleContext;
	}

	private final class AccessibleDisclosure extends AccessibleJPanel
	{
		public AccessibleStateSet getAccessibleStateSet()
		{
			AccessibleStateSet states = super.getAccessibleStateSet();
			states.add(content != null && content.isVisible() ? AccessibleState.EXPANDED : AccessibleState.COLLAPSED);
			return states;
		}
	}
}
