package com.questcape.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;

/** Fixed controls render navigation eligibility; the route owner retains all viewport intent. */
final class GuideNavigationBar extends JPanel
{
	final JButton boundary = iconButton("Bottom", new GuideControlIcons.BoundaryIcon(false));
	final JButton next = button("Continue");

	GuideNavigationBar(Runnable jumpBoundary, Runnable jumpProgress)
	{
		setLayout(new BorderLayout(0, 5));
		setBackground(BACKGROUND);
		setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER),
			new EmptyBorder(6, UiMetrics.SIDE_MARGIN, 8, UiMetrics.SIDE_MARGIN)));
		boundary.setBorder(new EmptyBorder(3, 3, 3, 3));
		boundary.addActionListener(e -> jumpBoundary.run());
		JPanel direction = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		direction.setOpaque(false);
		direction.add(boundary);
		add(direction, BorderLayout.NORTH);
		next.setForeground(BLUE);
		next.setFont(font(UiMetrics.BODY, true));
		next.setIcon(new GuideControlIcons.ContinueIcon());
		next.setBorder(new CompoundBorder(new LineBorder(QUIET_BORDER), new EmptyBorder(6, 6, 6, 6)));
		next.setPreferredSize(new Dimension(180, UiMetrics.CONTROL));
		next.addActionListener(e -> jumpProgress.run());
		add(next, BorderLayout.CENTER);
	}

	void direction(boolean up, boolean enabled)
	{
		String destination = up ? "Top" : "Bottom";
		boundary.setIcon(new GuideControlIcons.BoundaryIcon(up));
		boundary.setToolTipText(destination);
		boundary.getAccessibleContext().setAccessibleName(destination);
		boundary.setEnabled(enabled);
	}
}
