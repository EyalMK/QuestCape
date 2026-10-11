package com.questcape.ui;

import com.questcape.progress.Identities;
import java.awt.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.*;
import net.runelite.api.Skill;

/** Inert styled text: skill icons come only from RuneLite, never the guide's image URLs. */
final class SkillText extends JTextPane
{
	private static final Icon PENDING_ICON = new Icon()
	{
		public int getIconWidth() { return 16; }
		public int getIconHeight() { return 16; }
		public void paintIcon(Component c, Graphics g, int x, int y) { }
	};
	private static final Pattern MENTION = Pattern.compile("(?i)(?<![\\p{L}\\p{N}_])(" +
		Arrays.stream(Skill.values()).map(Skill::getName).map(Pattern::quote).collect(Collectors.joining("|"))
		+ "|Runecrafting|Defense)(?![\\p{L}\\p{N}_])");
	private final Map<Integer, Skill> icons = new LinkedHashMap<>();

	SkillText(String source, Font font, Color color, Map<Skill, Icon> images)
	{
		setFont(font);
		setForeground(color);
		setOpaque(false);
		setEditable(false);
		setAlignmentX(LEFT_ALIGNMENT);
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		getAccessibleContext().setAccessibleName(source);
		Matcher matcher = MENTION.matcher(source);
		StringBuilder text = new StringBuilder();
		int end = 0;
		while (matcher.find())
		{
			String identity = Identities.skill(matcher.group());
			text.append(source, end, matcher.start());
			if (identity != null)
			{
				icons.put(text.length(), Skill.valueOf(identity));
				text.append('\ufffc').append(' ');
			}
			text.append(matcher.group());
			end = matcher.end();
		}
		text.append(source.substring(end));
		setText(text.toString());
		updateIcons(images);
	}

	void updateIcons(Map<Skill, Icon> images)
	{
		icons.forEach((offset, skill) ->
		{
			SimpleAttributeSet attributes = new SimpleAttributeSet();
			StyleConstants.setIcon(attributes, images.getOrDefault(skill, PENDING_ICON));
			getStyledDocument().setCharacterAttributes(offset, 1, attributes, true);
		});
	}

	@Override
	public Dimension getPreferredSize()
	{
		int width = getWidth() > 0 ? getWidth() : 180;
		Insets insets = getInsets();
		View root = getUI().getRootView(this);
		root.setSize(Math.max(1, width - insets.left - insets.right), Integer.MAX_VALUE);
		return new Dimension(width, (int)Math.ceil(root.getPreferredSpan(View.Y_AXIS)) + insets.top + insets.bottom);
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
}
