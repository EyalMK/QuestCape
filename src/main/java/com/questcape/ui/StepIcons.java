package com.questcape.ui;

import com.questcape.guide.GuideRow;
import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/** Small vector fallbacks until RuneLite's standard quest, diary and skills sprites are available. */
final class StepIcons implements Icon
{
	private final GuideRow.Kind kind;
	private final Icon questIcon;

	StepIcons(GuideRow.Kind kind)
	{
		this(kind, null);
	}

	StepIcons(GuideRow.Kind kind, Icon questIcon)
	{
		this.kind = kind;
		this.questIcon = questIcon;
	}

	@Override
	public int getIconWidth()
	{
		return 16;
	}

	@Override
	public int getIconHeight()
	{
		return 16;
	}

	@Override
	public void paintIcon(Component component, Graphics graphics, int x, int y)
	{
		Graphics2D g = (Graphics2D)graphics.create();
		try
		{
			g.translate(x, y);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(GuidePanel.kindColor(kind));
			switch (kind)
			{
			case QUEST:
			case MINIQUEST:
			case DIARY:
				if (questIcon != null)
				{
					questIcon.paintIcon(component, g, 0, 0);
				}
				else
				{
					g.setColor(kind == GuideRow.Kind.DIARY ? GuidePanel.GREEN : GuidePanel.BLUE);
					Path2D star = new Path2D.Double();
					star.moveTo(8, 1);
					star.lineTo(10, 6);
					star.lineTo(15, 8);
					star.lineTo(10, 10);
					star.lineTo(8, 15);
					star.lineTo(6, 10);
					star.lineTo(1, 8);
					star.lineTo(6, 6);
					star.closePath();
					g.fill(star);
					g.setColor(GuidePanel.TEXT);
					g.drawLine(8, 4, 8, 11);
				}
				if (kind == GuideRow.Kind.MINIQUEST)
				{
					g.setColor(GuidePanel.CARD);
					g.fillOval(9, 9, 7, 7);
					g.setColor(GuidePanel.GOLD);
					g.fillOval(10, 10, 5, 5);
				}
				break;
			case TRAINING:
				g.fillRect(1, 10, 3, 5);
				g.fillRect(6, 6, 3, 9);
				g.fillRect(11, 1, 3, 14);
				break;
			case UNLOCK:
				g.drawArc(4, 1, 8, 10, 0, 180);
				g.fillRoundRect(2, 6, 12, 9, 3, 3);
				g.setColor(GuidePanel.CARD);
				g.drawLine(8, 9, 8, 12);
				break;
			case ACTIVITY:
				g.drawRoundRect(2, 2, 12, 13, 2, 2);
				g.fillRoundRect(5, 0, 6, 4, 2, 2);
				g.drawLine(5, 9, 7, 11);
				g.drawLine(7, 11, 11, 6);
				break;
			default:
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
				g.drawString("?", 4, 14);
			}
		}
		finally
		{
			g.dispose();
		}
	}
}
