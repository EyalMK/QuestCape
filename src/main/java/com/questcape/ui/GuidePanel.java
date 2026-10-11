package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import com.questcape.ui.GuideControlIcons.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.DefaultCaret;
import javax.swing.text.View;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.Skill;

/** Native, inert Swing UI. Only user navigation and structural edits move its viewport. */
public class GuidePanel extends PluginPanel
{
	public interface Actions
	{
		void syncPlayer();

		void refresh();

		void training(String skill);

		void manual(String scope, GuideRow row, boolean checked);

		void source(String url);
	}

	static final Color BACKGROUND = new Color(0x171C23), CARD = new Color(0x232B35), TEXT = new Color(0xECF1F7),
		MUTED = new Color(0xA5B3C4), GREEN = new Color(0x75D9A2), BLUE = new Color(0x84BFFF),
		AMBER = new Color(0xF2C479), BORDER = new Color(0x374555), GOLD = new Color(0xF2CC60),
		PURPLE = new Color(0xBC9CE8), TEAL = new Color(0x64C7C2), ORANGE = new Color(0xE6A36A);
	private final Actions actions;
	private final TrainingGuideResolver training;
	private final JTextArea accountName = text("Log in to get started", TEXT, 16, true),
		accountStatus = text("Your progress syncs automatically.", MUTED, 14, false),
		guideStatus = text("Loading route…", MUTED, 13, false), summary = text("Progress unavailable", TEXT, 14, false),
		notice = text("", AMBER, 14, false),
		provenance = text("", MUTED, 13, false);
	private final JButton syncPlayer = iconButton("Sync current player", new GlyphIcon(false, BLUE));
	private final JProgressBar progressBar = new JProgressBar(0, 100)
	{
		@Override
		public JToolTip createToolTip()
		{
			JToolTip tooltip = super.createToolTip();
			tooltip.setFont(font(14, false));
			return tooltip;
		}
	};
	private final JPanel body = new WidthTrackingPanel(), rowsPanel = vertical(),
		noticePanel = new JPanel(new BorderLayout(4, 0));
	private final JScrollPane viewport = new JScrollPane(body);
	private final JButton boundary = button("Bottom ↓"), next = button("Continue");
	private final List<RowPanel> rows = new ArrayList<>();
	private final Set<String> expanded = new HashSet<>();
	private final Map<GuideRow.Kind, Icon> stepIcons = new EnumMap<>(GuideRow.Kind.class);
	private final Map<Skill, Icon> skillIcons = new EnumMap<>(Skill.class);
	private final Map<Skill, Icon> projectedIcons = new EnumMap<>(Skill.class);
	private final Map<Skill, List<JLabel>> projectedLabels = new EnumMap<>(Skill.class);
	private final List<SkillText> additionalInfo = new ArrayList<>();
	private GuideRow.Kind selectedType;
	private Completion.State selectedState;
	private final Map<GuideRow.Kind, JRadioButtonMenuItem> typeOptions = new EnumMap<>(GuideRow.Kind.class);
	private final Map<Completion.State, JRadioButtonMenuItem> stateOptions = new EnumMap<>(Completion.State.class);
	private final JRadioButtonMenuItem allTypes = new JRadioButtonMenuItem("All step types", true),
		allStates = new JRadioButtonMenuItem("All progress states", true);
	private final List<Component> notes = new ArrayList<>();
	private final JTextField search = new JTextField()
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
	private final JButton types = iconButton("Filter by step type", new FilterIcon(false, false)),
		states = iconButton("Filter by progress state", new FilterIcon(true, false)),
		reset = iconButton("Reset filters and search", new ResetIcon());
	private final JButton searchMatches = iconButton("Next search match", new ChevronIcon());
	private final List<RowPanel> matches = new ArrayList<>();
	private int matchIndex = -1;
	private String matchedQuery = "";
	private ScrollAnchor searchOrigin;
	private long wrapArmedAt;
	private final JTextArea filterStatus = text("", MUTED, 13, false);
	private final JPopupMenu typeMenu = new JPopupMenu(), stateMenu = new JPopupMenu();
	private final javax.swing.Timer searchTimer = new javax.swing.Timer(300, e -> searchNow());
	private long searchIntent;
	private boolean resetting, disposed;
	private GuideSnapshot guide;
	private RowPanel progressRow;
	private boolean applying;
	private long scrollIntent;

