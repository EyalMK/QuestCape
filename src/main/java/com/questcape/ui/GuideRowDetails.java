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
	GuideRowDetails(GuideDetailProjector detail, GuideAssets assets, GuidePanel.Actions actions, Runnable reflow)
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
				add(text(UiCopy.ADVICE, MUTED, UiMetrics.DETAIL_HEADING, true));
				SkillText advice = new SkillText(field.getValue(), font(UiMetrics.BODY, false), TEXT, assets.skills,
					field.getTokens());
				assets.texts.add(advice);
				add(advice);
				break;
			case PROJECTIONS:
				add(text(UiCopy.PROJECTIONS, MUTED, UiMetrics.META, true));
				add(projected(field, assets));
				break;
			case QUEST_POINTS:
				headingValue(UiCopy.QUEST_POINTS, field.getValue());
				break;
			case GUIDE_QP:
				headingValue(UiCopy.GUIDE_QP, field.getValue());
				break;
			case LOCATION:
				add(text(UiCopy.LOCATION, MUTED, UiMetrics.META, true));
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
			add(Box.createVerticalStrut(UiMetrics.DETAIL_GAP));
		}
		if (!detail.getRelated().isEmpty())
		{
			JPanel related = vertical();
			detail.getRelated().forEach(destination -> related.add(link(destination, actions)));
			add(new DetailDisclosure(UiCopy.RELATED + " (" + detail.getRelated().size() + ")", related,
				detail.relatedInitiallyOpen(), reflow));
		}
		if (!detail.getRaw().isEmpty())
		{
			JPanel raw = vertical();
			detail.getRaw().forEach(
				field -> raw.add(text(field.getLabel() + ": " + field.getValue(), MUTED, UiMetrics.META, false)));
			raw.setVisible(false);
			JButton disclosure = textButton(UiCopy.SOURCE_FIELDS_CLOSED);
			disclosure.addActionListener(e ->
			{
				reflow.run();
				raw.setVisible(!raw.isVisible());
				disclosure.setText(raw.isVisible() ? UiCopy.SOURCE_FIELDS_OPEN : UiCopy.SOURCE_FIELDS_CLOSED);
				revalidate();
			});
			add(disclosure);
			add(raw);
		}
	}

	private void headingValue(String heading, String value)
	{
		add(text(heading, MUTED, UiMetrics.DETAIL_HEADING, true));
		add(text(value, TEXT, UiMetrics.BODY, false));
	}

	static JButton link(GuideDetailProjector.Link link, GuidePanel.Actions actions)
	{
		JButton button = textButton("<html>" + escape(link.getLabel()) + "</html>");
		button.setFont(font(UiMetrics.BODY, false));
		button.setToolTipText(link.getUrl());
		button.addActionListener(e -> actions.source(link.getUrl()));
		LinkCopyGesture.install(button, link::getUrl, actions);
		return button;
	}

	private static JComponent projected(GuideDetailProjector.Field field, GuideAssets assets)
	{
		List<ProjectedLevelInterpreter.Entry> entries = field.getProjections();
		if (entries.isEmpty())
		{
			JTextArea raw = text(field.getValue(), TEXT, UiMetrics.BODY, false);
			InteractionRole.readOnly(raw);
			return raw;
		}
		JPanel panel = new JPanel(
			new GridLayout(0, UiMetrics.PROJECTION_COLUMNS, UiMetrics.PROJECTION_COLUMN_GAP, UiMetrics.DETAIL_GAP));
		InteractionRole.readOnly(panel);
		panel.setOpaque(false);
		panel.setAlignmentX(LEFT_ALIGNMENT);
		for (ProjectedLevelInterpreter.Entry entry : entries)
		{
			JLabel label = new JLabel(String.valueOf(entry.getLevel()), assets.projections.get(entry.getSkill()),
				SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setFont(font(UiMetrics.BODY, false));
			label.setForeground(TEXT);
			label.setToolTipText(entry.getSkill().getName() + ": guide projected level " + entry.getLevel());
			label.getAccessibleContext()
				.setAccessibleName("Guide projected " + entry.getSkill().getName() + " level " + entry.getLevel());
			assets.projectedLabels.computeIfAbsent(entry.getSkill(), ignored -> new java.util.ArrayList<>()).add(label);
			panel.add(label);
		}
		return panel;
	}
}
