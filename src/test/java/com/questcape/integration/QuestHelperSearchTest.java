package com.questcape.integration;

import com.questcape.guide.GuideRow;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.event.*;
import net.runelite.client.ui.components.IconTextField;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Exercises the verified public Swing structure. This is not a confirmed quest-launch contract. */
public class QuestHelperSearchTest
{
	private final RuneLitePluginRegistry registry = mock(RuneLitePluginRegistry.class);
	private JTabbedPane sidebar;
	private JPanel origin, helper, header, searchContainer, results;
	private IconTextField search;
	private QuestHelperSearch fallback;
	private final AtomicInteger changes = new AtomicInteger();

	private GuideRow row(String identity)
	{
		return new GuideRow("quest:" + identity, 0, GuideRow.Kind.QUEST, "Start this quest", "Wiki quest title",
			identity, Map.of(), Map.of(), Map.of());
	}

	@Before
	public void fixture() throws Exception
	{
		when(registry.questHelper()).thenReturn(RuneLitePluginRegistry.State.ACTIVE);
		SwingUtilities.invokeAndWait(() ->
		{
			sidebar = new JTabbedPane(JTabbedPane.RIGHT);
			origin = new JPanel();
			helper = new JPanel(new BorderLayout());
			header = new JPanel(new BorderLayout());
			searchContainer = new JPanel();
			search = new IconTextField();
			searchContainer.add(search);
			header.add(searchContainer, BorderLayout.SOUTH);
			helper.add(header, BorderLayout.NORTH);
			results = new JPanel();
			helper.add(results, BorderLayout.CENTER);
			sidebar.addTab("Guide", origin);
			sidebar.addTab("Quest Helper", helper);
			sidebar.setSelectedIndex(0);
			sidebar.addChangeListener(e -> changes.incrementAndGet());
			fallback = new QuestHelperSearch(registry, c -> c == helper ? "com.questhelper.panel.QuestHelperPanel"
				: c instanceof ResultRow ? "com.questhelper.panel.QuestSelectPanel" : c.getClass().getName());
		});
	}

	@Test
	public void selectsExistingSidebarTabAndFillsSearchThroughItsNativeDocumentListener() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			AtomicInteger searches = new AtomicInteger();
			search.getDocument().addDocumentListener(new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e)
				{
					searches.incrementAndGet();
				}

				public void removeUpdate(DocumentEvent e)
				{
					searches.incrementAndGet();
				}

