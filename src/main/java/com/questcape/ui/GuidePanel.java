package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.DefaultCaret;
import javax.swing.text.View;
import net.runelite.client.ui.PluginPanel;

/** Native, inert Swing UI. Only user navigation and structural edits move its viewport. */
public class GuidePanel extends PluginPanel
{
	public interface Actions
	{
		void syncPlayer();

		void refresh();

		void quest(GuideRow row);

		void training(String skill);

		void manual(String scope, GuideRow row, boolean checked);

		void source(String url);

		void clearQuest();
	}

	static final Color BACKGROUND = new Color(0x171C23), CARD = new Color(0x232B35), TEXT = new Color(0xECF1F7),
		MUTED = new Color(0xA5B3C4), GREEN = new Color(0x75D9A2), BLUE = new Color(0x84BFFF),
		AMBER = new Color(0xF2C479), BORDER = new Color(0x374555);
	private final Actions actions;
	private final TrainingGuideResolver training;
	private final JTextArea accountName = text("Log in to get started", TEXT, 16, true),
		accountStatus = text("Your progress syncs automatically.", MUTED, 14, false),
		guideStatus = text("Loading route…", MUTED, 13, false), summary = text("Progress unavailable", TEXT, 14, false),
		notice = text("", AMBER, 14, false), integrationDetails = text("", MUTED, 14, false),
		provenance = text("", MUTED, 13, false);
	private final JButton syncPlayer = iconButton("Sync current player", new GlyphIcon(false, BLUE));
	private final JProgressBar progressBar = new JProgressBar(0, 100)
	{
		@Override
		public JToolTip createToolTip()
		{
			JToolTip tooltip = super.createToolTip();
			tooltip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			return tooltip;
		}
	};
	private final JPanel body = new WidthTrackingPanel(), rowsPanel = vertical(),
		noticePanel = new JPanel(new BorderLayout(4, 0));
	private final JScrollPane viewport = new JScrollPane(body);
	private final JButton boundary = button("Bottom ↓"), next = button("Next step ↓");
	private final List<RowPanel> rows = new ArrayList<>();
	private final Set<String> expanded = new HashSet<>();
	private final Map<String, String> questMessages = new HashMap<>();
	private GuideSnapshot guide;
	private Icon questHelperIcon;
	private RowPanel progressRow;
	private boolean applying;
	private long scrollIntent;

