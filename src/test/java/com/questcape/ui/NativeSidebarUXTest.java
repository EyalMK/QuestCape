package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.persistence.StoredView.SaveState;
import com.questcape.progress.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Isolated Swing only: these events never target RuneScape or a live client. */
public class NativeSidebarUXTest
{
	private GuidePanel panel;
	private GuidePanel.Actions actions;

	@Before
	public void create() throws Exception
	{
		actions = mock(GuidePanel.Actions.class);
		SwingUtilities.invokeAndWait(() -> panel = new GuidePanel(actions, new TrainingGuideResolver(null)));
	}

	@After
	public void dispose() throws Exception
	{
		SwingUtilities.invokeAndWait(panel::dispose);
	}

	private GuideSnapshot route() throws Exception
	{
		List<GuideRow> rows = new GuideParser()
			.parse(GuideParserTest.table(GuideParserTest.activity("Train Woodcutting from level 52 to level 55")
				+ GuideParserTest.activity("<a href='/w/Lunar_Diplomacy'>Lunar Diplomacy</a>")));
		return new GuideSnapshot("fixture", 1, 1, null, null, rows);
	}

	private AccountProgress account()
	{
		return ProgressTest.observation("synthetic", "Fixture", "STANDARD",
			Map.of("LUNAR_DIPLOMACY", AccountProgress.QuestStatus.INCOMPLETE), Map.of("WOODCUTTING", 54));
	}

