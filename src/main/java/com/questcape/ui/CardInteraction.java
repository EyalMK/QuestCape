package com.questcape.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Ctrl enables native text gestures locally; ordinary clicks disclose immediately. No global key hook. */
final class CardInteraction
{
	private final JComponent card;
	private final Runnable toggle, intent;
	private final java.util.List<JTextComponent> texts = new ArrayList<>();
	private boolean controlDown, disposed;

	CardInteraction(JComponent card, Runnable toggle, Runnable intent)
	{
		this.card = card;
		this.toggle = toggle;
		this.intent = intent;
		card.addKeyListener(modifiers());
		card.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),
			UiCopy.EXIT_SELECTION_ACTION);
		card.getActionMap().put(UiCopy.EXIT_SELECTION_ACTION, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				endSelection();
				card.requestFocusInWindow();
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
			component.addKeyListener(modifiers());
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
		if (component instanceof JTextComponent)
		{
			component.addKeyListener(modifiers());
		}
		component.setCursor(Cursor.getPredefinedCursor(readOnly ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
		if (!readOnly)
		{
			if (component instanceof JTextComponent)
			{
				JTextComponent text = (JTextComponent)component;
				texts.add(text);
				text.setFocusable(false);
				if (text.getToolTipText() == null)
				{
					text.setToolTipText(UiCopy.SELECT_HINT);
				}
				text.addFocusListener(new FocusAdapter()
				{
					public void focusLost(FocusEvent event)
					{
						clear(text);
						Component next = event.getOppositeComponent();
						if (next == null || !SwingUtilities.isDescendingFrom(next, card))
						{
							endSelection();
						}
					}
				});
			}
			MouseAdapter pointer = new MouseAdapter()
			{
				private boolean dragged;
				private boolean selectingGesture;

				public void mousePressed(MouseEvent e)
				{
					if (disposed)
					{
						e.consume();
						return;
					}
					dragged = false;
					selectingGesture = e.isControlDown() && SwingUtilities.isLeftMouseButton(e);
					control(e.isControlDown());
					if (component instanceof JTextComponent && SwingUtilities.isLeftMouseButton(e))
					{
						if (selectingGesture)
						{
							component.setFocusable(true);
						}
						else
						{
							endSelection();
							e.consume();
						}
					}
					if (!disposed)
					{
						intent.run();
					}
				}

				public void mouseDragged(MouseEvent e)
				{
					dragged = true;
					if (disposed || component instanceof JTextComponent && !selectingGesture)
					{
						e.consume();
					}
				}

				public void mouseReleased(MouseEvent e)
				{
					if (disposed || component instanceof JTextComponent && !selectingGesture
						&& SwingUtilities.isLeftMouseButton(e))
					{
						e.consume();
					}
				}

				public void mouseMoved(MouseEvent e)
				{
					control(e.isControlDown());
				}

				public void mouseEntered(MouseEvent e)
				{
					control(e.isControlDown());
				}

				public void mouseClicked(MouseEvent e)
				{
					if (disposed)
					{
						e.consume();
						return;
					}
					boolean selecting = selectingGesture || e.isControlDown();
					selectingGesture = false;
					if (!disposed && !selecting && !dragged && !e.isConsumed() && SwingUtilities.isLeftMouseButton(e)
						&& e.getClickCount() == 1)
					{
						toggle.run();
					}
					if (component instanceof JTextComponent && !selecting && SwingUtilities.isLeftMouseButton(e))
					{
						e.consume();
					}
				}
			};
			// Consume ordinary text gestures before the native caret can focus/scroll/select.
			// Ctrl gestures pass through unchanged to the installed RuneLite/Swing text UI.
			MouseListener[] mouse = component.getMouseListeners();
			MouseMotionListener[] motion = component.getMouseMotionListeners();
			for (MouseListener listener : mouse)
			{
				component.removeMouseListener(listener);
			}
			for (MouseMotionListener listener : motion)
			{
				component.removeMouseMotionListener(listener);
			}
			component.addMouseListener(pointer);
			component.addMouseMotionListener(pointer);
			for (MouseListener listener : mouse)
			{
				component.addMouseListener(listener);
			}
			for (MouseMotionListener listener : motion)
			{
				component.addMouseMotionListener(listener);
			}
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container)component).getComponents())
			{
				install(child, readOnly);
			}
		}
	}

	private KeyListener modifiers()
	{
		return new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					control(true);
				}
			}

			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					control(false);
				}
			}
		};
	}

	private void control(boolean value)
	{
		if (disposed || controlDown == value)
		{
			return;
		}
		controlDown = value;
		for (JTextComponent text : texts)
		{
			text.setCursor(Cursor.getPredefinedCursor(value ? Cursor.TEXT_CURSOR : Cursor.HAND_CURSOR));
			if (!value)
			{
				text.getCaret().setVisible(false);
			}
		}
	}

	void endSelection()
	{
		controlDown = false;
		texts.forEach(CardInteraction::clear);
	}

	private static void clear(JTextComponent text)
	{
		text.select(0, 0);
		text.getCaret().setVisible(false);
		text.setFocusable(false);
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	void dispose()
	{
		disposed = true;
		endSelection();
	}
}
