package com.questcape.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.*;
import net.runelite.client.ui.FontManager;
import static com.questcape.ui.UiTheme.*;
import static java.awt.Component.LEFT_ALIGNMENT;

/** Shared native metrics and inert wrapped text/button presentation. */
final class UiComponents
{
	private UiComponents()
	{
	}

	static void requireEdt()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new IllegalStateException("UI updates must run on EDT");
		}
	}

	static void setText(JTextArea area, String value)
	{
		String safe = value == null ? "" : value;
		if (!area.getText().equals(safe))
		{
			area.setText(safe);
		}
	}

	static String human(String value)
	{
		String text = value.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
		return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

	static void quietFocus(JButton button, int padding)
	{
		quietFocus(button, padding, padding);
	}

	static void quietFocus(JButton button, int vertical, int horizontal)
	{
		Runnable border = () -> button
			.setBorder(new CompoundBorder(new LineBorder(button.isFocusOwner() ? BLUE : QUIET_BORDER),
				new EmptyBorder(vertical, horizontal, vertical, horizontal)));
		border.run();
		button.addFocusListener(new java.awt.event.FocusAdapter()
		{
			public void focusGained(java.awt.event.FocusEvent e)
			{
				border.run();
			}

			public void focusLost(java.awt.event.FocusEvent e)
			{
				border.run();
			}
		});
	}

	static final class TrailingIconButton extends JButton
	{
		TrailingIconButton(String text)
		{
			super(text);
		}

		@Override
		public int getIconTextGap()
		{
			if (getIcon() == null || getFont() == null || getWidth() == 0)
			{
				return super.getIconTextGap();
			}
			Insets insets = getInsets();
			return Math.max(4, getWidth() - insets.left - insets.right - getIcon().getIconWidth()
				- getFontMetrics(getFont()).stringWidth(getText()));
		}
	}

	static JPanel vertical()
	{
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setAlignmentX(LEFT_ALIGNMENT);
		return p;
	}

	static JTextArea text(String value, Color color, int size, boolean bold)
	{
		JTextArea text = new WrappedText(value);
		text.setFont(font(size, bold));
		text.setForeground(color);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(false);
		text.setOpaque(false);
		text.setAlignmentX(LEFT_ALIGNMENT);
		text.setBorder(new EmptyBorder(2, 0, 2, 0));
		((DefaultCaret)text.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		return text;
	}

	static Font font(int size, boolean bold)
	{
		return FontManager.getFallbackFont("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
	}

	private static final class WrappedText extends JTextArea
	{
		WrappedText(String value)
		{
			super(value);
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D smooth = (Graphics2D)graphics.create();
			try
			{
				smooth.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				super.paintComponent(smooth);
			}
			finally
			{
				smooth.dispose();
			}
		}

		@Override
		public Dimension getPreferredSize()
		{
			// Let the installed UI measure its editor view; sizing the root directly undercounts
			// wrapped lines with RuneLite's FlatLaf text UI and clips the end of long titles.
			Dimension preferred = super.getPreferredSize();
			return new Dimension(getWidth() > 0 ? getWidth() : 180, preferred.height);
		}

		@Override
		public Dimension getMinimumSize()
		{
			return new Dimension(10, getPreferredSize().height);
		}

		@Override
		public Dimension getMaximumSize()
		{
			return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
		}

		@Override
		public JToolTip createToolTip()
		{
			JToolTip tooltip = super.createToolTip();
			tooltip.setFont(font(14, false));
			return tooltip;
		}
	}

	static JButton button(String label)
	{
		JButton b = new WrappedButton(label);
		b.setFont(font(14, true));
		b.setForeground(TEXT);
		b.setBackground(CARD);
		b.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(7, 6, 7, 6)));
		b.setFocusPainted(true);
		b.setAlignmentX(LEFT_ALIGNMENT);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	/** Let HTML titles use the actual sidebar width instead of a fixed CSS width. */
	private static final class WrappedButton extends JButton
	{
		WrappedButton(String label)
		{
			super(label);
		}

		@Override
		public Dimension getPreferredSize()
		{
			View html = (View)getClientProperty(BasicHTML.propertyKey);
			if (html == null)
			{
				return super.getPreferredSize();
			}
			Container parent = getParent();
			int width = parent == null || parent.getWidth() <= 0 ? 180
				: parent.getWidth() - parent.getInsets().left - parent.getInsets().right;
			Insets insets = getInsets();
			html.setSize(Math.max(1, width - insets.left - insets.right), 0);
			return new Dimension(width,
				(int)Math.ceil(html.getPreferredSpan(View.Y_AXIS)) + insets.top + insets.bottom);
		}

		@Override
		public Dimension getMaximumSize()
		{
			return getClientProperty(BasicHTML.propertyKey) == null ? super.getMaximumSize()
				: new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
		}

		@Override
		public Dimension getMinimumSize()
		{
			return getClientProperty(BasicHTML.propertyKey) == null ? super.getMinimumSize()
				: new Dimension(10, getPreferredSize().height);
		}

		@Override
		public JToolTip createToolTip()
		{
			JToolTip tooltip = super.createToolTip();
			tooltip.setFont(font(14, false));
			return tooltip;
		}
	}

	static JButton textButton(String label)
	{
		JButton b = button(label);
		b.setContentAreaFilled(false);
		b.setBorder(new EmptyBorder(5, 0, 5, 0));
		b.setForeground(BLUE);
		return b;
	}

	static JButton iconButton(String label, Icon icon)
	{
		JButton b = button("");
		b.setIcon(icon);
		b.setToolTipText(label);
		b.getAccessibleContext().setAccessibleName(label);
		b.setPreferredSize(new Dimension(34, 34));
		b.setBorder(new EmptyBorder(7, 7, 7, 7));
		b.setContentAreaFilled(false);
		return b;
	}

	static String escape(String value)
	{
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

}
