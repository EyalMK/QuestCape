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
	private static final String PROGRESS_UNAVAILABLE = "Progress unavailable";
	private static final String ROUTE_LOADING = "Loading route…";
	private static final String RESYNCING = "Resyncing local progress…";
	private static final String ACCOUNT_UNREADY = "Log in and wait for your character to be ready";
	private static final String REFRESHING = "Refreshing route…";
	private static final String ROUTE_UNAVAILABLE = "Route unavailable";
	private static final String WIKI_COUNT_PREFIX = "OSRS Wiki · ";
	private static final String STEPS_SUFFIX = " steps";
	private static final String LOGIN_LIMITATION = "Log in and wait for current progress.";
	private static final String RETAINED_ROUTE = " The retained route remains browsable.";
	private static final String REFRESH_RETRY = " Refresh to retry.";
	private static final String RETAINED_VALIDATED = " Retained route validated ";
	private static final String RESYNC_RETRY = " Resync Progress to retry.";
	private static final String ROUTE_UNSAVED = "Route unsaved. Refresh to retry saving.";
	private static final String PROGRESS_UNSAVED = "Progress unsaved. Resync Progress to retry saving.";
	private static final String REFRESH_TOOLTIP = "Refresh quest route from OSRS Wiki";
	private static final String LOGO_DESCRIPTION = "QuestCape plugin icon";
	private static final String PROGRESS_DESCRIPTION = "Completed guide steps";
	private static final String TIME_FORMAT = "MMM d, HH:mm";
	final JButton sync = iconButton(UiCopy.RESYNC, new GuideControlIcons.ResyncIcon()), report;
	final JTextArea summary = text(PROGRESS_UNAVAILABLE, TEXT, UiMetrics.BODY, false);
	final JTextArea source = text(ROUTE_LOADING, MUTED, UiMetrics.META, false);
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
		logo.getAccessibleContext().setAccessibleName(LOGO_DESCRIPTION);
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
		add(Box.createVerticalStrut(UiMetrics.HEADER_SOURCE_GAP));
		JPanel wiki = new JPanel(new BorderLayout());
		wiki.setOpaque(false);
		wiki.setAlignmentX(LEFT_ALIGNMENT);
		wiki.add(source, BorderLayout.CENTER);
		refresh = iconButton(REFRESH_TOOLTIP, new GuideControlIcons.GlyphIcon(false, BLUE));
		refresh.setPreferredSize(new Dimension(30, 30));
		refresh.addActionListener(e -> actions.refresh());
		wiki.add(refresh, BorderLayout.EAST);
		add(wiki);
		add(Box.createVerticalStrut(UiMetrics.SOURCE_RESYNC_GAP));
		JPanel resync = new JPanel(new BorderLayout());
		resync.setBackground(CARD);
		resync.setAlignmentX(LEFT_ALIGNMENT);
		resync.setBorder(new CompoundBorder(new LineBorder(QUIET_BORDER), new EmptyBorder(1, 8, 1, 1)));
		resync.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiMetrics.CONTROL));
		resync.setPreferredSize(new Dimension(200, UiMetrics.CONTROL));
		JLabel label = new JLabel(UiCopy.RESYNC);
		label.setFont(font(UiMetrics.BODY, false));
		label.setForeground(TEXT);
		resync.add(label, BorderLayout.CENTER);
		sync.setPreferredSize(new Dimension(UiMetrics.ICON_HIT, UiMetrics.ICON_HIT));
		sync.setBorder(new EmptyBorder(4, 4, 4, 4));
		focusBorder(sync, 4, 4, CARD);
		sync.addActionListener(e -> actions.syncPlayer());
		resync.add(sync, BorderLayout.EAST);
		add(resync);
		add(Box.createVerticalStrut(UiMetrics.RESYNC_PROGRESS_GAP));
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
		progress.getAccessibleContext().setAccessibleName(PROGRESS_DESCRIPTION);
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
			? RESYNCING
			: sync.isEnabled() ? UiCopy.RESYNC_TOOLTIP : ACCOUNT_UNREADY);
		refresh.setEnabled(status.getGuideOperation() != com.questcape.integration.OperationOutcome.State.RUNNING);
		setText(source,
			status.getGuideOperation() == com.questcape.integration.OperationOutcome.State.RUNNING ? REFRESHING
				: guide == null ? ROUTE_UNAVAILABLE
					: WIKI_COUNT_PREFIX + guide.getRows().stream().filter(GuideRow::isActionable).count()
						+ STEPS_SUFFIX);
		List<String> warnings = new ArrayList<>();
		if (account == null)
		{
			warnings.add(LOGIN_LIMITATION
				+ (guide == null ? "" : RETAINED_ROUTE));
		}
		if (status.getGuideOperation() == com.questcape.integration.OperationOutcome.State.FAILED)
		{
			warnings.add(status.getGuideDetail() + REFRESH_RETRY
				+ (guide == null ? "" : RETAINED_VALIDATED + time(guide.getValidatedAt()) + "."));
		}
		if (status.getProgressOperation() == com.questcape.integration.OperationOutcome.State.FAILED)
		{
			warnings.add(status.getProgressDetail() + RESYNC_RETRY);
		}
		if (status.getGuideSave() == SaveState.UNSAVED)
		{
			warnings.add(ROUTE_UNSAVED);
		}
		if (status.getProgressSave() == SaveState.UNSAVED)
		{
			warnings.add(PROGRESS_UNSAVED);
		}
		setText(recovery, String.join("\n", warnings));
		recovery.setVisible(!warnings.isEmpty());
		String detail = guide == null ? ROUTE_UNAVAILABLE
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
		return DateTimeFormatter.ofPattern(TIME_FORMAT).withZone(ZoneId.systemDefault())
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
