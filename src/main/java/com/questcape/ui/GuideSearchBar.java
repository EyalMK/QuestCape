package com.questcape.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;
import com.questcape.ui.GuideControlIcons.*;

/** Search field, coexisting controls and separate feedback; no traversal policy. */
final class GuideSearchBar extends JPanel
{
	final JTextField search = new JTextField()
	{
		@Override
		protected void paintComponent(Graphics graphics)
		{
			super.paintComponent(graphics);
			if (getText().isEmpty() && !isFocusOwner())
			{
				Graphics hint = graphics.create();
				try
				{
					hint.setColor(MUTED);
					FontMetrics metrics = hint.getFontMetrics();
					hint.drawString("Search steps…", 6, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
				}
				finally
				{
					hint.dispose();
				}
			}
		}
	};
	final JButton searchMatches = iconButton("Next search match", new ChevronIcon());
	final JButton clearSearch = iconButton(UiCopy.CLEAR, new GlyphIcon(true, MUTED));
	final JTextArea searchFeedback = text("", MUTED, UiMetrics.META, false);

	GuideSearchBar()
	{
		setOpaque(false);
		setAlignmentX(LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		search.setFont(font(14, false));
		search.setBackground(CARD);
		search.setForeground(TEXT);
		search.setCaretColor(BLUE);
		search.setBorder(new EmptyBorder(5, 6, 5, 2));
		search.setToolTipText("Search steps; Enter advances matches. At the last match, press Enter twice to wrap.");
		search.getAccessibleContext().setAccessibleName("Search steps");
		JPanel searchBox = new JPanel(new BorderLayout());
		searchBox.setBackground(CARD);
		searchBox.setBorder(new LineBorder(BORDER));
		searchBox.setAlignmentX(LEFT_ALIGNMENT);
		searchBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		searchMatches.setFont(font(12, false));
		searchMatches.setForeground(MUTED);
		searchMatches.setBorder(new EmptyBorder(3, 4, 3, 6));
		searchMatches.setHorizontalTextPosition(SwingConstants.LEFT);
		searchMatches.setIconTextGap(4);
		searchMatches.setPreferredSize(null);
		searchMatches.setVisible(false);

		searchBox.add(search, BorderLayout.CENTER);
		JPanel trailing = new JPanel(new BorderLayout(3, 0));
		trailing.setOpaque(false);
		trailing.add(searchMatches, BorderLayout.CENTER);
		clearSearch.setPreferredSize(new Dimension(28, 28));
		clearSearch.setBorder(new CompoundBorder(new LineBorder(UiTheme.QUIET_BORDER), new EmptyBorder(4, 4, 4, 4)));
		quietFocus(clearSearch, 4);
		clearSearch.setEnabled(false);
		clearSearch.addActionListener(e -> search.setText(""));
		trailing.add(clearSearch, BorderLayout.EAST);
		searchBox.add(trailing, BorderLayout.EAST);
		add(searchBox);
		add(searchFeedback);

	}
}
