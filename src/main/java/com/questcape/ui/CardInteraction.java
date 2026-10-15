package com.questcape.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import static com.questcape.ui.UiComponents.textButton;

/** Immediate card disclosure and explicit native text selection; owns no timers or game input. */
final class CardInteraction
{
	final JButton select = textButton(UiCopy.SELECT_TEXT);
	private final JComponent card;
	private final Runnable toggle, intent;
	private final java.util.List<JTextComponent> texts = new ArrayList<>();
	private boolean selecting, disposed;

	CardInteraction(JComponent card, Runnable toggle, Runnable intent)
	{
		this.card = card;
		this.toggle = toggle;
		this.intent = intent;
		select.addActionListener(e -> selection(!selecting));
		card.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),
			UiCopy.EXIT_SELECTION_ACTION);
		card.getActionMap().put(UiCopy.EXIT_SELECTION_ACTION, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (selecting)
				{
					selection(false);
					select.requestFocusInWindow();
				}
			}
		});
	}

	void install()
	{
		install(card, false);
	}

	private void install(Component component, boolean inheritedReadOnly)
	{
		InteractionRole role = component instanceof AbstractButton ? InteractionRole.CONTROL
			: component instanceof JComponent ? InteractionRole.of((JComponent)component) : null;
		if (role == InteractionRole.CONTROL)
		{
			((AbstractButton)component).addActionListener(e ->
			{
				if (!disposed)
				{
					intent.run();
				}
			});
			return;
		}
		boolean readOnly = inheritedReadOnly || role == InteractionRole.READ_ONLY;
		component.setCursor(Cursor.getPredefinedCursor(readOnly ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
		if (!readOnly)
		{
			if (component instanceof JTextComponent)
			{
				JTextComponent text = (JTextComponent)component;
				texts.add(text);
				text.setFocusable(false);
			}
			MouseAdapter pointer = new MouseAdapter()
			{
				private boolean dragged;

				public void mousePressed(MouseEvent e)
				{
					dragged = false;
					if (!disposed)
					{
						intent.run();
					}
				}

				public void mouseDragged(MouseEvent e)
				{
					dragged = true;
				}

				public void mouseClicked(MouseEvent e)
				{
					if (!disposed && !selecting && !dragged && !e.isConsumed() && SwingUtilities.isLeftMouseButton(e)
						&& e.getClickCount() == 1)
					{
						toggle.run();
					}
				}
			};
			component.addMouseListener(pointer);
			component.addMouseMotionListener(pointer);
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container)component).getComponents())
			{
				install(child, readOnly);
			}
		}
	}

	private void selection(boolean value)
	{
		if (disposed)
		{
			return;
		}
		intent.run();
		selecting = value;
		select.setText(value ? UiCopy.DONE_SELECTING : UiCopy.SELECT_TEXT);
		select.getAccessibleContext()
			.setAccessibleDescription(value ? UiCopy.SELECTION_ACTIVE : UiCopy.SELECTION_INACTIVE);
		for (JTextComponent text : texts)
		{
			text.setCursor(Cursor.getPredefinedCursor(value ? Cursor.TEXT_CURSOR : Cursor.HAND_CURSOR));
			text.setFocusable(value);
			if (!value)
			{
				text.select(0, 0);
			}
		}
		if (value)
		{
			texts.stream().filter(Component::isShowing).findFirst().ifPresent(Component::requestFocusInWindow);
		}
	}

	void endSelection()
	{
		if (selecting)
		{
			selection(false);
		}
	}

	void dispose()
	{
		disposed = true;
	}
}
