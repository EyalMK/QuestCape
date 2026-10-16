package com.questcape.ui.interaction;

import com.questcape.ui.GuideActions;
import com.questcape.ui.style.UiCopy;
import com.questcape.ui.style.UiMetrics;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;

import static com.questcape.ui.style.UiComponents.*;
import static com.questcape.ui.style.UiTheme.*;

/** Shift/right-click copies a resolved link and shows local feedback without changing layout. */
public final class LinkCopyGesture extends MouseAdapter
{
	private enum Property
	{
		GESTURE
	}

	private final JButton button;
	private final Supplier<String> destination;
	private final Predicate<String> copy;
	private Timer timer;
	private Popup popup;
	private boolean disposed;
	private boolean armed;

	private LinkCopyGesture(JButton button, Supplier<String> destination, Predicate<String> copy)
	{
		this.button = button;
		this.destination = destination;
		this.copy = copy;
		button.addMouseListener(this);
		button.addMouseMotionListener(this);
		button.putClientProperty(Property.GESTURE, this);
		button.getAccessibleContext().setAccessibleDescription(UiCopy.COPY_GESTURE);
		button.addHierarchyListener(event ->
		{
			if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !button.isShowing())
			{
				clear();
			}
		});
	}

	public static void install(JButton button, Supplier<String> destination, GuideActions actions)
	{
		new LinkCopyGesture(button, destination, actions::copyLink);
	}

	@Override
	public void mousePressed(MouseEvent event)
	{
		armed = !disposed && button.isEnabled() && event.isShiftDown() && SwingUtilities.isRightMouseButton(event)
			&& !event.isConsumed();
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		armed = false;
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		boolean clicked = armed;
		armed = false;
		if (!clicked || disposed || !button.isEnabled() || !event.isShiftDown()
			|| !SwingUtilities.isRightMouseButton(event)
			|| !button.contains(event.getPoint()) || event.isConsumed())
		{
			return;
		}
		event.consume();
		feedback(copy.test(destination.get()));
	}

	private void feedback(boolean success)
	{
		clear();
		String message = success ? UiCopy.LINK_COPIED : UiCopy.LINK_COPY_FAILED;
		button.getAccessibleContext().setAccessibleDescription(message + ". " + UiCopy.COPY_GESTURE);
		if (button.isShowing())
		{
			JLabel badge = new JLabel(message);
			badge.setFont(font(UiMetrics.TOOLTIP, true));
			badge.setForeground(success ? GREEN : AMBER);
			badge.setBackground(CARD);
			badge.setOpaque(true);
			badge.setBorder(new CompoundBorder(new LineBorder(success ? GREEN : AMBER),
				new EmptyBorder(UiMetrics.COPY_BADGE_VERTICAL, UiMetrics.COPY_BADGE_HORIZONTAL,
					UiMetrics.COPY_BADGE_VERTICAL, UiMetrics.COPY_BADGE_HORIZONTAL)));
			Point at = button.getLocationOnScreen();
			Dimension size = badge.getPreferredSize();
			Rectangle screen = button.getGraphicsConfiguration().getBounds();
			int x = Math.max(screen.x, Math.min(at.x, screen.x + screen.width - size.width));
			int y = at.y + button.getHeight() + UiMetrics.COPY_POPUP_GAP;
			if (y + size.height > screen.y + screen.height)
			{
				y = at.y - size.height - UiMetrics.COPY_POPUP_GAP;
			}
			popup = PopupFactory.getSharedInstance().getPopup(button, badge, x, y);
			popup.show();
		}
		timer = new Timer(UiMetrics.COPY_FEEDBACK_MILLIS, event -> clear());
		timer.setRepeats(false);
		timer.start();
	}

	void clear()
	{
		if (timer != null)
		{
			timer.stop();
			timer = null;
		}
		if (popup != null)
		{
			popup.hide();
			popup = null;
		}
		button.getAccessibleContext().setAccessibleDescription(UiCopy.COPY_GESTURE);
	}

	boolean feedbackActive()
	{
		return timer != null && timer.isRunning();
	}

	static LinkCopyGesture of(JButton button)
	{
		return (LinkCopyGesture)button.getClientProperty(Property.GESTURE);
	}

	public static void disposeTree(Container root)
	{
		for (Component child : root.getComponents())
		{
			if (child instanceof JButton)
			{
				LinkCopyGesture gesture = of((JButton)child);
				if (gesture != null)
				{
					gesture.disposed = true;
					gesture.clear();
				}
			}
			if (child instanceof Container)
			{
				disposeTree((Container)child);
			}
		}
	}
}
