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
			Graphics2D g = graphics(c, original, x, y, active ? UiTheme.BLUE : UiTheme.MUTED);
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
			Graphics2D g = graphics(c, original, x, y, UiTheme.stateColor(state));
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
			Graphics2D g = graphics(c, original, x, y, UiTheme.MUTED);
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
			Graphics2D g = graphics(c, original, x, y, UiTheme.BLUE);
			try { g.fillPolygon(new int[] {1, 1, 9}, new int[] {1, 11, 6}, 3); }
			finally { g.dispose(); }
		}
	}

	private static Graphics2D graphics(Component c, Graphics original, int x, int y, Color color)
	{
		Graphics2D g = (Graphics2D)original.create();
		g.translate(x, y);
		g.setColor(c.isEnabled() ? color : UiTheme.BORDER);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		return g;
	}

	static final class ResyncIcon implements Icon
	{
		public int getIconWidth()
		{
			return 22;
		}

		public int getIconHeight()
		{
			return 20;
		}

		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = graphics(c, original, x, y, UiTheme.BLUE);
			try
			{
				g.drawLine(2, 5, 19, 5);
				g.drawPolyline(new int[]
				{ 15, 19, 15 }, new int[]
				{ 1, 5, 9 }, 3);
				g.drawLine(19, 15, 2, 15);
				g.drawPolyline(new int[]
				{ 6, 2, 6 }, new int[]
				{ 11, 15, 19 }, 3);
			}
			finally
			{
				g.dispose();
			}
		}
	}

	static final class BoundaryIcon implements Icon
	{
		private final boolean up;

		BoundaryIcon(boolean up)
		{
			this.up = up;
		}

		public int getIconWidth()
		{
			return 28;
		}

		public int getIconHeight()
		{
			return 28;
		}

		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = graphics(c, original, x, y, UiTheme.MUTED);
			try
			{
				g.setColor(UiTheme.CARD);
				g.fillOval(1, 1, 26, 26);
				g.setColor(UiTheme.QUIET_BORDER);
				g.drawOval(1, 1, 26, 26);
				g.setColor(c.isEnabled() ? UiTheme.MUTED : UiTheme.BORDER);
				if (!up)
				{
					g.translate(0, 28);
					g.scale(1, -1);
				}
				g.drawLine(14, 7, 14, 21);
				g.drawPolyline(new int[]
				{ 8, 14, 20 }, new int[]
				{ 13, 7, 13 }, 3);
			}
			finally
			{
				g.dispose();
			}
		}
	}

	static final class ResetIcon implements Icon
	{
		public int getIconWidth()
		{
			return 18;
		}

		public int getIconHeight()
		{
			return 18;
		}

		public void paintIcon(Component component, Graphics original, int x, int y)
		{
			Graphics2D g = (Graphics2D)original.create();
			try
			{
				g.translate(x, y);
				g.setColor(component.isEnabled() ? UiTheme.MUTED : UiTheme.BORDER);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.drawArc(3, 3, 12, 12, 40, 285);
				g.drawLine(2, 2, 2, 7);
				g.drawLine(2, 7, 7, 7);
				g.drawLine(7, 7, 11, 11);
				g.drawLine(11, 7, 7, 11);
			}
			finally
			{
				g.dispose();
			}
		}
	}

	static final class GlyphIcon implements Icon
	{
		final boolean close;
		final Color color;

		GlyphIcon(boolean close, Color color)
		{
			this.close = close;
			this.color = color;
		}

		public int getIconWidth()
		{
			return 18;
		}

		public int getIconHeight()
		{
			return 18;
		}

		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = (Graphics2D)original.create();
			g.translate(x, y);
			g.setColor(c.isEnabled() ? color : UiTheme.BORDER);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			if (close)
			{
				g.drawLine(5, 5, 13, 13);
				g.drawLine(13, 5, 5, 13);
			}
			else
			{
				g.drawArc(3, 3, 12, 12, 30, 280);
				g.drawLine(15, 2, 15, 7);
				g.drawLine(15, 7, 10, 7);
			}
			g.dispose();
		}
	}
}
