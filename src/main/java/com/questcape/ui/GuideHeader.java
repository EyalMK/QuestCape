package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.progress.AccountProgress;
import com.questcape.persistence.StoredView.SaveState;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;

/** Compact identity/actions and conditional recovery; no Client/service reads or IO. */
final class GuideHeader extends JPanel
{
	final JButton sync = new TrailingIconButton(UiCopy.RESYNC), report;
	final JTextArea summary = text("Progress unavailable", TEXT, UiMetrics.BODY, false);
	final JTextArea source = text("Loading route…", MUTED, UiMetrics.META, false);
	final JTextArea recovery = text("", AMBER, UiMetrics.META, false);
	final JTextArea provenance = text("", MUTED, UiMetrics.META, false);
	final JProgressBar progress = new JProgressBar(0, 100);
	private final JLabel logo = new JLabel();
	private final JButton refresh;

	GuideHeader(GuidePanel.Actions actions)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setBorder(new EmptyBorder(12, UiMetrics.SIDE_MARGIN, 10, UiMetrics.SIDE_MARGIN));
		JPanel title = new JPanel(new BorderLayout(UiMetrics.HEADER_LOGO_GAP, 0));
		title.setOpaque(false);
		title.setAlignmentX(LEFT_ALIGNMENT);
		logo.setPreferredSize(new Dimension(22, 22));
		logo.getAccessibleContext().setAccessibleName("QuestCape plugin icon");
		title.add(logo, BorderLayout.WEST);
		title.add(text(UiCopy.TITLE, TEXT, UiMetrics.TITLE, true), BorderLayout.CENTER);
		report = new BelowTooltipButton();
		report.setPreferredSize(new Dimension(30, 30));
		report.setContentAreaFilled(false);
		report.setBorder(new CompoundBorder(new LineBorder(QUIET_BORDER), new EmptyBorder(4, 4, 4, 4)));
		quietFocus(report, 4);
		report.setToolTipText(UiCopy.REPORT);
		report.getAccessibleContext().setAccessibleName(UiCopy.REPORT);
		report.addActionListener(e -> actions.report());
		title.add(report, BorderLayout.EAST);
		add(title);
		JPanel wiki = new JPanel(new BorderLayout());
		wiki.setOpaque(false);
		wiki.setAlignmentX(LEFT_ALIGNMENT);
		wiki.add(source, BorderLayout.CENTER);
		refresh = iconButton("Refresh quest route from OSRS Wiki", new GuideControlIcons.GlyphIcon(false, BLUE));
		refresh.setPreferredSize(new Dimension(30, 30));
		refresh.addActionListener(e -> actions.refresh());
		wiki.add(refresh, BorderLayout.EAST);
		add(wiki);
		sync.setFont(font(UiMetrics.BODY, false));
		sync.setForeground(TEXT);
		sync.setBackground(CARD);
		sync.setAlignmentX(LEFT_ALIGNMENT);
		sync.setHorizontalAlignment(SwingConstants.LEFT);
		sync.setHorizontalTextPosition(SwingConstants.LEFT);
		sync.setIcon(new GuideControlIcons.ResyncIcon());
		sync.setIconTextGap(30);
		sync.setBorder(new CompoundBorder(new LineBorder(QUIET_BORDER), new EmptyBorder(6, 8, 6, 8)));
		sync.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiMetrics.CONTROL));
		sync.setPreferredSize(new Dimension(200, UiMetrics.CONTROL));
		quietFocus(sync, 6, 8);
		sync.addActionListener(e -> actions.syncPlayer());
		add(sync);
		add(Box.createVerticalStrut(6));
		add(summary);
		progress.setBorderPainted(false);
		progress.setForeground(GREEN);
		progress.setBackground(BORDER);
		progress.setAlignmentX(LEFT_ALIGNMENT);
		progress.setUI(new BasicProgressBarUI()
		{
			protected Color getSelectionForeground()
			{
				return BACKGROUND;
			}

			protected Color getSelectionBackground()
			{
				return TEXT;
			}
		});
		progress.setStringPainted(true);
		progress.setFont(font(UiMetrics.META, true));
		progress.setPreferredSize(new Dimension(200, 22));
		progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		progress.getAccessibleContext().setAccessibleName("Completed guide steps");
		add(progress);
		add(Box.createVerticalStrut(8));
		recovery.setVisible(false);
		add(recovery);
	}

	void assets(BufferedImage plugin, BufferedImage github)
	{
		logo.setIcon(new ImageIcon(plugin));
		report.setIcon(new ImageIcon(github));
	}

	void update(GuideSnapshot guide, AccountProgress account, SidebarStatus status, boolean ready)
	{
		sync.setEnabled(ready && account != null
			&& status.getProgressOperation() != com.questcape.integration.OperationOutcome.State.RUNNING);
		sync.setToolTipText(status.getProgressOperation() == com.questcape.integration.OperationOutcome.State.RUNNING
			? "Resyncing local progress…"
			: sync.isEnabled() ? UiCopy.RESYNC_TOOLTIP : "Log in and wait for your character to be ready");
		refresh.setEnabled(status.getGuideOperation() != com.questcape.integration.OperationOutcome.State.RUNNING);
		setText(source,
			status.getGuideOperation() == com.questcape.integration.OperationOutcome.State.RUNNING ? "Refreshing route…"
				: guide == null ? "Route unavailable"
					: "OSRS Wiki · " + guide.getRows().stream().filter(GuideRow::isActionable).count() + " steps");
		List<String> warnings = new ArrayList<>();
		if (account == null)
		{
			warnings.add("Log in and wait for current progress."
				+ (guide == null ? "" : " The retained route remains browsable."));
		}
		if (status.getGuideOperation() == com.questcape.integration.OperationOutcome.State.FAILED)
		{
			warnings.add(status.getGuideDetail() + " Refresh to retry."
				+ (guide == null ? "" : " Retained route validated " + time(guide.getValidatedAt()) + "."));
		}
		if (status.getProgressOperation() == com.questcape.integration.OperationOutcome.State.FAILED)
		{
			warnings.add(status.getProgressDetail() + " Resync Progress to retry.");
		}
		if (status.getGuideSave() == SaveState.UNSAVED)
		{
			warnings.add("Route unsaved. Refresh to retry saving.");
		}
		if (status.getProgressSave() == SaveState.UNSAVED)
		{
			warnings.add("Progress unsaved. Resync Progress to retry saving.");
		}
		setText(recovery, String.join("\n", warnings));
		recovery.setVisible(!warnings.isEmpty());
		String detail = guide == null ? "Route unavailable"
			: "Revision " + guide.getRevision()
				+ "\nGuide retrieved " + time(guide.getRetrievedAt()) + "\nValidated " + time(guide.getValidatedAt());
		if (account != null)
		{
			detail += "\nCharacter: " + account.getUsername().replace('_', ' ') + " · " + human(account.getMode())
				+ "\nCharacter observed " + time(account.getRetrievedAt()) + "\nProgress storage: "
				+ human(status.getProgressSave().name());
		}
		detail += "\nRoute storage: " + human(status.getGuideSave().name());
		setText(provenance, detail);
	}

	private static String time(long at)
	{
		return DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())
			.format(Instant.ofEpochMilli(at));
	}

	private static final class BelowTooltipButton extends JButton
	{
		public Point getToolTipLocation(java.awt.event.MouseEvent event)
		{
			JToolTip tip = createToolTip();
			tip.setTipText(getToolTipText());
			return new Point(Math.min(0, getWidth() - tip.getPreferredSize().width), getHeight() + 3);
		}

		public JToolTip createToolTip()
		{
			JToolTip tip = super.createToolTip();
			tip.setFont(font(UiMetrics.TOOLTIP, false));
			tip.setBackground(CARD);
			tip.setForeground(TEXT);
			tip.setBorder(new CompoundBorder(new LineBorder(TOOLTIP_BORDER), new EmptyBorder(3, 5, 3, 5)));
			return tip;
		}
	}
}
