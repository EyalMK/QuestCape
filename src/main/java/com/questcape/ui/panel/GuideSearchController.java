package com.questcape.ui.panel;

import com.questcape.ui.panel.GuideNavigator.ScrollAnchor;
import com.questcape.ui.style.UiCopy;
import com.questcape.ui.style.UiMetrics;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.*;

import static com.questcape.ui.style.UiComponents.requireEdt;

/** Debounced local find/traversal, saved origin and wrap belong to one EDT owner. */
final class GuideSearchController
{
	final JTextField search;
	final JButton searchMatches, clearSearch;
	final List<GuideRowCard> rows, matches = new ArrayList<>();
	final GuideNavigator navigator;
	final Runnable applyFilters;
	final Consumer<String> feedback;
	final javax.swing.Timer searchTimer = new javax.swing.Timer(UiMetrics.DEBOUNCE_MS, e -> searchNow());
	int matchIndex = -1;
	String matchedQuery = "";
	ScrollAnchor searchOrigin;
	long wrapArmedAt, searchIntent;
	boolean resetting, disposed;

	GuideSearchController(GuideSearchBar bar, List<GuideRowCard> rows, GuideNavigator navigator, Runnable applyFilters,
		Consumer<String> feedback)
	{
		search = bar.search;
		searchMatches = bar.searchMatches;
		clearSearch = bar.clearSearch;
		this.rows = rows;
		this.navigator = navigator;
		this.applyFilters = applyFilters;
		this.feedback = feedback;
		searchTimer.setRepeats(false);
		search.addActionListener(e -> nextSearchMatch(System.nanoTime()));
		searchMatches.addActionListener(e -> nextSearchMatch(System.nanoTime()));
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
	}

	void visibilityChanged(boolean changed)
	{
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
			feedback.accept("");
		}
	}

	void dispose()
	{
		disposed = true;
		searchTimer.stop();
		clearMatches();
		searchOrigin = null;
	}

	private void applyFilters()
	{
		applyFilters.run();
	}

	void searchChanged()
	{
		searchTimer.stop();
		clearSearch.setEnabled(!search.getText().isBlank());
		if (resetting || disposed)
		{
			return;
		}
		String query = StepSearch.normalize(search.getText());
		if (!query.isEmpty() && searchOrigin == null)
		{
			searchOrigin = navigator.captureAnchor();
		}
		searchIntent = ++navigator.scrollIntent;
		clearMatches();
		feedback.accept("");
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

	void filtersChanged()
	{
		searchTimer.stop();
		wrapArmedAt = 0;
		if (StepSearch.normalize(search.getText()).isEmpty())
		{
			searchOrigin = null;
		}
		navigator.scrollIntent++;
		applyFilters();
		long intent = navigator.scrollIntent;
		SwingUtilities.invokeLater(() ->
		{
			if (disposed || intent != navigator.scrollIntent)
			{
				return;
			}
			navigator.body.validate();
			if (StepSearch.normalize(search.getText()).isEmpty())
			{
				navigator.jumpTo(0);
			}
			else
			{
				searchIntent = navigator.scrollIntent;
				searchNow();
			}
		});
	}

	void searchNow()
	{
		requireEdt();
		searchTimer.stop();
		if (disposed || searchIntent != navigator.scrollIntent || StepSearch.normalize(search.getText()).isEmpty())
		{
			return;
		}
		int best = refreshMatches(false);
		if (!matches.isEmpty())
		{
			showMatch(best);
		}
	}

	int refreshMatches(boolean preserveCurrent)
	{
		String key = preserveCurrent && matchIndex >= 0 && matchIndex < matches.size()
			? matches.get(matchIndex).row.getKey()
			: null;
		matches.clear();
		matchedQuery = StepSearch.normalize(search.getText());
		GuideRowCard best = null;
		int bestScore = Integer.MAX_VALUE;
		for (GuideRowCard card : rows)
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

	void showMatch(int index)
	{
		matchIndex = index;
		wrapArmedAt = 0;
		GuideRowCard card = matches.get(index);
		updateMatchStatus();
		navigator.jumpTo(navigator.position(card));
	}

	void nextSearchMatch(long now)
	{
		requireEdt();
		if (disposed || StepSearch.normalize(search.getText()).isEmpty())
		{
			return;
		}
		if (searchTimer.isRunning() || !matchedQuery.equals(StepSearch.normalize(search.getText())))
		{
			searchIntent = navigator.scrollIntent;
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
			if (wrapArmedAt != 0 && now - wrapArmedAt >= 0 && now - wrapArmedAt <= UiMetrics.WRAP_NANOS)
			{
				showMatch(0);
			}
			else
			{
				wrapArmedAt = now;
				feedback.accept(" · Press Enter twice to return to the first match");
			}
		}
	}

	void updateMatchStatus()
	{
		searchMatches.setVisible(!matchedQuery.isEmpty());
		searchMatches.setText((matchIndex + 1) + "/" + matches.size());
		searchMatches.setEnabled(!matches.isEmpty());
		searchMatches.getAccessibleContext()
			.setAccessibleName("Search match " + (matchIndex + 1) + " of " + matches.size());
		searchMatches.setToolTipText("Next match (Enter). At the last match, press Enter twice within 500 ms to wrap.");
		feedback.accept(matches.isEmpty() ? " · " + UiCopy.NO_MATCH
			: matchIndex >= 0 ? " · Search match: step " + matches.get(matchIndex).stepNumber : "");
	}

	void clearMatches()
	{
		matches.clear();
		matchedQuery = "";
		matchIndex = -1;
		wrapArmedAt = 0;
		searchMatches.setVisible(false);
	}

	void restoreSearchOrigin()
	{
		ScrollAnchor origin = searchOrigin;
		if (origin != null)
		{
			navigator.restoreAfterLayout(
				new ScrollAnchor(origin.key, origin.offset, origin.absolute, navigator.scrollIntent),
				() -> searchOrigin = null);
		}
	}

	void userNavigated()
	{
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

	void reset(Runnable clearSelections)
	{
		resetting = true;
		searchTimer.stop();
		clearSelections.run();
		search.setText("");
		clearMatches();
		resetting = false;
		navigator.scrollIntent++;
		applyFilters();
		if (searchOrigin != null)
		{
			restoreSearchOrigin();
		}
		else
		{
			navigator.jumpTo(0);
		}
	}
}