				public void changedUpdate(DocumentEvent e)
				{
					searches.incrementAndGet();
				}
			});
			QuestHelperBridge.Result result = fallback.open(origin, row("COOKS_ASSISTANT"));
			assertEquals(QuestHelperBridge.State.SEARCH_READY, result.getState());
			assertSame(helper, sidebar.getSelectedComponent());
			assertEquals(1, changes.get());
			assertEquals("Cook's Assistant", search.getText());
			assertTrue(searches.get() > 0);
			assertTrue(result.getMessage().contains("Select the matching result"));
		});
	}

	@Test
	public void leavesAnAssistSettingsViewThroughItsExistingToggleBeforeSearching() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			searchContainer.setVisible(false);
			JButton toggle = new JButton();
			toggle.setToolTipText("Change your settings");
			toggle.addActionListener(e -> searchContainer.setVisible(true));
			header.add(toggle, BorderLayout.EAST);
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("IN_SEARCH_OF_KNOWLEDGE")).getState());
			assertTrue(searchContainer.isVisible());
			assertEquals("In Search of Knowledge", search.getText());
		});
	}

	@Test
	public void ambiguousChangedOrRemovedSearchNeverWritesIntoAnotherInput() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			IconTextField other = new IconTextField();
			header.add(other, BorderLayout.EAST);
			assertEquals(QuestHelperBridge.State.FAILED, fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			assertTrue(search.getText().isEmpty());
			assertTrue(other.getText().isEmpty());
			header.remove(other);
			searchContainer.setVisible(false);
			assertEquals(QuestHelperBridge.State.FAILED, fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			sidebar.remove(helper);
			assertEquals(QuestHelperBridge.State.FAILED, fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			assertTrue(search.getText().isEmpty());
		});
	}

	@Test
	public void unavailableDependencyAndDetachedPanelDoNotNavigate() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			when(registry.questHelper()).thenReturn(RuneLitePluginRegistry.State.DISABLED);
			assertEquals(QuestHelperBridge.State.UNAVAILABLE, fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			assertSame(origin, sidebar.getSelectedComponent());
			assertEquals(0, changes.get());
			when(registry.questHelper()).thenReturn(RuneLitePluginRegistry.State.ACTIVE);
			assertEquals(QuestHelperBridge.State.FAILED,
				fallback.open(new JPanel(), row("COOKS_ASSISTANT")).getState());
		});
	}

	@Test
	public void ordinarySubquestAndStartStageQueriesMatchQuestHelperDisplayNames()
	{
		assertEquals("Cook's Assistant", QuestHelperSearch.query(row("COOKS_ASSISTANT")));
		assertEquals("RFD - Start", QuestHelperSearch.query(row("RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST")));
		assertEquals("RFD - Dwarf", QuestHelperSearch.query(row("RECIPE_FOR_DISASTER__MOUNTAIN_DWARF")));
		assertEquals("RFD - Wartface & Bentnoze",
			QuestHelperSearch.query(row("RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE")));
		assertEquals("RFD - Monkey Ambassador", QuestHelperSearch.query(row("RECIPE_FOR_DISASTER__KING_AWOWOGEI")));
		assertEquals("RFD - Finale", QuestHelperSearch.query(row("RECIPE_FOR_DISASTER__CULINAROMANCER")));
		assertEquals("Wiki quest title", QuestHelperSearch.query(row(null)));
	}

	@Test
	public void reusesTheCurrentVerifiedSidebarIconWithoutSelectingOrRetainingTheTab() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			Icon first = new ImageIcon(
				new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB));
			sidebar.setToolTipTextAt(1, "Quest Helper");
			sidebar.setIconAt(1, first);
			assertSame(first, fallback.sidebarIcon(origin));
			assertSame(origin, sidebar.getSelectedComponent());
			Icon replacement = new ImageIcon(
				new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB));
			sidebar.setIconAt(1, replacement);
			assertSame(replacement, fallback.sidebarIcon(origin));
			sidebar.remove(helper);
			assertNull(fallback.sidebarIcon(origin));
			assertNull(fallback.sidebarIcon(new JPanel()));
		});
	}

	private static class ResultRow extends JPanel
	{
		final JButton arrow = new JButton();
		final AtomicInteger clicks = new AtomicInteger();

		ResultRow(String name)
		{
			super(new BorderLayout());
			add(new JLabel(name), BorderLayout.CENTER);
			arrow.setIcon(
				new ImageIcon(new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB)));
			arrow.addActionListener(e -> clicks.incrementAndGet());
			add(arrow, BorderLayout.LINE_END);
		}
	}

	@Test
	public void opensOnlyTheSingleFreshResultAndPreservesItsNativeSetupFlow() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			ResultRow stale = new ResultRow("Old search"), target = new ResultRow("Cook's Assistant");
			results.add(stale);
			JPanel nativeSetup = new JPanel();
			nativeSetup.setVisible(false);
			target.arrow.addActionListener(e ->
			{
				nativeSetup.setVisible(true);
				search.setText("");
			});
			search.getDocument().addDocumentListener(new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e)
				{
					results.removeAll();
					results.add(target);
				}

				public void removeUpdate(DocumentEvent e)
				{
					results.removeAll();
					results.add(stale);
				}

				public void changedUpdate(DocumentEvent e)
				{
				}
			});
			assertEquals(QuestHelperBridge.State.RESULT_SELECTED,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			assertEquals(1, target.clicks.get());
			assertEquals(0, stale.clicks.get());
			assertTrue(nativeSetup.isVisible());
			assertTrue("The native arrow may clear search", search.getText().isEmpty());
		});
	}

	@Test
	public void zeroAndMultipleMatchesNeverChooseAResultEvenIfOnlyOneArrowIsEnabled() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			ResultRow first = new ResultRow("First"), second = new ResultRow("Second");
			results.add(first);
			results.add(second);
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			second.arrow.setEnabled(false);
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			assertEquals(0, first.clicks.get());
			assertEquals(0, second.clicks.get());
		});
	}

	@Test
	public void hiddenViewsAreExcludedButDisabledMissingOrAmbiguousArrowsAreNotPressed() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			ResultRow target = new ResultRow("Target"), hidden = new ResultRow("Hidden quest");
			JPanel hiddenView = new JPanel();
			hiddenView.add(hidden);
			hiddenView.setVisible(false);
			results.add(hiddenView);
			results.add(target);
			target.arrow.setEnabled(false);
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			target.arrow.setEnabled(true);
			target.remove(target.arrow);
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			target.add(target.arrow, BorderLayout.LINE_END);
			JButton other = new JButton();
			target.add(other, BorderLayout.SOUTH);
			assertEquals(QuestHelperBridge.State.SEARCH_READY,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			target.remove(other);
			assertEquals(QuestHelperBridge.State.RESULT_SELECTED,
				fallback.open(origin, row("COOKS_ASSISTANT")).getState());
			assertEquals(1, target.clicks.get());
			assertEquals(0, hidden.clicks.get());
		});
	}
}
