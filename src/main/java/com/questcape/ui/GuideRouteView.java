package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.*;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;

/** Source-order cards, expansion keys, notes and attribution belong to the route view. */
final class GuideRouteView extends JScrollPane
{
	final JPanel body = new WidthTrackingPanel(), rowsPanel = vertical();
	final List<GuideRowCard> rows = new ArrayList<>();
	final List<Component> notes = new ArrayList<>();
	private final Set<String> expanded = new HashSet<>();
	private final GuideAssets assets;
	private final GuidePanel.Actions actions;
	private final TrainingGuideResolver training;
	private GuideSnapshot guide;

	GuideRouteView(GuidePanel.Actions actions, GuideAssets assets, TrainingGuideResolver training, JTextArea provenance)
	{
		this.actions = actions;
		this.assets = assets;
		this.training = training;
		setViewportView(body);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setBackground(BACKGROUND);
		body.setBorder(new EmptyBorder(0, 10, 8, 10));
		body.add(rowsPanel);
		JPanel footer = vertical();
		footer.setBorder(new EmptyBorder(10, 0, 8, 0));
		provenance.setVisible(false);
		JButton provenanceToggle = textButton("Source and progress details ▾");
		provenanceToggle.addActionListener(e ->
		{
			provenance.setVisible(!provenance.isVisible());
			provenanceToggle
				.setText(provenance.isVisible() ? "Source and progress details ▴" : "Source and progress details ▾");
			body.revalidate();
		});
		footer.add(provenanceToggle);
		footer.add(provenance);
		JButton source = textButton("OSRS Wiki ↗");
		java.util.function.Supplier<String> sourceUrl = () -> guide == null || guide.getRevision() == null
			? GuideSnapshot.SOURCE
			: GuideSnapshot.SOURCE + "?oldid=" + guide.getRevision();
		source.addActionListener(e -> actions.source(sourceUrl.get()));
		LinkCopyGesture.install(source, sourceUrl, actions);
		footer.add(source);
		footer.add(text("OSRS Wiki contributors\nCC BY-NC-SA 3.0 · Additional terms apply", MUTED, 13, false));
		JButton license = textButton("Content license ↗");
		license.addActionListener(e -> actions.source(ApplicationLinks.CONTENT_LICENSE));
		LinkCopyGesture.install(license, () -> ApplicationLinks.CONTENT_LICENSE, actions);
		footer.add(license);
		body.add(footer);

	}

	void replace(GuideSnapshot snapshot, BiConsumer<GuideRow, Boolean> disclosed, Runnable userIntent)
	{
		guide = snapshot;
		rows.forEach(GuideRowCard::dispose);
		rows.clear();
		notes.clear();
		assets.clearBindings();
		rowsPanel.removeAll();
		if (snapshot != null)
		{
			int number = 0;
			for (GuideRow row : snapshot.getRows())
			{
				if (!row.isActionable())
				{
					Component note = comment(row), spacer = Box.createVerticalStrut(9);
					notes.add(note);
					notes.add(spacer);
					rowsPanel.add(note);
					rowsPanel.add(spacer);
					continue;
				}
				GuideRowCard card = new GuideRowCard(row, ++number, expanded.contains(row.getKey()), assets, actions,
					training,
					(changed, open) ->
					{
						if (open)
						{
							expanded.add(changed.getKey());
						}
						else
						{
							expanded.remove(changed.getKey());
						}
						disclosed.accept(changed, open);
					}, userIntent);
				rows.add(card);
				rowsPanel.add(card);
				rowsPanel.add(card.spacer);
			}
		}
		body.revalidate();
	}

	private static JPanel comment(GuideRow row)
	{
		JPanel comment = vertical();
		comment.setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, BORDER), new EmptyBorder(12, 2, 12, 2)));
		comment.add(text(row.getTitle(), MUTED, 14, false));
		comment.getAccessibleContext().setAccessibleName("Guide note: " + row.getTitle());
		return comment;
	}

	private static final class WidthTrackingPanel extends JPanel implements Scrollable
	{
		public Dimension getPreferredScrollableViewportSize()
		{
			return new Dimension(225, 600);
		}

		public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction)
		{
			return 24;
		}

		public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction)
		{
			return Math.max(24, r.height - 40);
		}

		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}
	}

}