	public GuidePanel(Actions actions, TrainingGuideResolver training)
	{
		super(false);
		this.actions = actions;
		this.training = training;
		for (GuideRow.Kind kind : GuideRow.Kind.values())
		{
			stepIcons.put(kind, new StepIcons(kind));
		}
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
		progressBar.setFont(font(13, true));
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(200, 22));
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		progressBar.getAccessibleContext().setAccessibleName("Completed guide steps");
		header.add(progressBar);
		header.add(Box.createVerticalStrut(8));
		header.add(filterControls());
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
		body.add(footer);
		viewport.setBorder(null);
		viewport.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		viewport.getVerticalScrollBar().setUnitIncrement(24);
		viewport.getViewport().setBackground(BACKGROUND);
		viewport.getVerticalScrollBar().addAdjustmentListener(e -> updateNavigation());
		viewport.addMouseWheelListener(e -> userNavigated());
		viewport.getVerticalScrollBar().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				userNavigated();
			}
		});
		add(viewport, BorderLayout.CENTER);
		JPanel navigation = new JPanel(new BorderLayout(8, 0));
		navigation.setBackground(BACKGROUND);
		navigation.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(8, 10, 8, 10)));
		boundary.addActionListener(e -> jumpBoundary());
		next.addActionListener(e -> jumpProgress());
		next.setForeground(BLUE);
		next.setFont(font(13, true));
		next.setIcon(new ContinueIcon());
		next.setIconTextGap(4);
		next.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(5, 6, 5, 6)));
		JPanel resume = new JPanel(new GridBagLayout());
		resume.setOpaque(false);
		resume.add(next);
		navigation.add(boundary, BorderLayout.CENTER);
		navigation.add(resume, BorderLayout.EAST);
		add(navigation, BorderLayout.SOUTH);
		bind("ctrl HOME", "routeTop", () -> jumpTo(0));
		bind("ctrl END", "routeBottom", () -> jumpTo(Integer.MAX_VALUE));
		bind("ctrl J", "routeNext", this::jumpProgress);
		updateNavigation();
	}

	private JPanel filterControls()
	{
		JPanel controls = vertical();
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
		searchMatches.addActionListener(e -> nextSearchMatch(System.nanoTime()));
		search.addActionListener(e -> nextSearchMatch(System.nanoTime()));
		searchBox.add(search, BorderLayout.CENTER);
		searchBox.add(searchMatches, BorderLayout.EAST);
		controls.add(searchBox);
		controls.add(Box.createVerticalStrut(5));
		ButtonGroup typeGroup = new ButtonGroup(), stateGroup = new ButtonGroup();
		for (JPopupMenu menu : List.of(typeMenu, stateMenu))
		{
			menu.setBackground(CARD);
			menu.setBorder(new LineBorder(BORDER));
		}
		allTypes.setIcon(new FilterIcon(false, false));
		styleOption(allTypes, MUTED, 12);
		allStates.setIcon(new FilterIcon(true, false));
		styleOption(allStates, MUTED, 4);
		typeGroup.add(allTypes);
		stateGroup.add(allStates);
		typeMenu.add(allTypes);
		stateMenu.add(allStates);
		typeMenu.addSeparator();
		stateMenu.addSeparator();
		allTypes.addActionListener(e -> { selectedType = null; filtersChanged(); });
		allStates.addActionListener(e -> { selectedState = null; filtersChanged(); });
		for (GuideRow.Kind kind : GuideRow.Kind.values())
		{
			if (kind == GuideRow.Kind.INFORMATION)
			{
				continue;
			}
			JRadioButtonMenuItem option = new JRadioButtonMenuItem(human(kind.name()), stepIcons.get(kind));
			styleOption(option, TEXT, 12);
			option.addActionListener(e ->
			{
				selectedType = kind;
				filtersChanged();
			});
			typeGroup.add(option);
			typeOptions.put(kind, option);
			typeMenu.add(option);
		}
		for (Completion.State state : List.of(Completion.State.COMPLETE, Completion.State.INCOMPLETE,
			Completion.State.IN_PROGRESS, Completion.State.UNKNOWN))
		{
			JRadioButtonMenuItem option = new JRadioButtonMenuItem(human(state.name()), new StateIcon(state));
			styleOption(option, stateColor(state), 4);
			option.addActionListener(e ->
			{
				selectedState = state;
				filtersChanged();
			});
			stateGroup.add(option);
			stateOptions.put(state, option);
			stateMenu.add(option);
		}
		types.addActionListener(e -> typeMenu.show(types, 0, types.getHeight()));
		states.addActionListener(e -> stateMenu.show(states, 0, states.getHeight()));
		JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
		buttons.setOpaque(false);
		buttons.add(types);
		buttons.add(states);
		buttons.add(reset);
		buttons.setAlignmentX(LEFT_ALIGNMENT);
		controls.add(buttons);
		controls.add(filterStatus);
		reset.addActionListener(e -> resetFilters());
		searchTimer.setRepeats(false);
		search.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				searchChanged();
			}
			public void removeUpdate(DocumentEvent e)
			{
				searchChanged();
			}
			public void changedUpdate(DocumentEvent e)
			{
				searchChanged();
			}
		});
		return controls;
	}

	private static void styleOption(JRadioButtonMenuItem option, Color color, int gap)
	{
		option.setFont(font(14, false));
		option.setForeground(color);
		option.setBackground(CARD);
		option.setBorder(new EmptyBorder(6, 8, 6, 8));
		option.setIconTextGap(gap);
	}

	private void searchChanged()
	{
		searchTimer.stop();
		if (resetting || disposed)
		{
			return;
		}
		String query = StepSearch.normalize(search.getText());
		if (!query.isEmpty() && searchOrigin == null)
		{
			searchOrigin = captureAnchor();
		}
		searchIntent = ++scrollIntent;
		clearMatches();
		updateFilterStatus("");
		if (!query.isEmpty())
		{
			searchMatches.setText("…");
			searchMatches.setVisible(true);
			searchMatches.setEnabled(false);
			searchTimer.restart();
		}
		else
		{
			restoreSearchOrigin();
		}
	}

	private void filtersChanged()
	{
		searchTimer.stop();
		wrapArmedAt = 0;
		if (StepSearch.normalize(search.getText()).isEmpty())
		{
			searchOrigin = null;
		}
		scrollIntent++;
		applyFilters();
		long intent = scrollIntent;
		SwingUtilities.invokeLater(() ->
		{
			if (disposed || intent != scrollIntent)
			{
				return;
			}
			body.validate();
			if (StepSearch.normalize(search.getText()).isEmpty())
			{
				jumpTo(0);
			}
			else
			{
				searchIntent = scrollIntent;
				searchNow();
			}
		});
	}

	private void applyFilters()
	{
		boolean active = selectedType != null || selectedState != null;
		boolean changed = false;
		progressRow = null;
		for (RowPanel card : rows)
		{
			boolean visible = (selectedType == null || selectedType == card.row.getKind())
				&& (selectedState == null || card.last != null && selectedState == card.last.getState());
			changed |= card.isVisible() != visible;
			card.setVisible(visible);
			card.spacer.setVisible(visible);
			if (visible && card.scope != null && card.last.getState() != Completion.State.COMPLETE && progressRow == null)
			{
				progressRow = card;
			}
		}
		for (Component note : notes)
		{
			changed |= note.isVisible() == active;
			note.setVisible(!active);
		}
		for (RowPanel card : rows)
		{
			card.highlight(card == progressRow);
		}
		types.setIcon(new FilterIcon(false, selectedType != null));
		states.setIcon(new FilterIcon(true, selectedState != null));
		types.setToolTipText("Filter by step type: " + (selectedType == null ? "All" : human(selectedType.name())));
		states.setToolTipText("Filter by progress state: " + (selectedState == null ? "All" : human(selectedState.name())));
		if (!searchTimer.isRunning() && !matchedQuery.isEmpty())
		{
			if (changed)
			{
				refreshMatches(true);
			}
			else
			{
				updateMatchStatus();
			}
		}
		else
		{
			updateFilterStatus("");
		}
		if (changed)
		{
			body.revalidate();
			body.repaint();
		}
		updateNavigation();
	}

	private void updateFilterStatus(String result)
	{
		long visible = rows.stream().filter(Component::isVisible).count();
		setText(filterStatus, visible + " of " + rows.size() + " steps" + result);
		reset.setEnabled(selectedType != null || selectedState != null || !search.getText().isEmpty());
	}

	void searchNow()
	{
		requireEdt();
		searchTimer.stop();
		if (disposed || searchIntent != scrollIntent || StepSearch.normalize(search.getText()).isEmpty())
		{
			return;
		}
		int best = refreshMatches(false);
		if (!matches.isEmpty())
		{
			showMatch(best);
		}
	}

	private int refreshMatches(boolean preserveCurrent)
	{
		String key = preserveCurrent && matchIndex >= 0 && matchIndex < matches.size()
			? matches.get(matchIndex).row.getKey() : null;
		matches.clear();
		matchedQuery = StepSearch.normalize(search.getText());
		RowPanel best = null;
		int bestScore = Integer.MAX_VALUE;
		for (RowPanel card : rows)
		{
			if (!card.isVisible())
			{
				continue;
			}
			int candidate = StepSearch.score(card.row.getTitle(), search.getText());
			if (candidate < Integer.MAX_VALUE)
			{
				matches.add(card);
				if (candidate < bestScore)
				{
					best = card;
					bestScore = candidate;
				}
			}
		}
		matches.sort(Comparator.comparingInt(card -> card.stepNumber));
		matchIndex = -1;
		for (int i = 0; i < matches.size(); i++)
		{
			if (matches.get(i).row.getKey().equals(key))
			{
				matchIndex = i;
				break;
			}
		}
		wrapArmedAt = 0;
		updateMatchStatus();
		return matches.indexOf(best);
	}

	private void showMatch(int index)
	{
		matchIndex = index;
		wrapArmedAt = 0;
		RowPanel card = matches.get(index);
		updateMatchStatus();
		jumpTo(SwingUtilities.convertPoint(card.getParent(), card.getLocation(), body).y);
	}

	/** Both the field's Enter action and the inline chevron use the same guarded traversal. */
	void nextSearchMatch(long now)
	{
		requireEdt();
		if (disposed || StepSearch.normalize(search.getText()).isEmpty())
		{
			return;
		}
		if (searchTimer.isRunning() || !matchedQuery.equals(StepSearch.normalize(search.getText())))
		{
			searchIntent = scrollIntent;
			searchNow();
			return;
		}
		if (matches.isEmpty())
		{
			return;
		}
		if (matchIndex < matches.size() - 1)
		{
			showMatch(matchIndex + 1);
		}
		else if (matches.size() > 1)
		{
			if (wrapArmedAt != 0 && now - wrapArmedAt >= 0 && now - wrapArmedAt <= 500_000_000L)
			{
				showMatch(0);
			}
			else
			{
				wrapArmedAt = now;
				updateFilterStatus(" · Press Enter twice to return to the first match");
			}
		}
	}

	private void updateMatchStatus()
	{
		searchMatches.setVisible(!matchedQuery.isEmpty());
		searchMatches.setText((matchIndex + 1) + "/" + matches.size());
		searchMatches.setEnabled(!matches.isEmpty());
		searchMatches.getAccessibleContext().setAccessibleName("Search match " + (matchIndex + 1) + " of " + matches.size());
		searchMatches.setToolTipText("Next match (Enter). At the last match, press Enter twice within 500 ms to wrap.");
		updateFilterStatus(matches.isEmpty() ? " · No matching step"
			: matchIndex >= 0 ? " · Step " + matches.get(matchIndex).stepNumber : "");
	}

	private void clearMatches()
	{
		matches.clear();
		matchedQuery = "";
		matchIndex = -1;
		wrapArmedAt = 0;
		searchMatches.setVisible(false);
	}

	private void restoreSearchOrigin()
	{
		ScrollAnchor origin = searchOrigin;
		if (origin != null)
		{
			restoreAfterLayout(new ScrollAnchor(origin.key, origin.offset, origin.absolute, scrollIntent), true);
		}
	}

	private void userNavigated()
	{
		scrollIntent++;
		wrapArmedAt = 0;
		if (StepSearch.normalize(search.getText()).isEmpty())
		{
			searchOrigin = null;
		}
		if (searchTimer.isRunning())
		{
			searchTimer.stop();
			refreshMatches(false);
		}
	}

	private void resetFilters()
	{
		resetting = true;
		searchTimer.stop();
		selectedType = null;
		selectedState = null;
		allTypes.setSelected(true);
		allStates.setSelected(true);
		search.setText("");
		clearMatches();
		resetting = false;
		scrollIntent++;
		applyFilters();
		if (searchOrigin != null)
		{
			restoreSearchOrigin();
		}
		else
		{
			jumpTo(0);
		}
	}

	public void dispose()
	{
		requireEdt();
		disposed = true;
		searchTimer.stop();
		clearMatches();
		searchOrigin = null;
		typeMenu.setVisible(false);
		stateMenu.setVisible(false);
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
		String contentStatus, String syncStatus, boolean canSync)
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
				: "OSRS Wiki · " + snapshot.getRows().stream().filter(GuideRow::isActionable).count() + " steps";
			if (contentStatus != null && contentStatus.startsWith("Checking"))
			{
				freshness = "Refreshing route…";
			}
			else if (contentStatus != null && (contentStatus.contains("failed") || contentStatus.contains("busy")))
			{
				freshness += " · cached";
			}
			setText(guideStatus, freshness);
			if (contentStatus != null && contentStatus.startsWith("Route unsaved"))
			{
				setText(guideStatus, freshness + " · unsaved");
			}
			guideStatus.setToolTipText(contentStatus);
			String detail = snapshot == null ? ""
				: "Revision " + snapshot.getRevision() + "\nGuide retrieved " + time(snapshot.getRetrievedAt())
					+ "\nValidated " + time(snapshot.getValidatedAt());
			if (progress != null)
			{
				detail += "\nCharacter observed " + time(progress.getRetrievedAt())
					+ (syncStatus != null && syncStatus.contains("unsaved")
						? "\nLive progress is unsaved." : "\nStored locally on this computer.");
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
				notes.clear();
				projectedLabels.clear();
				additionalInfo.clear();
				rowsPanel.removeAll();
				if (snapshot != null)
				{
					int stepNumber = 0;
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
						RowPanel card = new RowPanel(row, ++stepNumber);
						rows.add(card);
						rowsPanel.add(card);
						rowsPanel.add(card.spacer);
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
				total++;
				if (completion.getState() == Completion.State.COMPLETE)
				{
					complete++;
				}
				if (completion.getState() == Completion.State.UNKNOWN)
				{
					unknown++;
				}
				if (progress != null && completion.getState() != Completion.State.COMPLETE && progressRow == null)
				{
					progressRow = card;
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
				.setToolTipText(complete + " completed; " + unknown + " unknown. Totals cover the entire route.");
			applyFilters();
			if (structural && !search.getText().isBlank())
			{
				searchChanged();
			}
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
	}

	public void setStepSprite(GuideRow.Kind kind, BufferedImage sprite)
	{
		requireEdt();
		Icon icon = new ImageIcon(ImageUtil.resizeImage(sprite, 16, 16));
		stepIcons.put(kind, icon);
		if (kind == GuideRow.Kind.QUEST)
		{
			stepIcons.put(GuideRow.Kind.MINIQUEST, new StepIcons(GuideRow.Kind.MINIQUEST, icon));
		}
		for (RowPanel row : rows)
		{
			row.stepIcon.setIcon(stepIcons.get(row.row.getKind()));
		}
		typeOptions.forEach((type, option) -> option.setIcon(stepIcons.get(type)));
	}

	public void setSkillIcons(Map<Skill, BufferedImage> images, Map<Skill, BufferedImage> projections)
	{
		requireEdt();
		images.forEach((skill, image) -> skillIcons.put(skill, new ImageIcon(image)));
		projections.forEach((skill, image) -> projectedIcons.put(skill, new ImageIcon(image)));
		projectedLabels.forEach((skill, labels) -> labels.forEach(label -> label.setIcon(projectedIcons.get(skill))));
		additionalInfo.forEach(text -> text.updateIcons(skillIcons));
	}

	JTextField searchField()
	{
		return search;
	}
	JRadioButtonMenuItem typeOption(GuideRow.Kind kind)
	{
		return typeOptions.get(kind);
	}
	JRadioButtonMenuItem stateOption(Completion.State state)
	{
		return stateOptions.get(state);
	}
	JButton resetButton()
	{
		return reset;
	}
	JButton searchMatchesButton()
	{
		return searchMatches;
	}
	JButton typeFilterButton()
	{
		return types;
	}
	JButton stateFilterButton()
	{
		return states;
	}
	JRadioButtonMenuItem allTypeOption()
	{
		return allTypes;
	}
	JRadioButtonMenuItem allStateOption()
	{
		return allStates;
	}
	long visibleRowCount()
	{
		return rows.stream().filter(Component::isVisible).count();
	}
	boolean searchPending()
	{
		return searchTimer.isRunning();
	}
	String filterMessage()
	{
		return filterStatus.getText();
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
		userNavigated();
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
			next.setEnabled(false);
			next.setToolTipText("No visible unfinished step. Adjust filters or log in.");
			return;
		}
		int y = SwingUtilities.convertPoint(progressRow.getParent(), progressRow.getLocation(), body).y;
		int target = Math.max(0, Math.min(y, end));
		next.setEnabled(Math.abs(target - bar.getValue()) > 1);
		next.setToolTipText(
			"Step " + progressRow.stepNumber + ": " + progressRow.row.getTitle() + " (Ctrl+J)");
	}

	private ScrollAnchor captureAnchor()
	{
		int y = viewport.getVerticalScrollBar().getValue();
		for (RowPanel row : rows)
		{
			if (!row.isVisible())
			{
				continue;
			}
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
		restoreAfterLayout(anchor, false);
	}

	private void restoreAfterLayout(ScrollAnchor anchor, boolean endSearch)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (disposed || scrollIntent != anchor.intent)
			{
				return;
			}
			if (endSearch)
			{
				searchOrigin = null;
			}
			body.validate();
			int value = anchor.absolute;
			for (RowPanel row : rows)
			{
				if (row.isVisible() && row.row.getKey().equals(anchor.key))
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

	private static Font font(int size, boolean bold)
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

	private static JButton button(String label)
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

	static Color kindColor(GuideRow.Kind kind)
	{
		switch (kind)
		{
		case UNLOCK:
			return PURPLE;
		case UNKNOWN:
			return MUTED;
		case DIARY:
			return GREEN;
		case TRAINING:
			return TEAL;
		case ACTIVITY:
			return ORANGE;
		default:
			return BORDER;
		}
	}

	static Color stateColor(Completion.State state)
	{
		return state == Completion.State.COMPLETE ? GREEN : state == Completion.State.IN_PROGRESS ? GOLD : MUTED;
	}

	private static JPanel comment(GuideRow row)
	{
		JPanel comment = vertical();
		comment.setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, BORDER), new EmptyBorder(12, 2, 12, 2)));
		comment.add(text(row.getTitle(), MUTED, 14, false));
		comment.getAccessibleContext().setAccessibleName("Guide note: " + row.getTitle());
		return comment;
	}

	private JComponent projectedLevels(String value)
	{
		List<ProjectedLevels.Entry> entries = ProjectedLevels.parse(value);
		if (entries.isEmpty())
		{
			return text(value, TEXT, 14, false);
		}
		JPanel levels = new JPanel(new GridLayout(0, 3, 6, 5));
		levels.setOpaque(false);
		levels.setAlignmentX(LEFT_ALIGNMENT);
		for (ProjectedLevels.Entry entry : entries)
		{
			JLabel label = new JLabel(String.valueOf(entry.level),
				projectedIcons.getOrDefault(entry.skill, stepIcons.get(GuideRow.Kind.TRAINING)), SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setFont(font(14, false));
			label.setForeground(TEXT);
			label.setToolTipText(entry.skill.getName() + ": guide projected level " + entry.level);
			label.getAccessibleContext().setAccessibleName("Guide projected " + entry.skill.getName() + " level " + entry.level);
			projectedLabels.computeIfAbsent(entry.skill, skill -> new ArrayList<>()).add(label);
			levels.add(label);
		}
		return levels;
	}

	private JComponent additionalInfo(String value)
	{
		SkillText text = new SkillText(value, font(14, false), TEXT, skillIcons);
		additionalInfo.add(text);
		return text;
	}

	private final class RowPanel extends JPanel
	{
		final GuideRow row;
		final int stepNumber;
		final JLabel stepIcon = new JLabel();
		final JTextArea status = text("", MUTED, 14, false);
		final JCheckBox check = new JCheckBox("Mark step complete");
		final JPanel details = vertical();
		final Component spacer = Box.createVerticalStrut(9);
		final JButton expand = textButton("Details  ▾");
		String scope;
		Completion last;
		boolean highlighted;

		RowPanel(GuideRow row, int stepNumber)
		{
			this.row = row;
			this.stepNumber = stepNumber;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(CARD);
			setAlignmentX(LEFT_ALIGNMENT);
			updateBorder();
			JPanel heading = new JPanel(new BorderLayout(5, 0));
			heading.setOpaque(false);
			heading.setAlignmentX(LEFT_ALIGNMENT);
			JPanel metadata = new JPanel(new BorderLayout(5, 0));
			metadata.setOpaque(false);
			stepIcon.setIcon(stepIcons.get(row.getKind()));
			stepIcon.setVerticalAlignment(SwingConstants.TOP);
			stepIcon.setToolTipText(human(row.getKind().name()));
			stepIcon.getAccessibleContext().setAccessibleName(human(row.getKind().name()) + " icon");
			metadata.add(stepIcon, BorderLayout.WEST);
			metadata.add(text(String.format("%03d", stepNumber) + " / " + human(row.getKind().name()), MUTED,
				13, false), BorderLayout.CENTER);
			heading.add(metadata, BorderLayout.CENTER);
			add(heading);
			add(text(row.getTitle(), TEXT, 16, true));
			add(status);
			if (row.isManual())
			{
				check.setFont(font(13, false));
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
						training.resolve(skill).equals(TrainingGuideResolver.FALLBACK) ? "Theoatrix guides ↗"
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
					details.add(key.equals("New levels after quest") ? projectedLevels(value)
						: key.equals("Additional info") ? additionalInfo(value) : text(value, TEXT, 14, false));
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
					updateBorder();
				}

				@Override
				public void focusLost(FocusEvent e)
				{
					updateBorder();
				}
			});
			getAccessibleContext().setAccessibleName(row.getTitle() + " details");
			getAccessibleContext().setAccessibleDescription(
				"Click the card or press Enter or Space to expand details.");
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
			userNavigated();
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

		void update(Completion completion, AccountProgress account)
		{
			scope = account == null ? null : account.getScope();
			if (!completion.equals(last))
			{
				setText(status, completion.label());
				status.setForeground(stateColor(completion.getState()));
				setBackground(completion.getState() == Completion.State.COMPLETE ? new Color(0x21372F) : CARD);
				last = completion;
				updateBorder();
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
			updateBorder();
			status.setToolTipText(value
				? "Your next step: the earliest unfinished step in route order." : null);
		}

		private void updateBorder()
		{
			Color accent = kindColor(row.getKind());
			if (last != null && last.getState() == Completion.State.IN_PROGRESS)
			{
				accent = GOLD;
			}
			else if ((highlighted || isFocusOwner())
				&& (row.getKind() == GuideRow.Kind.QUEST || row.getKind() == GuideRow.Kind.MINIQUEST))
			{
				accent = BLUE;
			}
			setBorder(cardBorder(accent));
		}

		private Border cardBorder(Color accent)
		{
			int outline = isFocusOwner() ? 2 : 1;
			return new CompoundBorder(new MatteBorder(outline, 3, outline, outline, accent),
				new EmptyBorder(9 - outline, 8, 7 - outline, 9 - outline));
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

	private static final class ResetIcon implements Icon
	{
		public int getIconWidth()
		{
			return 18;
		}
		public int getIconHeight()
		{
			return 18;
		}
		public void paintIcon(Component component, Graphics original, int x, int y)
		{
			Graphics2D g = (Graphics2D)original.create();
			try
			{
				g.translate(x, y);
				g.setColor(component.isEnabled() ? MUTED : BORDER);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.drawArc(3, 3, 12, 12, 40, 285);
				g.drawLine(2, 2, 2, 7);
				g.drawLine(2, 7, 7, 7);
				g.drawLine(7, 7, 11, 11);
				g.drawLine(11, 7, 7, 11);
			}
			finally
			{
				g.dispose();
			}
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
