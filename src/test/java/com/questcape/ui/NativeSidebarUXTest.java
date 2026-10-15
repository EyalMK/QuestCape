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
		SwingUtilities
			.invokeAndWait(() -> panel = new GuidePanel(actions, com.questcape.integration.TestTrainingLinks.local()));
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
		assertEquals(Integer.valueOf(54),
			projection.getEntries().get(0).getTraining().getComparisons().get(0).getLevel());
		assertEquals(Integer.valueOf(66),
			projection.getEntries().get(0).getTraining().getComparisons().get(0).getPercent());
		assertEquals(2, GuideProjection.of(guide, account(), null, null).getEntries().size());
	}

	@Test
	public void unavailableAndUnsupportedReasonsDoNotPromiseTheSameRecovery() throws Exception
	{
		GuideSnapshot guide = route();
		assertEquals(UiCopy.MISSING_ACCOUNT,
			GuideProjection.of(guide, null, null, null).getEntries().get(0).getExplanation());
		GuideRow unsupported = com.questcape.guide.GuideAssembler.row("future", 0, GuideRow.Kind.ACTIVITY,
			"Future activity", "", null, Map.of(),
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
	public void disclosureRejectsDragRightAndConsumedClicksAndStopsSelectionOnFold() throws Exception
	{
		GuideSnapshot guide = route();
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(guide, account(), SidebarStatus.idle("", ""), true);
			JTextComponent title = findText(panel, "Lunar Diplomacy");
			JButton expand = findDetails(title.getParent());
			title.dispatchEvent(
				new MouseEvent(title, MouseEvent.MOUSE_PRESSED, 0, 0, 2, 2, 1, false, MouseEvent.BUTTON1));
			title.dispatchEvent(
				new MouseEvent(title, MouseEvent.MOUSE_DRAGGED, 0, MouseEvent.BUTTON1_DOWN_MASK, 20, 2, 0, false));
			click(title, 1);
			assertEquals(UiCopy.DETAILS_CLOSED, expand.getText());
			title.dispatchEvent(
				new MouseEvent(title, MouseEvent.MOUSE_CLICKED, 0, 0, 2, 2, 1, false, MouseEvent.BUTTON3));
			MouseEvent consumed = new MouseEvent(title, MouseEvent.MOUSE_CLICKED, 0, 0, 2, 2, 1, false,
				MouseEvent.BUTTON1);
			consumed.consume();
			title.dispatchEvent(consumed);
			assertEquals(UiCopy.DETAILS_CLOSED, expand.getText());
			expand.doClick(0);
			JButton selection = findButton(title.getParent(), UiCopy.SELECT_TEXT);
			selection.doClick(0);
			expand.doClick(0);
			assertEquals(UiCopy.SELECT_TEXT, selection.getText());
			assertEquals(Cursor.HAND_CURSOR, title.getCursor().getType());
		});
	}

	@Test
	public void titleClicksAreImmediateAndSelectionIsExplicit() throws Exception
	{
		GuideSnapshot guide = route();
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(guide, account(), SidebarStatus.idle("", ""), true);
			JTextComponent title = findText(panel, "Lunar Diplomacy");
			JButton expand = findDetails(title.getParent());
			assertEquals(Cursor.HAND_CURSOR, title.getCursor().getType());
			click(title, 1);
			assertEquals(UiCopy.DETAILS_OPEN, expand.getText());
			JButton selection = findButton(title.getParent(), UiCopy.SELECT_TEXT);
			selection.doClick(0);
			assertEquals(Cursor.TEXT_CURSOR, title.getCursor().getType());
			click(title, 1);
			click(title, 2);
			title.select(0, 5);
			assertEquals(UiCopy.DETAILS_OPEN, expand.getText());
			assertEquals(5, title.getSelectionEnd());
			selection.doClick(0);
			assertEquals(Cursor.HAND_CURSOR, title.getCursor().getType());
			click(title, 1);
			assertEquals(UiCopy.DETAILS_CLOSED, expand.getText());
		});
	}

	@Test
	public void projectedRegionRelatedSubsectionAndResyncHaveIndependentHitAreas() throws Exception
	{
		GuideRow quest = com.questcape.guide.GuideAssembler.row("fixture", 0, GuideRow.Kind.QUEST, "Lunar Diplomacy",
			"Lunar Diplomacy", "LUNAR_DIPLOMACY", Map.of(),
			Map.of("Additional info", "Keep this complete advice", "New levels after quest", "65 Magic", "Quest points",
				"2", "Total QP", "279"),
			Map.of("https://oldschool.runescape.wiki/w/Lunar_Diplomacy", "Lunar Diplomacy"));
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(new GuideSnapshot("fixture", 1, 1, null, null, List.of(quest)), account(),
				SidebarStatus.idle("", ""), true);
			JTextComponent title = findText(panel, "Lunar Diplomacy");
			JButton expand = findDetails(title.getParent());
			expand.doClick(0);
			JLabel projection = findLabel(title.getParent(), "65");
			assertNotNull(projection);
			assertEquals(Cursor.DEFAULT_CURSOR, projection.getCursor().getType());
			click(projection, 1);
			click(projection.getParent(), 1);
			assertEquals(UiCopy.DETAILS_OPEN, expand.getText());
			JButton related = findButton(title.getParent(), "Related wiki pages");
			JButton link = findButton(title.getParent(), "<html>Quest page");
			assertFalse(link.getParent().isVisible());
			related.doClick(0);
			assertTrue(link.getParent().isVisible());
			assertEquals(UiCopy.DETAILS_OPEN, expand.getText());
			JTextComponent advice = findText(title.getParent(), UiCopy.ADVICE),
				qp = findText(title.getParent(), UiCopy.QUEST_POINTS),
				total = findText(title.getParent(), UiCopy.GUIDE_QP);
			assertEquals(advice.getFont(), qp.getFont());
			assertEquals(advice.getFont(), total.getFont());
			assertEquals(14, advice.getFont().getSize());
			JButton resync = findNamedButton(panel, UiCopy.RESYNC);
			assertNotNull(resync);
			click(resync.getParent(), 1);
			verify(actions, never()).syncPlayer();
			resync.doClick(0);
			verify(actions).syncPlayer();
		});
	}

	private static JButton findNamedButton(Container parent, String name)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JButton && name.equals(((JButton)child).getAccessibleContext().getAccessibleName()))
			{
				return (JButton)child;
			}
			if (child instanceof Container)
			{
				JButton result = findNamedButton((Container)child, name);
				if (result != null)
				{
					return result;
				}
			}
		}
		return null;
	}

	@Test
	public void relatedOnlyContentStartsOpenAndKeepsUserChoiceAcrossParentAndLiveUpdates() throws Exception
	{
		GuideRow row = GuideAssembler.row("only-links", 0, GuideRow.Kind.ACTIVITY, "Link-only fixture", "", null,
			Map.of(),
			Map.of("Quick Guide", "N/A"), Map.of("https://oldschool.runescape.wiki/w/Mining#Training", "Training"));
		GuideSnapshot route = new GuideSnapshot("fixture", 1, 1, null, null, List.of(row));
		assertTrue(GuideDetailProjector.of(row).relatedInitiallyOpen());
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(route, account(), SidebarStatus.idle("", ""), true);
			Container card = findText(panel, "Link-only fixture").getParent();
			JButton details = findDetails(card);
			details.doClick(0);
			JButton related = findButton(card, UiCopy.RELATED), link = findButton(card, "<html>Training");
			assertTrue(link.getParent().isVisible());
			related.doClick(0);
			assertFalse(link.getParent().isVisible());
			details.doClick(0);
			details.doClick(0);
			panel.render(route, account(), SidebarStatus.idle("", ""), true);
			assertFalse(link.getParent().isVisible());
			assertEquals(UiCopy.DETAILS_OPEN, details.getText());
			JButton select = findButton(card, UiCopy.SELECT_TEXT);
			select.doClick(0);
			((JComponent)card).getActionMap().get(UiCopy.EXIT_SELECTION_ACTION).actionPerformed(null);
			assertEquals(UiCopy.SELECT_TEXT, select.getText());
		});
	}

	private static JButton findButton(Container parent, String prefix)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JButton && ((JButton)child).getText().startsWith(prefix))
			{
				return (JButton)child;
			}
			if (child instanceof Container)
			{
				JButton result = findButton((Container)child, prefix);
				if (result != null)
				{
					return result;
				}
			}
		}
		return null;
	}

	private static JLabel findLabel(Container parent, String value)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JLabel && value.equals(((JLabel)child).getText()))
			{
				return (JLabel)child;
			}
			if (child instanceof Container)
			{
				JLabel result = findLabel((Container)child, value);
				if (result != null)
				{
					return result;
				}
			}
		}
		return null;
	}

	private static void click(Component target, int count)
	{
		target.dispatchEvent(
			new MouseEvent(target, MouseEvent.MOUSE_CLICKED, 1, 0, 3, 3, count, false, MouseEvent.BUTTON1));
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
