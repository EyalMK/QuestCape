package com.questcape.ui;

import com.questcape.guide.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;

/** Ordered, complete source presentation with independent link actions. */
final class GuideRowDetails extends JPanel
{
	GuideRowDetails(GuideDetailProjector detail, GuideAssets assets, GuidePanel.Actions actions)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setAlignmentX(LEFT_ALIGNMENT);
		if (detail.getPrimary() != null)
		{
			add(link(detail.getPrimary(), actions));
		}
		for (GuideDetailProjector.Field field : detail.getFields())
		{
			switch (field.getRole())
			{
			case ADVICE:
				add(text(UiCopy.ADVICE, MUTED, UiMetrics.META, true));
				SkillText advice = new SkillText(field.getValue(), font(UiMetrics.BODY, false), TEXT, assets.skills);
				assets.texts.add(advice);
				add(advice);
				break;
			case PROJECTIONS:
				add(text(UiCopy.PROJECTIONS, MUTED, UiMetrics.META, true));
				add(projected(field.getValue(), assets));
				break;
			case QUEST_POINTS:
				add(text("Quest points: " + field.getValue(), TEXT, UiMetrics.BODY, false));
				break;
			case GUIDE_QP:
				add(text("Guide QP total: " + field.getValue(), MUTED, UiMetrics.META, false));
				break;
			case LOCATION:
				add(text("Location", MUTED, UiMetrics.META, true));
				if (detail.getLocation().isEmpty())
				{
					add(text(field.getValue(), TEXT, UiMetrics.BODY, false));
				}
				else
				{
					if (detail.getLocation().size() != 1
						|| !detail.getLocation().get(0).getLabel().equals(field.getValue() + " ↗"))
					{
						add(text(field.getValue(), TEXT, UiMetrics.BODY, false));
					}
					detail.getLocation().forEach(destination -> add(link(destination, actions)));
				}
				break;
			default:
				add(text(field.getLabel() + ": " + field.getValue(), TEXT, UiMetrics.BODY, false));
			}
			add(Box.createVerticalStrut(5));
		}
		if (!detail.getRelated().isEmpty())
		{
			add(text(UiCopy.RELATED + " (" + detail.getRelated().size() + ")", BLUE, UiMetrics.META, true));
			detail.getRelated().forEach(destination -> add(link(destination, actions)));
		}
		if (!detail.getRaw().isEmpty())
		{
			JPanel raw = vertical();
			detail.getRaw().forEach(
				field -> raw.add(text(field.getLabel() + ": " + field.getValue(), MUTED, UiMetrics.META, false)));
			raw.setVisible(false);
			JButton disclosure = textButton("Source fields ▾");
			disclosure.addActionListener(e ->
			{
				raw.setVisible(!raw.isVisible());
				disclosure.setText(raw.isVisible() ? "Source fields ▴" : "Source fields ▾");
				revalidate();
			});
			add(disclosure);
			add(raw);
		}
	}

	static JButton link(GuideDetailProjector.Link link, GuidePanel.Actions actions)
	{
		JButton button = textButton("<html>" + escape(link.getLabel()) + "</html>");
		button.setFont(font(UiMetrics.BODY, false));
		button.setToolTipText(link.getUrl());
		button.addActionListener(e -> actions.source(link.getUrl()));
		return button;
	}

	private static JComponent projected(String source, GuideAssets assets)
	{
		List<ProjectedLevels.Entry> entries = ProjectedLevels.parse(source);
		if (entries.isEmpty())
		{
			return text(source, TEXT, UiMetrics.BODY, false);
		}
		JPanel panel = new JPanel(new GridLayout(0, 3, 6, 5));
		panel.setOpaque(false);
		panel.setAlignmentX(LEFT_ALIGNMENT);
		for (ProjectedLevels.Entry entry : entries)
		{
			JLabel label = new JLabel(String.valueOf(entry.level), assets.projections.get(entry.skill),
				SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setFont(font(UiMetrics.BODY, false));
			label.setForeground(TEXT);
			label.setToolTipText(entry.skill.getName() + ": guide projected level " + entry.level);
			label.getAccessibleContext()
				.setAccessibleName("Guide projected " + entry.skill.getName() + " level " + entry.level);
			assets.projectedLabels.computeIfAbsent(entry.skill, ignored -> new java.util.ArrayList<>()).add(label);
			panel.add(label);
		}
		return panel;
	}
}
