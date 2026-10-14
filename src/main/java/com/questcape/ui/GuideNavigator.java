package com.questcape.ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;

/** Owns viewport geometry, explicit user intent, anchors and clamped alignment. */
final class GuideNavigator
{
	final JScrollPane viewport;
	final JPanel body;
	final List<GuideRowCard> rows;
	final GuideNavigationBar navigation;
	final JButton next;
	GuideRowCard progressRow;
	boolean applying, disposed;
	long scrollIntent;
	Runnable onUserNavigation = () ->
	{
	};

	GuideNavigator(JScrollPane viewport, JPanel body, List<GuideRowCard> rows, GuideNavigationBar navigation)
	{
		this.viewport = viewport;
		this.body = body;
		this.rows = rows;
		this.navigation = navigation;
		next = navigation.next;
	}

	void userNavigated()
	{
		scrollIntent++;
		onUserNavigation.run();
	}

	int position(GuideRowCard card)
	{
		return SwingUtilities.convertPoint(card.getParent(), card.getLocation(), body).y;
	}

	void jumpBoundary()
	{
		JScrollBar bar = viewport.getVerticalScrollBar();
		int extent = bar.getMaximum() - bar.getVisibleAmount();
		jumpTo(bar.getValue() < Math.max(1, extent / 2) ? Integer.MAX_VALUE : 0);
	}

	void jumpProgress()
	{
		if (progressRow != null)
		{
			jumpTo(SwingUtilities.convertPoint(progressRow.getParent(), progressRow.getLocation(), body).y);
		}
	}

	void jumpTo(int y)
	{
		userNavigated();
		viewport.getVerticalScrollBar().setValue(y);
		updateNavigation();
	}

	void updateNavigation()
	{
		if (applying)
		{
			return;
		}
		JScrollBar bar = viewport.getVerticalScrollBar();
		int end = Math.max(0, bar.getMaximum() - bar.getVisibleAmount());
		navigation.direction(bar.getValue() >= Math.max(1, end / 2), end > 0);
		if (progressRow == null)
		{
			next.setEnabled(false);
			next.setToolTipText("No visible unfinished step. Adjust filters or log in.");
			return;
		}
		int y = SwingUtilities.convertPoint(progressRow.getParent(), progressRow.getLocation(), body).y;
		int target = Math.max(0, Math.min(y, end));
		next.setEnabled(Math.abs(target - bar.getValue()) > 1);
		next.setToolTipText(
			"Step " + progressRow.stepNumber + ": " + progressRow.row.getTitle() + " (Ctrl+J)");
	}

	ScrollAnchor captureAnchor()
	{
		int y = viewport.getVerticalScrollBar().getValue();
		for (GuideRowCard row : rows)
		{
			if (!row.isVisible())
			{
				continue;
			}
			int top = SwingUtilities.convertPoint(row.getParent(), row.getLocation(), body).y;
			if (top + row.getHeight() > y)
			{
				return new ScrollAnchor(row.row.getKey(), y - top, y, scrollIntent);
			}
		}
		return new ScrollAnchor(null, 0, y, scrollIntent);
	}

	void restoreAfterLayout(ScrollAnchor anchor)
	{
		restoreAfterLayout(anchor, () ->
		{
		});
	}

	void restoreAfterLayout(ScrollAnchor anchor, Runnable restored)
	{
		restore(anchor, restored);
	}

	private void restore(ScrollAnchor anchor, Runnable restored)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (disposed || scrollIntent != anchor.intent)
			{
				return;
			}
			restored.run();
			body.validate();
			int value = anchor.absolute;
			for (GuideRowCard row : rows)
			{
				if (row.isVisible() && row.row.getKey().equals(anchor.key))
				{
					value = SwingUtilities.convertPoint(row.getParent(), row.getLocation(), body).y + anchor.offset;
					break;
				}
			}
			viewport.getVerticalScrollBar().setValue(value);
			updateNavigation();
		});
	}

	static final class ScrollAnchor
	{
		final String key;
		final int offset, absolute;
		final long intent;

		ScrollAnchor(String key, int offset, int absolute, long intent)
		{
			this.key = key;
			this.offset = offset;
			this.absolute = absolute;
			this.intent = intent;
		}
	}

}