	@Test
	public void clearCoexistsWithBorderlessNavigatorAndRetainsFilters() throws Exception
	{
		GuideSnapshot guide = route();
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(guide, account(), SidebarStatus.idle("", ""), true);
			panel.typeOption(GuideRow.Kind.QUEST).doClick();
			panel.searchField().setText("no such fixture step");
			panel.searchNow();
			assertEquals(1, panel.visibleRowCount());
			assertEquals("No matching step", panel.searchMessage());
			assertEquals("1 of 2 steps shown", panel.filterMessage());
			assertEquals("0/0", panel.searchMatchesButton().getText());
			assertFalse(panel.searchMatchesButton().isEnabled());
			assertTrue(panel.searchMatchesButton().isVisible());
			assertTrue(panel.clearSearchButton().isEnabled());
			assertEquals("Clear search", panel.clearSearchButton().getToolTipText());
			panel.clearSearchButton().doClick();
			assertEquals("", panel.searchField().getText());
			assertTrue(panel.typeOption(GuideRow.Kind.QUEST).isSelected());
			assertEquals(1, panel.visibleRowCount());
			assertFalse(panel.searchPending());
			assertFalse(panel.clearSearchButton().isEnabled());
		});
	}

	@Test
	public void githubTooltipIsSmallFloatingAndUsesIndependentAction() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JButton github = panel.reportButton();
			assertEquals("Report issues or contribute", github.getToolTipText());
			JToolTip tip = github.createToolTip();
			tip.setTipText(github.getToolTipText());
			assertEquals(11, tip.getFont().getSize());
			assertTrue(tip.getPreferredSize().width < 220);
			assertTrue(github.getToolTipLocation(null).y > github.getHeight());
			github.doClick();
			verify(actions).report();
			verifyNoMoreInteractions(actions);
			assertEquals("https://github.com/EyalMK/QuestCape/issues", ApplicationLinks.ISSUES);
		});
	}

	@Test
	public void projectionSharesProgressTruthAcrossCardsFiltersAndTotals() throws Exception
	{
		GuideSnapshot guide = route();
		GuideProjection projection = GuideProjection.of(guide, account(), GuideRow.Kind.TRAINING,
			Completion.State.IN_PROGRESS);
		assertEquals(0, projection.getComplete());
		assertEquals(2, projection.getEntries().size());
		assertTrue(projection.getEntries().get(0).isVisible());
		assertFalse(projection.getEntries().get(1).isVisible());
		assertEquals("◐ In progress", projection.getEntries().get(0).getCompletion().label());
		assertEquals("Current level: 54 - 66%", projection.getEntries().get(0).getComparison());
		assertEquals(2, GuideProjection.of(guide, account(), null, null).getEntries().size());
	}

	@Test
	public void unavailableAndUnsupportedReasonsDoNotPromiseTheSameRecovery() throws Exception
	{
		GuideSnapshot guide = route();
		assertEquals(UiCopy.MISSING_ACCOUNT,
			GuideProjection.of(guide, null, null, null).getEntries().get(0).getExplanation());
		GuideRow unsupported = new GuideRow("future", 0, GuideRow.Kind.ACTIVITY, "Future activity", "", null, Map.of(),
			Map.of(), Map.of());
		assertEquals(UiCopy.UNSUPPORTED_ACTION,
			GuideProjection
				.of(new GuideSnapshot("future", 1, 1, null, null, List.of(unsupported)), account(), null, null)
				.getEntries().get(0).getExplanation());
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(guide, account(), new SidebarStatus(OperationOutcome.State.IDLE, OperationOutcome.State.IDLE,
				SaveState.SAVED, SaveState.UNSAVED, "", ""), true);
			JTextComponent warning = findText(panel, "Progress unsaved.");
			assertNotNull(warning);
			assertFalse(SwingUtilities.isDescendingFrom(warning, panel.routeScrollPane().getViewport().getView()));
			panel.render(guide, account(), SidebarStatus.idle("", ""), true);
			assertFalse(warning.isVisible());
		});
	}

	@Test
	public void singleTitleClickDisclosesAfterSelectionWindowButDoubleClickDoesNot() throws Exception
	{
		GuideSnapshot guide = route();
		JTextComponent[] title = new JTextComponent[1];
		JButton[] expand = new JButton[1];
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(guide, account(), SidebarStatus.idle("", ""), true);
			title[0] = findText(panel, "Lunar Diplomacy");
			expand[0] = findDetails(title[0].getParent());
			click(title[0], 1);
		});
		awaitText(expand[0], UiCopy.DETAILS_OPEN);
		SwingUtilities.invokeAndWait(() ->
		{
			click(title[0], 1);
			click(title[0], 2);
			title[0].select(0, 5);
		});
		// A timer spanning the native multi-click window observes any unwanted collapse.
		CompletableFuture<Void> elapsed = new CompletableFuture<>();
		SwingUtilities.invokeAndWait(() ->
		{
			javax.swing.Timer timer = new javax.swing.Timer(700, e -> elapsed.complete(null));
			timer.setRepeats(false);
			timer.start();
		});
		elapsed.get(3, TimeUnit.SECONDS);
		SwingUtilities.invokeAndWait(() -> assertEquals(UiCopy.DETAILS_OPEN, expand[0].getText()));
	}

	private static void click(Component target, int count)
	{
		target.dispatchEvent(
			new MouseEvent(target, MouseEvent.MOUSE_CLICKED, 1, 0, 3, 3, count, false, MouseEvent.BUTTON1));
	}

	private static void awaitText(JButton button, String expected) throws Exception
	{
		CompletableFuture<Void> found = new CompletableFuture<>();
		javax.swing.Timer[] observer = new javax.swing.Timer[1];
		SwingUtilities.invokeAndWait(() ->
		{
			observer[0] = new javax.swing.Timer(10, e ->
			{
				if (expected.equals(button.getText()))
				{
					found.complete(null);
				}
			});
			observer[0].start();
		});
		try
		{
			found.get(3, TimeUnit.SECONDS);
		}
		finally
		{
			SwingUtilities.invokeAndWait(observer[0]::stop);
		}
	}

	private static JButton findDetails(Container parent)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JButton && ((JButton)child).getText().startsWith("Details"))
			{
				return (JButton)child;
			}
		}
		throw new AssertionError("Missing disclosure");
	}

	private static JTextComponent findText(Container parent, String prefix)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JTextComponent && ((JTextComponent)child).getText().startsWith(prefix))
			{
				return (JTextComponent)child;
			}
			if (child instanceof Container)
			{
				JTextComponent found = findText((Container)child, prefix);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}
}