	public GuidePanel(Actions actions, TrainingGuideResolver training)
	{
		super(false);
		this.actions = actions;
		this.training = training;
		setLayout(new BorderLayout());
		setBackground(BACKGROUND);
		JPanel header = vertical();
		header.setBorder(new EmptyBorder(12, 10, 10, 10));
		JPanel title = new JPanel(new BorderLayout());
		title.setOpaque(false);
		title.setAlignmentX(LEFT_ALIGNMENT);
		title.add(text("QuestCape", TEXT, 18, true), BorderLayout.CENTER);
		JButton refresh = iconButton("Refresh quest route", new GlyphIcon(false, MUTED));
		refresh.addActionListener(e -> actions.refresh());
		title.add(refresh, BorderLayout.EAST);
		header.add(title);
		header.add(guideStatus);
		header.add(Box.createVerticalStrut(8));
		JPanel account = new JPanel(new BorderLayout(4, 0));
		account.setBackground(CARD);
		account.setAlignmentX(LEFT_ALIGNMENT);
		account.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(8, 8, 8, 6)));
		JPanel identity = vertical();
		identity.add(accountName);
		identity.add(accountStatus);
		account.add(identity, BorderLayout.CENTER);
		syncPlayer.addActionListener(e -> actions.syncPlayer());
		account.add(syncPlayer, BorderLayout.EAST);
		header.add(account);
		header.add(Box.createVerticalStrut(9));
		header.add(summary);
		progressBar.setBorderPainted(false);
		progressBar.setForeground(GREEN);
		progressBar.setBackground(BORDER);
		progressBar.setAlignmentX(LEFT_ALIGNMENT);
		progressBar.setUI(new BasicProgressBarUI()
		{
			@Override
			protected Color getSelectionForeground()
			{
				return BACKGROUND;
			}

			@Override
			protected Color getSelectionBackground()
			{
				return TEXT;
			}
		});
		progressBar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(200, 22));
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		progressBar.getAccessibleContext().setAccessibleName("Completed guide steps");
		header.add(progressBar);
		add(header, BorderLayout.NORTH);

		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setBackground(BACKGROUND);
		body.setBorder(new EmptyBorder(0, 10, 8, 10));
		noticePanel.setOpaque(false);
		noticePanel.setAlignmentX(LEFT_ALIGNMENT);
		noticePanel.add(notice, BorderLayout.CENTER);
		JButton dismiss = iconButton("Dismiss message", new GlyphIcon(true, AMBER));
		dismiss.addActionListener(e -> message(""));
		noticePanel.add(dismiss, BorderLayout.EAST);
		noticePanel.setVisible(false);
		body.add(noticePanel);
		body.add(rowsPanel);
		JPanel footer = vertical();
		footer.setBorder(new EmptyBorder(10, 0, 8, 0));
		JButton integrations = textButton("Integration status");
		integrationDetails.setVisible(false);
		integrations.addActionListener(e ->
		{
			integrationDetails.setVisible(!integrationDetails.isVisible());
			body.revalidate();
		});
		footer.add(integrations);
		footer.add(integrationDetails);
		footer.add(provenance);
		JButton source = textButton("OSRS Wiki ↗");
		source.addActionListener(e -> actions.source(guide == null || guide.getRevision() == null
			? GuideSnapshot.SOURCE
			: GuideSnapshot.SOURCE + "?oldid=" + guide.getRevision()));
		footer.add(source);
		footer.add(text("OSRS Wiki contributors\nCC BY-NC-SA 3.0 · Additional terms apply", MUTED, 13, false));
		JButton license = textButton("Content license ↗");
		license.addActionListener(e -> actions.source("https://creativecommons.org/licenses/by-nc-sa/3.0/"));
		footer.add(license);
		JButton clear = textButton("Clear remembered quest");
		clear.addActionListener(e -> actions.clearQuest());
		footer.add(clear);
		body.add(footer);
		viewport.setBorder(null);
		viewport.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		viewport.getVerticalScrollBar().setUnitIncrement(24);
		viewport.getViewport().setBackground(BACKGROUND);
		viewport.getVerticalScrollBar().addAdjustmentListener(e -> updateNavigation());
		viewport.addMouseWheelListener(e -> scrollIntent++);
		viewport.getVerticalScrollBar().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				scrollIntent++;
			}
		});
		add(viewport, BorderLayout.CENTER);
		JPanel navigation = new JPanel(new GridLayout(1, 2, 6, 0));
		navigation.setBackground(BACKGROUND);
		navigation.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(8, 10, 8, 10)));
		boundary.addActionListener(e -> jumpBoundary());
		next.addActionListener(e -> jumpProgress());
		next.setForeground(BLUE);
		navigation.add(boundary);
		navigation.add(next);
		add(navigation, BorderLayout.SOUTH);
		bind("ctrl HOME", "routeTop", () -> jumpTo(0));
		bind("ctrl END", "routeBottom", () -> jumpTo(Integer.MAX_VALUE));
		bind("ctrl J", "routeNext", this::jumpProgress);
		updateNavigation();
	}

	private void bind(String key, String name, Runnable action)
	{
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), name);
		getActionMap().put(name, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				action.run();
			}
		});
	}

	public void render(GuideSnapshot snapshot, AccountProgress progress,
		String contentStatus, String syncStatus, String dependencyStatus, boolean canSync)
	{
		requireEdt();
		applying = true;
		try
		{
			syncPlayer.setEnabled(canSync && progress != null);
			syncPlayer.setToolTipText(
				canSync ? "Sync character progress from RuneLite" : "Log in and wait for your character to be ready");
			setText(accountName, progress == null ? "Log in to get started" : progress.getUsername().replace('_', ' '));
			String state = progress == null ? "Your progress syncs automatically."
				: "● Live · " + human(progress.getMode());
			if (syncStatus != null && !syncStatus.isBlank())
			{
				state += "\n" + syncStatus;
			}
			setText(accountStatus, state);
			String freshness = snapshot == null ? "Route unavailable · retry with ↻"
				: "OSRS Wiki · " + snapshot.getRows().size() + " steps";
			if (contentStatus != null && contentStatus.startsWith("Checking"))
			{
				freshness = "Refreshing route…";
			}
			else if (contentStatus != null && (contentStatus.contains("failed") || contentStatus.contains("busy")))
			{
				freshness += " · cached";
			}
			setText(guideStatus, freshness);
			guideStatus.setToolTipText(contentStatus);
			setText(integrationDetails, dependencyStatus);
			String detail = snapshot == null ? ""
				: "Revision " + snapshot.getRevision() + "\nGuide retrieved " + time(snapshot.getRetrievedAt())
					+ "\nValidated " + time(snapshot.getValidatedAt());
			if (progress != null)
			{
				detail += "\nCharacter observed " + time(progress.getRetrievedAt())
					+ "\nStored locally on this computer.";
			}
			if (contentStatus != null && !contentStatus.isBlank())
			{
				detail += "\n" + contentStatus;
			}
			setText(provenance, detail);
			boolean structural = guide == null ? snapshot != null
				: snapshot == null || !guide.getRows().equals(snapshot.getRows());
			if (structural)
			{
				ScrollAnchor anchor = captureAnchor();
				rows.clear();
				rowsPanel.removeAll();
				if (snapshot != null)
				{
					for (GuideRow row : snapshot.getRows())
					{
						RowPanel card = new RowPanel(row);
						rows.add(card);
						JPanel entry = vertical();
						entry.add(card.feedbackPanel);
						entry.add(card);
						rowsPanel.add(entry);
						rowsPanel.add(Box.createVerticalStrut(9));
					}
				}
				body.revalidate();
				restoreAfterLayout(anchor);
			}
			guide = snapshot;
			int complete = 0, total = 0, unknown = 0;
			progressRow = null;
			for (RowPanel card : rows)
			{
				Completion completion = Completion.of(card.row, progress);
				card.update(completion, progress);
				if (!card.row.isActionable())
				{
					continue;
				}
				total++;
				if (completion.getState() == Completion.State.COMPLETE)
				{
					complete++;
				}
				if (completion.getState() == Completion.State.UNKNOWN)
				{
					unknown++;
				}
				if (progress != null && completion.getState() == Completion.State.IN_PROGRESS && progressRow == null)
				{
					progressRow = card;
				}
			}
			if (progressRow == null && progress != null)
			{
				for (RowPanel card : rows)
				{
					if (card.row.isActionable()
						&& Completion.of(card.row, progress).getState() != Completion.State.COMPLETE)
					{
						progressRow = card;
						break;
					}
				}
			}
			for (RowPanel card : rows)
			{
				card.highlight(card == progressRow);
			}
			setText(summary, snapshot == null ? "Progress unavailable"
				: complete + " of " + total + " complete" + (unknown > 0 ? "\n" + unknown + " unknown" : ""));
			progressBar.setValue(total == 0 ? 0 : complete * 100 / total);
			progressBar
				.setToolTipText(complete + " completed; " + unknown + " unknown. All completed rows stay visible.");
		}
		finally
		{
			applying = false;
			updateNavigation();
		}
	}

	public void message(String value)
	{
		requireEdt();
		setText(notice, value);
		boolean visible = value != null && !value.isBlank();
		if (noticePanel.isVisible() != visible)
		{
			noticePanel.setVisible(visible);
			body.revalidate();
		}
		if (!visible && !questMessages.isEmpty())
		{
			questMessages.clear();
			for (RowPanel row : rows)
			{
				row.feedback("");
			}
			body.revalidate();
		}
	}

	/** A quest action's feedback belongs immediately above that quest, even deep in the route. */
	public void questMessage(String key, String value)
	{
		requireEdt();
		questMessages.clear();
		if (value != null && !value.isBlank())
		{
			questMessages.put(key, value);
		}
		for (RowPanel row : rows)
		{
			row.feedback(questMessages.get(row.row.getKey()));
		}
		body.revalidate();
		body.repaint();
	}

	public void setQuestHelperIcon(Icon icon)
	{
		requireEdt();
		if (questHelperIcon == icon)
		{
			return;
		}
		questHelperIcon = icon;
		for (RowPanel row : rows)
		{
			if (row.openHelper != null)
			{
				row.openHelper.setIcon(icon);
			}
		}
	}

	public JScrollPane routeScrollPane()
	{
		return viewport;
	}

	public JButton boundaryButton()
	{
		return boundary;
	}

	public JButton progressButton()
	{
		return next;
	}

	public int renderedRowCount()
	{
		return rows.size();
	}

	private void jumpBoundary()
	{
		JScrollBar bar = viewport.getVerticalScrollBar();
		int extent = bar.getMaximum() - bar.getVisibleAmount();
		jumpTo(bar.getValue() < Math.max(1, extent / 2) ? Integer.MAX_VALUE : 0);
	}

	private void jumpProgress()
	{
		if (progressRow != null)
		{
			jumpTo(SwingUtilities.convertPoint(progressRow.getParent(), progressRow.getLocation(), body).y);
		}
	}

	private void jumpTo(int y)
	{
		scrollIntent++;
		viewport.getVerticalScrollBar().setValue(y);
		updateNavigation();
	}

	private void updateNavigation()
	{
		if (applying)
		{
			return;
		}
		JScrollBar bar = viewport.getVerticalScrollBar();
		int end = Math.max(0, bar.getMaximum() - bar.getVisibleAmount());
		boundary.setText(bar.getValue() < Math.max(1, end / 2) ? "Bottom ↓" : "Top ↑");
		boundary.setEnabled(end > 0);
		if (progressRow == null)
		{
			next.setText("Next step");
			next.setEnabled(false);
			next.setToolTipText("Log in to find your next unfinished step.");
			return;
		}
		int y = SwingUtilities.convertPoint(progressRow.getParent(), progressRow.getLocation(), body).y;
		boolean visible = y >= bar.getValue() && y < bar.getValue() + bar.getVisibleAmount();
		next.setText(visible ? "At next step" : y < bar.getValue() ? "Next step ↑" : "Next step ↓");
		next.setEnabled(!visible);
		next.setToolTipText(
			"Step " + (progressRow.row.getPosition() + 1) + ": " + progressRow.row.getTitle() + " (Ctrl+J)");
	}

	private ScrollAnchor captureAnchor()
	{
		int y = viewport.getVerticalScrollBar().getValue();
		for (RowPanel row : rows)
		{
			int top = SwingUtilities.convertPoint(row.getParent(), row.getLocation(), body).y;
			if (top + row.getHeight() > y)
			{
				return new ScrollAnchor(row.row.getKey(), y - top, y, scrollIntent);
			}
		}
		return new ScrollAnchor(null, 0, y, scrollIntent);
	}

	private void restoreAfterLayout(ScrollAnchor anchor)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (scrollIntent != anchor.intent)
			{
				return;
			}
			body.validate();
			int value = anchor.absolute;
			for (RowPanel row : rows)
			{
				if (row.row.getKey().equals(anchor.key))
				{
					value = SwingUtilities.convertPoint(row.getParent(), row.getLocation(), body).y + anchor.offset;
					break;
				}
			}
			viewport.getVerticalScrollBar().setValue(value);
			updateNavigation();
		});
	}

	private static final class ScrollAnchor
	{
		final String key;
		final int offset, absolute;
		final long intent;

		ScrollAnchor(String key, int offset, int absolute, long intent)
		{
			this.key = key;
			this.offset = offset;
			this.absolute = absolute;
			this.intent = intent;
		}
	}

	private static void requireEdt()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new IllegalStateException("UI updates must run on EDT");
		}
	}

	private static void setText(JTextArea area, String value)
	{
		String safe = value == null ? "" : value;
		if (!area.getText().equals(safe))
		{
			area.setText(safe);
		}
	}

	private static String time(long at)
	{
		return DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())
			.format(Instant.ofEpochMilli(at));
	}

	private static String human(String value)
	{
		String s = value.toLowerCase(Locale.ROOT).replace('_', ' ');
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static JPanel vertical()
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
		text.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
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

	private static final class WrappedText extends JTextArea
	{
		WrappedText(String value)
		{
			super(value);
		}

		@Override
		public Dimension getPreferredSize()
		{
			int width = getWidth();
			if (width <= 0)
			{
				width = 180;
			}
			Insets insets = getInsets();
			View root = getUI().getRootView(this);
			root.setSize(Math.max(1, width - insets.left - insets.right), Integer.MAX_VALUE);
			return new Dimension(width,
				(int)Math.ceil(root.getPreferredSpan(View.Y_AXIS)) + insets.top + insets.bottom);
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
			tooltip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			return tooltip;
		}
	}

	private static JButton button(String label)
	{
		JButton b = new WrappedButton(label);
		b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
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
			tooltip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			return tooltip;
		}
	}

	private static JButton textButton(String label)
	{
		JButton b = button(label);
		b.setContentAreaFilled(false);
		b.setBorder(new EmptyBorder(5, 0, 5, 0));
		b.setForeground(BLUE);
		return b;
	}

	private static JButton iconButton(String label, Icon icon)
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

	public static String escape(String value)
	{
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private final class RowPanel extends JPanel
	{
		final GuideRow row;
		final JTextArea status = text("", MUTED, 14, false), feedback = text("", AMBER, 14, false);
		final JPanel feedbackPanel = new JPanel(new BorderLayout(4, 0));
		final JCheckBox check = new JCheckBox("Mark activity complete");
		final JPanel details = vertical();
		final JButton expand = textButton("Details  ▾");
		final JButton openHelper;
		String scope;
		Completion last;
		boolean highlighted;

		RowPanel(GuideRow row)
		{
			this.row = row;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(CARD);
			setAlignmentX(LEFT_ALIGNMENT);
			setBorder(cardBorder(BORDER));
			feedbackPanel.setOpaque(false);
			feedbackPanel.setAlignmentX(LEFT_ALIGNMENT);
			feedbackPanel.add(feedback, BorderLayout.CENTER);
			JButton dismiss = iconButton("Dismiss quest message", new GlyphIcon(true, AMBER));
			dismiss.addActionListener(e -> questMessage(row.getKey(), ""));
			feedbackPanel.add(dismiss, BorderLayout.EAST);
			feedback(questMessages.get(row.getKey()));
			JPanel heading = new JPanel(new BorderLayout(5, 0));
			heading.setOpaque(false);
			heading.setAlignmentX(LEFT_ALIGNMENT);
			heading.add(text(String.format("%03d", row.getPosition() + 1) + " / " + human(row.getKind().name()), MUTED,
				13, false), BorderLayout.CENTER);
			if (row.getKind() == GuideRow.Kind.QUEST || row.getKind() == GuideRow.Kind.MINIQUEST)
			{
				openHelper = button("Open");
				openHelper.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
				openHelper.setForeground(BLUE);
				openHelper.setIcon(questHelperIcon);
				openHelper.setHorizontalTextPosition(SwingConstants.LEFT);
				openHelper.setIconTextGap(4);
				openHelper.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(4, 5, 4, 5)));
				openHelper.getAccessibleContext().setAccessibleName("Open " + row.getTitle() + " in Quest Helper");
				openHelper.setToolTipText("Open " + row.getTitle() + " in Quest Helper");
				openHelper.addActionListener(e -> actions.quest(row));
				heading.add(openHelper, BorderLayout.EAST);
			}
			else
			{
				openHelper = null;
			}
			add(heading);
			add(text(row.getTitle(), TEXT, 16, true));
			add(status);
			if (row.isManual())
			{
				check.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
				check.setOpaque(false);
				check.setForeground(MUTED);
				check.setAlignmentX(LEFT_ALIGNMENT);
				check.setBorder(new EmptyBorder(5, 0, 4, 0));
				check.addActionListener(e -> actions.manual(scope, row, check.isSelected()));
				add(check);
			}
			if (row.getKind() == GuideRow.Kind.TRAINING)
			{
				for (String skill : row.getTargets().keySet())
				{
					JButton link = textButton(
						training.resolve(skill).equals(TrainingGuideResolver.FALLBACK) ? "Browse Theoatrix guides ↗"
							: human(skill) + " guide ↗");
					link.addActionListener(e -> actions.training(skill));
					link.setToolTipText(training.label(skill));
					add(link);
				}
			}
			expand.addActionListener(e -> toggleDetails());
			add(expand);
			details.setVisible(expanded.contains(row.getKey()));
			expand.setText(details.isVisible() ? "Details  ▴" : "Details  ▾");
			row.getFields().forEach((key, value) ->
			{
				if (!value.isBlank() && !value.equals(row.getTitle()))
				{
					details.add(text(
						key.equals("New levels after quest") ? "GUIDE PROJECTED LEVELS" : key.toUpperCase(Locale.ROOT),
						MUTED, 13, true));
					details.add(text(value, TEXT, 14, false));
					details.add(Box.createVerticalStrut(5));
				}
			});
			row.getLinks().forEach((url, label) ->
			{
				if (GuideParser.safeWikiLink(url) != null)
				{
					JButton link = textButton("<html>" + escape(label) + " ↗</html>");
					link.setToolTipText("OSRS Wiki source link");
					link.addActionListener(e -> actions.source(url));
					details.add(link);
				}
			});
			add(details);
			MouseAdapter click = new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1)
					{
						toggleDetails();
					}
				}
			};
			clickableBody(this, click);
			setFocusable(true);
			addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusGained(FocusEvent e)
				{
					setBorder(cardBorder(BLUE));
				}

				@Override
				public void focusLost(FocusEvent e)
				{
					setBorder(cardBorder(highlighted ? BLUE : BORDER));
				}
			});
			getAccessibleContext().setAccessibleName(row.getTitle() + " details");
			getAccessibleContext().setAccessibleDescription(
				"Click the card or press Enter or Space to expand details. Use Open beside the step number to launch Quest Helper.");
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "details");
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "details");
			getActionMap().put("details", new AbstractAction()
			{
				public void actionPerformed(ActionEvent e)
				{
					toggleDetails();
				}
			});
		}

		void toggleDetails()
		{
			scrollIntent++;
			boolean open = !details.isVisible();
			details.setVisible(open);
			expand.setText(open ? "Details  ▴" : "Details  ▾");
			if (open)
			{
				expanded.add(row.getKey());
			}
			else
			{
				expanded.remove(row.getKey());
			}
			body.revalidate();
			repaint();
		}

		void clickableBody(Component component, MouseListener click)
		{
			if (component instanceof AbstractButton)
			{
				return;
			}
			component.addMouseListener(click);
			component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			if (component instanceof Container)
			{
				for (Component child : ((Container)component).getComponents())
				{
					clickableBody(child, click);
				}
			}
		}

		void feedback(String message)
		{
			setText(feedback, message);
			feedbackPanel.setVisible(message != null && !message.isBlank());
		}

		void update(Completion completion, AccountProgress account)
		{
			scope = account == null ? null : account.getScope();
			if (!completion.equals(last))
			{
				setText(status, completion.label());
				status.setForeground(completion.getState() == Completion.State.COMPLETE ? GREEN
					: completion.getState() == Completion.State.IN_PROGRESS ? BLUE : MUTED);
				setBackground(completion.getState() == Completion.State.COMPLETE ? new Color(0x21372F) : CARD);
				last = completion;
			}
			check.setEnabled(account != null);
			boolean selected = account != null && account.getManual().contains(row.getKey());
			if (check.isSelected() != selected)
			{
				check.setSelected(selected);
			}
		}

		void highlight(boolean value)
		{
			if (highlighted == value)
			{
				return;
			}
			highlighted = value;
			setBorder(cardBorder(value ? BLUE : BORDER));
			if (value)
			{
				status
					.setToolTipText("Your next step: in-progress quests first, then the earliest unfinished activity.");
			}
		}

		private Border cardBorder(Color accent)
		{
			return new CompoundBorder(new MatteBorder(1, 3, 1, 1, accent), new EmptyBorder(8, 8, 6, 8));
		}
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

	private static final class GlyphIcon implements Icon
	{
		final boolean close;
		final Color color;

		GlyphIcon(boolean close, Color color)
		{
			this.close = close;
			this.color = color;
		}

		public int getIconWidth()
		{
			return 18;
		}

		public int getIconHeight()
		{
			return 18;
		}

		public void paintIcon(Component c, Graphics original, int x, int y)
		{
			Graphics2D g = (Graphics2D)original.create();
			g.translate(x, y);
			g.setColor(c.isEnabled() ? color : BORDER);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			if (close)
			{
				g.drawLine(5, 5, 13, 13);
				g.drawLine(13, 5, 5, 13);
			}
			else
			{
				g.drawArc(3, 3, 12, 12, 30, 280);
				g.drawLine(15, 2, 15, 7);
				g.drawLine(15, 7, 10, 7);
			}
			g.dispose();
		}
	}
}
