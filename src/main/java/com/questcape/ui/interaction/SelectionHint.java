package com.questcape.ui.interaction;

import com.questcape.ui.style.UiComponents;
import com.questcape.ui.style.UiCopy;
import com.questcape.ui.style.UiMetrics;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** One delayed hint per card, independent of the client's shared tooltip timing. */
final class SelectionHint extends MouseAdapter
{
	final Timer delay;
	private final JComponent card;
	private JComponent target;
	private Point pointer;
	private Popup popup;
	private boolean disposed;

	SelectionHint(JComponent card)
	{
		this.card = card;
		delay = new Timer(UiMetrics.SELECTION_HINT_MILLIS, e -> show());
		delay.setRepeats(false);
		card.addHierarchyListener(e ->
		{
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !card.isShowing())
			{
				clear();
			}
		});
		card.addHierarchyBoundsListener(new HierarchyBoundsAdapter()
		{
			public void ancestorMoved(HierarchyEvent e)
			{
				clear();
			}

			public void ancestorResized(HierarchyEvent e)
			{
				clear();
			}
		});
	}

	void install(JComponent component)
	{
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addHierarchyListener(event ->
		{
			if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
				&& target == component && !component.isShowing())
			{
				clear();
			}
		});
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		if (!disposed)
		{
			clear();
			target = (JComponent)event.getSource();
			pointer = event.getPoint();
			delay.restart();
		}
	}

	@Override
	public void mouseMoved(MouseEvent event)
	{
		if (target != event.getSource())
		{
			mouseEntered(event);
		}
		else
		{
			pointer = event.getPoint();
		}
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
		clear();
	}

	@Override
	public void mousePressed(MouseEvent event)
	{
		clear();
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		clear();
	}

	private void show()
	{
		if (disposed || target == null || !target.isShowing() || !card.isShowing())
		{
			clear();
			return;
		}
		JToolTip tip = target.createToolTip();
		tip.setTipText(UiCopy.SELECT_HINT);
		tip.setFont(UiComponents.font(UiMetrics.TOOLTIP, false));
		Point location = target.getLocationOnScreen();
		Rectangle screen = target.getGraphicsConfiguration().getBounds();
		Dimension size = tip.getPreferredSize();
		int x = Math.max(screen.x, Math.min(location.x + pointer.x, screen.x + screen.width - size.width));
		int y = location.y + pointer.y + UiMetrics.SELECTION_HINT_OFFSET;
		if (y + size.height > screen.y + screen.height)
		{
			y = Math.max(screen.y, location.y + pointer.y - size.height - UiMetrics.COPY_POPUP_GAP);
		}
		popup = PopupFactory.getSharedInstance().getPopup(target, tip, x, y);
		popup.show();
	}

	void clear()
	{
		delay.stop();
		target = null;
		if (popup != null)
		{
			popup.hide();
			popup = null;
		}
	}

	void dispose()
	{
		disposed = true;
		clear();
	}
}
