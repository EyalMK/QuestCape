package com.questcape.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Isolated popup/timer fixtures only; no native window, clock sleep, game input or clipboard. */
public class SelectionHintTest
{
	@Test
	public void waitsOneSecondWithoutChangingSharedTimingAndCancelsOnExitClickOrDisposal() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			int originalDelay = ToolTipManager.sharedInstance().getInitialDelay();
			PopupFactory original = PopupFactory.getSharedInstance();
			AtomicInteger shown = new AtomicInteger(), hidden = new AtomicInteger();
			AtomicBoolean showing = new AtomicBoolean(true);
			GraphicsConfiguration screen = mock(GraphicsConfiguration.class);
			when(screen.getBounds()).thenReturn(new Rectangle(0, 0, 800, 600));
			JPanel card = new JPanel()
			{
				public boolean isShowing()
				{
					return true;
				}
			};
			JPanel target = new JPanel()
			{
				public boolean isShowing()
				{
					return showing.get();
				}

				public Point getLocationOnScreen()
				{
					return new Point(750, 580);
				}

				public GraphicsConfiguration getGraphicsConfiguration()
				{
					return screen;
				}
			};
			SelectionHint hint = new SelectionHint(card);
			try
			{
				PopupFactory.setSharedInstance(new PopupFactory()
				{
					public Popup getPopup(Component owner, Component contents, int x, int y)
					{
						assertSame(target, owner);
						assertEquals(UiCopy.SELECT_HINT, ((JToolTip)contents).getTipText());
						assertFalse(((JToolTip)contents).getTipText().contains("Escape"));
						assertTrue(x >= 0 && x + contents.getPreferredSize().width <= 800);
						assertTrue(y >= 0 && y + contents.getPreferredSize().height <= 600);
						return new Popup()
						{
							public void show()
							{
								shown.incrementAndGet();
							}

							public void hide()
							{
								hidden.incrementAndGet();
							}
						};
					}
				});
				hint.install(target);
				assertEquals(1000, hint.delay.getInitialDelay());
				assertFalse(hint.delay.isRepeats());
				event(target, MouseEvent.MOUSE_ENTERED);
				assertTrue(hint.delay.isRunning());
				assertEquals(0, shown.get());
				// Drive the scheduled expiry deterministically; no elapsed-time or OS interaction.
				for (ActionListener callback : hint.delay.getActionListeners())
				{
					callback.actionPerformed(null);
				}
				assertEquals(1, shown.get());
				event(target, MouseEvent.MOUSE_EXITED);
				assertEquals(1, hidden.get());
				assertFalse(hint.delay.isRunning());
				event(target, MouseEvent.MOUSE_ENTERED);
				for (ActionListener callback : hint.delay.getActionListeners())
				{
					callback.actionPerformed(null);
				}
				assertEquals(2, shown.get());
				showing.set(false);
				target.dispatchEvent(new HierarchyEvent(target, HierarchyEvent.HIERARCHY_CHANGED, target, card,
					HierarchyEvent.SHOWING_CHANGED));
				assertEquals(2, hidden.get());
				assertFalse(hint.delay.isRunning());
				showing.set(true);
				event(target, MouseEvent.MOUSE_ENTERED);
				event(target, MouseEvent.MOUSE_PRESSED);
				assertFalse(hint.delay.isRunning());
				event(target, MouseEvent.MOUSE_ENTERED);
				hint.dispose();
				event(target, MouseEvent.MOUSE_ENTERED);
				for (ActionListener callback : hint.delay.getActionListeners())
				{
					callback.actionPerformed(null);
				}
				assertFalse(hint.delay.isRunning());
				assertEquals(2, shown.get());
				assertEquals(originalDelay, ToolTipManager.sharedInstance().getInitialDelay());
				assertNull(target.getToolTipText());
			}
			finally
			{
				hint.dispose();
				PopupFactory.setSharedInstance(original);
			}
		});
	}

	private static void event(JComponent target, int id)
	{
		target.dispatchEvent(new MouseEvent(target, id, 1, 0, 2, 2, 1, false,
			id == MouseEvent.MOUSE_PRESSED ? MouseEvent.BUTTON1 : MouseEvent.NOBUTTON));
	}
}
