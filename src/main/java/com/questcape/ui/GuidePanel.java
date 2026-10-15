package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import com.questcape.ui.GuideControlIcons.*;
import static com.questcape.ui.UiComponents.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import net.runelite.client.ui.PluginPanel;
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

		void report();

		boolean copyLink(String url);
	}

	static final Color BACKGROUND = UiTheme.BACKGROUND, CARD = UiTheme.CARD, TEXT = UiTheme.TEXT, MUTED = UiTheme.MUTED,
		GREEN = UiTheme.GREEN, BLUE = UiTheme.BLUE, AMBER = UiTheme.AMBER, BORDER = UiTheme.BORDER, GOLD = UiTheme.GOLD,
		PURPLE = UiTheme.PURPLE, TEAL = UiTheme.TEAL, ORANGE = UiTheme.ORANGE;
	private final GuideHeader header;
	private final GuideNavigationBar navigation;
	private final GuideAssets assets = new GuideAssets();
	private final JTextArea notice = text("", AMBER, UiMetrics.BODY, false);
	private final JPanel body, noticePanel = new JPanel(new BorderLayout(4, 0));
	private final GuideRouteView viewport;
	private final JButton boundary, next;
	private final List<GuideRowCard> rows;
	private final Map<GuideRow.Kind, Icon> stepIcons = assets.steps;
	private GuideRow.Kind selectedType;
	private Completion.State selectedState;
	private final List<Component> notes;
	private final GuideSearchBar searchBar = new GuideSearchBar();
	private final GuideFilterBar filters;
	private final GuideSearchController finder;
	private final GuideNavigator navigator;
	private boolean disposed;
	private GuideSnapshot guide;
	private AccountProgress account;
	private boolean progressReady;
	private GuideRowCard progressRow;

	public GuidePanel(Actions actions, TrainingGuideResolver training)
	{
		super(false);
		for (GuideRow.Kind kind : GuideRow.Kind.values())
		{
			stepIcons.put(kind, new StepIcons(kind));
		}
		setLayout(new BorderLayout());
		setBackground(BACKGROUND);
		header = new GuideHeader(actions);

		add(header, BorderLayout.NORTH);

		viewport = new GuideRouteView(actions, assets, training, header.provenance);
		body = viewport.body;
		rows = viewport.rows;
		notes = viewport.notes;
		noticePanel.setOpaque(false);
		noticePanel.setAlignmentX(LEFT_ALIGNMENT);
		noticePanel.add(notice, BorderLayout.CENTER);
		JButton dismiss = iconButton("Dismiss message", new GlyphIcon(true, AMBER));
		dismiss.addActionListener(e -> message(""));
		noticePanel.add(dismiss, BorderLayout.EAST);
		noticePanel.setVisible(false);
		header.add(noticePanel);
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
		navigation = new GuideNavigationBar(this::jumpBoundary, this::jumpProgress);
		boundary = navigation.boundary;
		next = navigation.next;
		navigator = new GuideNavigator(viewport, body, rows, navigation);
		filters = new GuideFilterBar(stepIcons, kind ->
		{
			selectedType = kind;
			finderFiltersChanged();
		},
			state ->
			{
				selectedState = state;
				finderFiltersChanged();
			}, this::resetFilters);
		finder = new GuideSearchController(searchBar, rows, navigator, this::applyFilters, this::updateFilterStatus);
		navigator.onUserNavigation = finder::userNavigated;
		header.add(searchBar);
		header.add(filters);
		add(navigation, BorderLayout.SOUTH);
		bind("ctrl HOME", "routeTop", () -> jumpTo(0));
		bind("ctrl END", "routeBottom", () -> jumpTo(Integer.MAX_VALUE));
		bind("ctrl J", "routeNext", this::jumpProgress);
		updateNavigation();
	}

	private void finderFiltersChanged()
	{
		finder.filtersChanged();
	}

	private void resetFilters()
	{
		finder.reset(() ->
		{
			selectedType = null;
			selectedState = null;
			filters.allTypes.setSelected(true);
			filters.allStates.setSelected(true);
		});
	}

	void searchNow()
	{
		finder.searchNow();
	}

	void nextSearchMatch(long now)
	{
		finder.nextSearchMatch(now);
	}

	private void applyFilters()
	{
		GuideProjection projection = GuideProjection.of(guide, account, selectedType, selectedState);
		boolean active = selectedType != null || selectedState != null;
		boolean changed = false;
		progressRow = null;
		for (int i = 0; i < rows.size(); i++)
		{
			GuideRowCard card = rows.get(i);
			GuideProjection.Entry entry = projection.getEntries().get(i);
			card.update(entry, account);
			boolean visible = entry.isVisible();
			changed |= card.isVisible() != visible;
			card.setVisible(visible);
			card.spacer.setVisible(visible);
			if (visible && progressReady && card.scope != null && card.last.getState() != Completion.State.COMPLETE
				&& progressRow == null)
			{
				progressRow = card;
			}
		}
		for (Component note : notes)
		{
			changed |= note.isVisible() == active;
			note.setVisible(!active);
		}
		for (GuideRowCard card : rows)
		{
			card.highlight(card == progressRow);
		}
		filters.types.setIcon(new FilterIcon(false, selectedType != null));
		filters.states.setIcon(new FilterIcon(true, selectedState != null));
		filters.types
			.setToolTipText("Filter by step type: " + (selectedType == null ? "All" : human(selectedType.name())));
		filters.states.setToolTipText(
			"Filter by progress state: " + (selectedState == null ? "All" : human(selectedState.name())));
		int total = projection.getEntries().size();
		setText(header.summary,
			guide == null ? "Progress unavailable"
				: projection.getComplete() + " of " + total + " complete"
					+ (projection.getUnknown() > 0 ? "\n" + projection.getUnknown() + " unknown" : ""));
		header.progress.setValue(total == 0 ? 0 : projection.getComplete() * 100 / total);
		header.progress.setToolTipText(projection.getComplete() + " completed; " + projection.getUnknown()
			+ " unknown. Totals cover the entire route.");
		finder.visibilityChanged(changed);
		if (changed)
		{
			body.revalidate();
			body.repaint();
		}
		navigator.progressRow = progressRow;
		updateNavigation();
	}

	private void updateFilterStatus(String result)
	{
		long visible = rows.stream().filter(Component::isVisible).count();
		setText(filters.filterStatus, visible + " of " + rows.size() + " steps shown");
		setText(searchBar.searchFeedback, result.replaceFirst("^ · ", ""));
		filters.reset
			.setEnabled(selectedType != null || selectedState != null || !searchBar.search.getText().isEmpty());
	}

	public void dispose()
	{
		requireEdt();
		disposed = true;
		LinkCopyGesture.disposeTree(this);
		finder.dispose();
		filters.dispose();
		navigator.disposed = true;
		rows.forEach(GuideRowCard::dispose);
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
		render(snapshot, progress, SidebarStatus.idle(contentStatus, syncStatus), canSync);
	}

	public void render(GuideSnapshot snapshot, AccountProgress progress, SidebarStatus status, boolean canSync)
	{
		requireEdt();
		if (disposed)
		{
			return;
		}
		if (!canSync)
		{
			progress = null;
		}
		navigator.applying = true;
		try
		{
			header.update(snapshot, progress, status, canSync);
			boolean structural = guide == null ? snapshot != null
				: snapshot == null || !guide.getRows().equals(snapshot.getRows());
			if (structural)
			{
				GuideNavigator.ScrollAnchor anchor = navigator.captureAnchor();
				viewport.replace(snapshot, (changed, open) -> navigator.disclosed(changed), navigator::userNavigated);
				navigator.restoreAfterLayout(anchor);
			}
			guide = snapshot;
			account = progress;
			progressReady = canSync;
			applyFilters();
			if (structural && !searchBar.search.getText().isBlank())
			{
				finder.searchChanged();
			}
		}
		finally
		{
			navigator.applying = false;
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
			header.revalidate();
		}
	}

	public void setStepSprite(GuideRow.Kind kind, BufferedImage sprite)
	{
		requireEdt();
		if (disposed || sprite == null)
		{
			return;
		}
		Icon icon = new ImageIcon(sprite.getWidth() == 16 && sprite.getHeight() == 16 ? sprite
			: net.runelite.client.util.ImageUtil.resizeImage(sprite, 16, 16));
		stepIcons.put(kind, icon);
		if (kind == GuideRow.Kind.QUEST)
		{
			stepIcons.put(GuideRow.Kind.MINIQUEST, new StepIcons(GuideRow.Kind.MINIQUEST, icon));
		}
		for (GuideRowCard row : rows)
		{
			row.stepIcon.setIcon(stepIcons.get(row.row.getKind()));
		}
		filters.typeOptions.forEach((type, option) -> option.setIcon(stepIcons.get(type)));
	}

	public void setSkillIcons(Map<Skill, BufferedImage> images, Map<Skill, BufferedImage> projections)
	{
		requireEdt();
		if (!disposed)
		{
			assets.update(images, projections);
		}
	}

	public void setHeaderIcons(BufferedImage plugin, BufferedImage github)
	{
		requireEdt();
		if (!disposed)
		{
			header.assets(plugin, github);
		}
	}

	JButton clearSearchButton()
	{
		return searchBar.clearSearch;
	}

	JButton reportButton()
	{
		return header.report;
	}

	String searchMessage()
	{
		return searchBar.searchFeedback.getText();
	}

	JTextField searchField()
	{
		return searchBar.search;
	}

	JRadioButtonMenuItem typeOption(GuideRow.Kind kind)
	{
		return filters.typeOptions.get(kind);
	}

	JRadioButtonMenuItem stateOption(Completion.State state)
	{
		return filters.stateOptions.get(state);
	}

	JButton resetButton()
	{
		return filters.reset;
	}

	JButton searchMatchesButton()
	{
		return searchBar.searchMatches;
	}

	JButton typeFilterButton()
	{
		return filters.types;
	}

	JButton stateFilterButton()
	{
		return filters.states;
	}

	JRadioButtonMenuItem allTypeOption()
	{
		return filters.allTypes;
	}

	JRadioButtonMenuItem allStateOption()
	{
		return filters.allStates;
	}

	long visibleRowCount()
	{
		return rows.stream().filter(Component::isVisible).count();
	}

	boolean searchPending()
	{
		return finder.searchTimer.isRunning();
	}

	String filterMessage()
	{
		return filters.filterStatus.getText();
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
		navigator.jumpBoundary();
	}

	private void jumpProgress()
	{
		navigator.jumpProgress();
	}

	private void jumpTo(int y)
	{
		navigator.jumpTo(y);
	}

	private void updateNavigation()
	{
		navigator.updateNavigation();
	}

	private void userNavigated()
	{
		navigator.userNavigated();
	}

	static Color kindColor(GuideRow.Kind kind)
	{
		return UiTheme.kindColor(kind);
	}

	static Color stateColor(Completion.State state)
	{
		return UiTheme.stateColor(state);
	}
}
