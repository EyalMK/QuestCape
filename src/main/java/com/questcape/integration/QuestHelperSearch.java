package com.questcape.integration;

import com.questcape.guide.GuideRow;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import javax.inject.*;
import javax.swing.*;
import net.runelite.api.Quest;
import net.runelite.client.ui.components.IconTextField;

/** User-requested UI fallback, using only the existing sidebar and public Swing components. */
@Singleton
public class QuestHelperSearch
{
	private static final String PANEL = "com.questhelper.panel.QuestHelperPanel";
	private static final String RESULT_PANEL = "com.questhelper.panel.QuestSelectPanel";
	private final RuneLitePluginRegistry registry;
	private final Function<Component, String> identity;

	@Inject
	public QuestHelperSearch(RuneLitePluginRegistry registry)
	{
		this(registry, c -> c.getClass().getName());
	}

	QuestHelperSearch(RuneLitePluginRegistry registry, Function<Component, String> identity)
	{
		this.registry = registry;
		this.identity = identity;
	}

	/** Reuse the installed plugin's own tab icon without bundling its assets or retaining its panel. */
	public Icon sidebarIcon(Component origin)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new IllegalStateException("Sidebar icons must be read on EDT");
		}
		JTabbedPane sidebar = (JTabbedPane)SwingUtilities.getAncestorOfClass(JTabbedPane.class, origin);
		if (sidebar == null)
		{
			return null;
		}
		Icon icon = null;
		int matches = 0;
		for (int i = 0; i < sidebar.getTabCount(); i++)
		{
			if (!"Quest Helper".equals(sidebar.getToolTipTextAt(i)))
			{
				continue;
			}
			if (descendants(sidebar.getComponentAt(i), c -> PANEL.equals(identity.apply(c))).size() != 1)
			{
				continue;
			}
			matches++;
			icon = sidebar.getIconAt(i);
		}
		return matches == 1 ? icon : null;
	}

	public QuestHelperBridge.Result open(Component origin, GuideRow row)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new IllegalStateException("Quest Helper search must run on EDT");
		}
		if (registry.questHelper() != RuneLitePluginRegistry.State.ACTIVE)
		{
			return result(QuestHelperBridge.State.UNAVAILABLE,
				RuneLitePluginRegistry.guidance("Quest Helper", registry.questHelper()));
		}
		JTabbedPane sidebar = (JTabbedPane)SwingUtilities.getAncestorOfClass(JTabbedPane.class, origin);
		if (sidebar == null)
		{
			return failed("Quest Helper's sidebar tab is not ready. Reopen this panel and try again.");
		}
		List<Component> panels = descendants(sidebar, c -> PANEL.equals(identity.apply(c)));
		if (panels.size() != 1 || !(panels.get(0) instanceof Container))
		{
			return failed("Quest Helper's panel could not be identified. Open it from RuneLite's sidebar and retry.");
		}
		Container panel = (Container)panels.get(0);
		int tab = -1;
		for (int i = 0; i < sidebar.getTabCount(); i++)
		{
			if (panel == sidebar.getComponentAt(i) || SwingUtilities.isDescendingFrom(panel, sidebar.getComponentAt(i)))
			{
				tab = i;
			}
		}
		if (tab < 0)
		{
			return failed("Quest Helper's sidebar tab is no longer available.");
		}
		// RuneLite's own change listener activates/deactivates panels and records sidebar history.
		sidebar.setSelectedIndex(tab);
		if (!(panel.getLayout() instanceof BorderLayout))
		{
			return changedLayout();
		}
		Component header = ((BorderLayout)panel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
		List<Component> fields = descendants(header, c -> c instanceof IconTextField);
		if (fields.size() != 1)
		{
			return changedLayout();
		}
		IconTextField search = (IconTextField)fields.get(0);
		if (!visibleWithin(search, panel))
		{
			// The verified panel hides search in its assist/settings view; use its own view toggle.
			List<Component> toggles = descendants(header,
				c -> c instanceof JButton && "Change your settings".equals(((JButton)c).getToolTipText()));
			if (toggles.size() == 1 && toggles.get(0).isEnabled())
			{
				((JButton)toggles.get(0)).doClick();
			}
		}
		if (!visibleWithin(search, panel) || !search.isEnabled())
		{
			return changedLayout();
		}
		String query = query(row);
		if (query.isBlank())
		{
			return failed("Quest Helper opened. Enter the quest name in its search field.");
		}
		search.setText(query);
		search.requestFocusInWindow();
		if (sidebar.getSelectedIndex() != tab || !query.equals(search.getText()))
		{
			return changedLayout();
		}
		Component resultList = ((BorderLayout)panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
		// Quest Helper's document listener filters the attached rows synchronously in setText.
		// Count every visible matching row, including disabled rows, before choosing any action.
		List<Component> matches = descendants(resultList,
			c -> RESULT_PANEL.equals(identity.apply(c)) && visibleWithin(c, panel));
		if (matches.size() == 1 && matches.get(0) instanceof Container)
		{
			Container match = (Container)matches.get(0);
			if (match.getLayout() instanceof BorderLayout)
			{
				Component arrow = ((BorderLayout)match.getLayout()).getLayoutComponent(BorderLayout.LINE_END);
				if (arrow instanceof JButton && descendants(match, c -> c instanceof JButton).size() == 1)
				{
					JButton button = (JButton)arrow;
					if (button.isEnabled() && button.getIcon() != null && button.getActionListeners().length > 0
						&& visibleWithin(button, panel)
						&& registry.questHelper() == RuneLitePluginRegistry.State.ACTIVE)
					{
						// Use the result's own listener: it preserves Quest Helper's assist/branch flow.
						button.doClick(0);
						return result(QuestHelperBridge.State.RESULT_SELECTED,
							"Selected “" + query + "” in Quest Helper. Complete any setup shown there.");
					}
				}
			}
		}
		return result(QuestHelperBridge.State.SEARCH_READY,
			"Quest Helper search: “" + query + "”. Select the matching result to start.");
	}

	static String query(GuideRow row)
	{
		String id = row.getQuestIdentity();
		if (id != null)
		{
			// Quest Helper's displayed RFD names differ from RuneLite's canonical subquest names.
			switch (id)
			{
			case "RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST":
				return "RFD - Start";
			case "RECIPE_FOR_DISASTER__MOUNTAIN_DWARF":
				return "RFD - Dwarf";
			case "RECIPE_FOR_DISASTER__KING_AWOWOGEI":
				return "RFD - Monkey Ambassador";
			case "RECIPE_FOR_DISASTER__CULINAROMANCER":
				return "RFD - Finale";
			default:
				try
				{
					return Quest.valueOf(id).getName().replace("Recipe for Disaster - ", "RFD - ");
				}
				catch (IllegalArgumentException ignored)
				{
				}
			}
		}
		return row.getWikiTarget() == null || row.getWikiTarget().isBlank() ? row.getTitle()
			: row.getWikiTarget().replace('_', ' ');
	}

	private static boolean visibleWithin(Component child, Container parent)
	{
		for (Component c = child; c != parent; c = c.getParent())
		{
			if (c == null || !c.isVisible())
			{
				return false;
			}
		}
		return true;
	}

	private static List<Component> descendants(Component root, java.util.function.Predicate<Component> match)
	{
		List<Component> found = new ArrayList<>();
		if (root == null)
		{
			return found;
		}
		if (match.test(root))
		{
			found.add(root);
		}
		if (root instanceof Container)
		{
			for (Component child : ((Container)root).getComponents())
			{
				found.addAll(descendants(child, match));
			}
		}
		return found;
	}

	private static QuestHelperBridge.Result changedLayout()
	{
		return failed("Quest Helper opened, but its search field is unavailable. Open its quest list and try again.");
	}

	private static QuestHelperBridge.Result failed(String message)
	{
		return result(QuestHelperBridge.State.FAILED, message);
	}

	private static QuestHelperBridge.Result result(QuestHelperBridge.State state, String message)
	{
		return new QuestHelperBridge.Result(state, message);
	}
}
