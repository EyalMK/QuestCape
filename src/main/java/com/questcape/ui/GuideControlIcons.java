package com.questcape.ui;

import com.questcape.progress.Completion;
import java.awt.*;
import javax.swing.Icon;

/** Small local vector controls; no image downloads or additional resources. */
final class GuideControlIcons
{
	private GuideControlIcons()
	{
	}

	static final class FilterIcon implements Icon
	{
		private final boolean state, active;

		FilterIcon(boolean state, boolean active)
		{
			this.state = state;
			this.active = active;
		}
		public int getIconWidth() { return 28; }
		public int getIconHeight() { return 18; }
		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = graphics(c, original, x, y, active ? GuidePanel.BLUE : GuidePanel.MUTED);
			try
			{
				g.drawPolyline(new int[] {1, 15, 10, 10, 6, 6, 1}, new int[] {3, 3, 9, 15, 13, 9, 3}, 7);
				if (state)
				{
					g.drawOval(18, 5, 8, 8);
					g.drawPolyline(new int[] {20, 22, 25}, new int[] {9, 11, 7}, 3);
				}
				else
				{
					for (int line = 5; line <= 13; line += 4)
					{
						g.drawLine(19, line, 26, line);
					}
				}
			}
			finally { g.dispose(); }
		}
	}

	static final class StateIcon implements Icon
	{
		private final Completion.State state;

		StateIcon(Completion.State state) { this.state = state; }
		public int getIconWidth() { return 16; }
		public int getIconHeight() { return 16; }
		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = graphics(c, original, x, y, GuidePanel.stateColor(state));
			try
			{
				g.setFont(c.getFont());
				FontMetrics metrics = g.getFontMetrics();
				String symbol = new Completion(state, false).label().substring(0, 1);
				g.drawString(symbol, (16 - metrics.stringWidth(symbol)) / 2,
					(16 - metrics.getHeight()) / 2 + metrics.getAscent());
			}
			finally { g.dispose(); }
		}
	}

	static final class ChevronIcon implements Icon
	{
		public int getIconWidth() { return 9; }
		public int getIconHeight() { return 12; }
		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = graphics(c, original, x, y, GuidePanel.MUTED);
			try { g.drawPolyline(new int[] {1, 4, 7}, new int[] {4, 7, 4}, 3); }
			finally { g.dispose(); }
		}
	}

	static final class ContinueIcon implements Icon
	{
		public int getIconWidth() { return 10; }
		public int getIconHeight() { return 12; }
		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = graphics(c, original, x, y, GuidePanel.BLUE);
			try { g.fillPolygon(new int[] {1, 1, 9}, new int[] {1, 11, 6}, 3); }
			finally { g.dispose(); }
		}
	}

	private static Graphics2D graphics(Component c, Graphics original, int x, int y, Color color)
	{
		Graphics2D g = (Graphics2D)original.create();
		g.translate(x, y);
		g.setColor(c.isEnabled() ? color : GuidePanel.BORDER);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		return g;
	}
}
