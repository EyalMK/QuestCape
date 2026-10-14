package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.TrainingGuideResolver;
import com.questcape.progress.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import net.runelite.api.Skill;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GuideFiltersTest
{
	private GuidePanel panel;
	private GuideSnapshot guide;
	private AccountProgress account;

	@Before
	public void setup() throws Exception
	{
		List<GuideRow> rows = List.of(
			row("Cook's Assistant", GuideRow.Kind.QUEST, "COOKS_ASSISTANT", "4 Cooking 10 Hitpoints"),
			row("Guide note", GuideRow.Kind.INFORMATION, null, ""),
			row("Cooking Contest", GuideRow.Kind.QUEST, "COOKING_CONTEST", ""),
			row("Contact!", GuideRow.Kind.QUEST, "CONTACT", ""),
			row("Easy Ardougne Diary", GuideRow.Kind.DIARY, null, ""),
			row("Unlock: Varrock Museum Kudos", GuideRow.Kind.UNLOCK, null, ""));
		guide = new GuideSnapshot("test", 1000, 1000, null, null, rows);
		account = new AccountProgress("live:test:STANDARD", "Synthetic", "STANDARD", "Live RuneLite", 1000, 1000L,
			Map.of("COOKS_ASSISTANT", AccountProgress.QuestStatus.COMPLETE,
				"COOKING_CONTEST", AccountProgress.QuestStatus.INCOMPLETE,
				"CONTACT", AccountProgress.QuestStatus.IN_PROGRESS), Map.of(),
			Map.of("DIARY:ARDOUGNE_EASY:complete", false), Set.of());
		SwingUtilities.invokeAndWait(() ->
		{
			panel = new GuidePanel(mock(GuidePanel.Actions.class), new TrainingGuideResolver(null));
			panel.setSize(242, 650);
			panel.render(guide, account, "Up to date", "", true);
			layout();
		});
		SwingUtilities.invokeAndWait(() -> {});
	}

	@After
	public void stop() throws Exception
	{
		SwingUtilities.invokeAndWait(panel::dispose);
	}

	private static GuideRow row(String title, GuideRow.Kind kind, String quest, String projection)
	{
		return new GuideRow(title, 0, kind, title, "", quest, Map.of(),
			Map.of("New levels after quest", projection), Map.of());
	}

	@Test
	public void typesAndStatesComposeWithoutRenumberingOrChangingTotals() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			assertNotNull(panel.typeOption(GuideRow.Kind.QUEST).getIcon());
			assertEquals("Quest", panel.typeOption(GuideRow.Kind.QUEST).getText());
			assertTrue(findText(panel, "Guide note").getParent().isVisible());
			panel.typeOption(GuideRow.Kind.QUEST).doClick();
			panel.stateOption(Completion.State.INCOMPLETE).doClick();
			assertEquals(1, panel.visibleRowCount());
			assertTrue(panel.progressButton().getToolTipText().startsWith("Step 2: Cooking Contest"));
			panel.typeOption(GuideRow.Kind.DIARY).doClick();
			assertFalse(panel.typeOption(GuideRow.Kind.QUEST).isSelected());
			assertEquals(1, panel.visibleRowCount());
			assertFalse(findText(panel, "Guide note").getParent().isVisible());
			panel.stateOption(Completion.State.COMPLETE).doClick();
			assertFalse(panel.stateOption(Completion.State.INCOMPLETE).isSelected());
			assertEquals(0, panel.visibleRowCount());
			panel.typeOption(GuideRow.Kind.QUEST).doClick();
			assertEquals(1, panel.visibleRowCount());
			panel.allStateOption().doClick();
			assertEquals(3, panel.visibleRowCount());
			assertTrue(texts(panel).contains("1 of 5 complete"));
			assertTrue(texts(panel).contains("002 / Quest"));
			panel.render(guide, null, "Up to date", "", false);
			assertEquals(3, panel.visibleRowCount());
			panel.stateOption(Completion.State.COMPLETE).doClick();
			assertEquals(0, panel.visibleRowCount());
			assertFalse(panel.progressButton().isEnabled());
			panel.resetButton().doClick();
			assertEquals(5, panel.visibleRowCount());
			assertTrue(findText(panel, "Guide note").getParent().isVisible());
			panel.stateOption(Completion.State.UNKNOWN).doClick();
			assertEquals(5, panel.visibleRowCount());
			panel.render(guide, account, "Up to date", "", true);
			assertEquals(1, panel.visibleRowCount());
			panel.render(guide, account.withManual(Set.of("Unlock: Varrock Museum Kudos")), "Up to date", "", true);
			assertEquals(0, panel.visibleRowCount());
			panel.resetButton().doClick();
			panel.stateOption(Completion.State.IN_PROGRESS).doClick();
			assertEquals(1, panel.visibleRowCount());
			assertTrue(panel.progressButton().getToolTipText().startsWith("Step 3: Contact!"));
		});
	}

	@Test
	public void searchDebouncesRapidTypingAndScrollsOnlyToVisibleMatches() throws Exception
	{
		CompletableFuture<Void> found = new CompletableFuture<>();
		javax.swing.Timer observer = new javax.swing.Timer(10, e ->
		{
			if (panel.searchMessage().contains("Search match: step 2"))
			{
				found.complete(null);
			}
		});
		try
		{
			SwingUtilities.invokeAndWait(() ->
			{
				panel.searchField().setText("Contact");
				panel.searchField().setText("Cooking Contest");
				assertTrue(panel.searchPending());
				assertFalse(panel.searchMessage().contains("Search match: step 2"));
				observer.start();
			});
			found.get(3, TimeUnit.SECONDS);
			SwingUtilities.invokeAndWait(() ->
			{
				assertTrue(panel.routeScrollPane().getVerticalScrollBar().getValue() > 0);
				panel.typeOption(GuideRow.Kind.DIARY).doClick();
			});
			SwingUtilities.invokeAndWait(() ->
			{
				assertTrue(panel.searchMessage().contains("No matching step"));
				panel.resetButton().doClick();
				assertEquals("", panel.searchField().getText());
				assertEquals(5, panel.visibleRowCount());
				assertFalse(panel.searchPending());
			});
		}
		finally
		{
			SwingUtilities.invokeAndWait(observer::stop);
		}
	}

	@Test
	public void clearingResettingDisposalAndUserScrollCancelPendingSearch() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.searchField().setText("Contact");
			panel.searchField().setText("");
			assertFalse(panel.searchPending());
			panel.searchField().setText("Cooking");
			panel.resetButton().doClick();
			assertFalse(panel.searchPending());
			panel.searchField().setText("Contact");
			panel.boundaryButton().doClick();
			int position = panel.routeScrollPane().getVerticalScrollBar().getValue();
			panel.searchNow();
			assertEquals(position, panel.routeScrollPane().getVerticalScrollBar().getValue());
			panel.searchField().setText("Cooking");
			panel.dispose();
			assertFalse(panel.searchPending());
			panel.searchNow();
			assertEquals(position, panel.routeScrollPane().getVerticalScrollBar().getValue());
		});
	}

	@Test
	public void clearingSearchRestoresOriginalOffsetAcrossEditsAndPendingQueries() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.routeScrollPane().getVerticalScrollBar().setValue(135);
			assertEquals(135, panel.routeScrollPane().getVerticalScrollBar().getValue());
			panel.searchField().setText("cook");
			panel.searchNow();
			assertEquals("1/2", panel.searchMatchesButton().getText());
			try
			{
				panel.searchField().getDocument().insertString(4, "ing", null);
			}
			catch (javax.swing.text.BadLocationException e)
			{
				throw new AssertionError(e);
			}
			panel.searchNow();
			panel.searchField().setText("Contact");
			panel.searchNow();
			panel.searchField().setText("");
			layout();
		});
		SwingUtilities.invokeAndWait(() ->
		{
			assertEquals("Clear must return to the pre-search card offset", 135,
				panel.routeScrollPane().getVerticalScrollBar().getValue());
			assertFalse(panel.searchMatchesButton().isVisible());
			panel.searchField().setText("Contact");
			assertTrue(panel.searchPending());
			panel.searchField().setText("   ");
		});
		SwingUtilities.invokeAndWait(() ->
		{
			assertFalse(panel.searchPending());
			assertEquals(135, panel.routeScrollPane().getVerticalScrollBar().getValue());
		});
	}

	@Test
	public void enterCyclesRankedMatchesAndWrapRequiresTwoTimelyPressesAtLastMatch() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.searchField().setText("cook");
			panel.searchField().postActionEvent();
			assertEquals("1/2", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 1"));
			panel.searchField().postActionEvent();
			assertEquals("2/2", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 2"));
			panel.nextSearchMatch(1_000_000_000L);
			assertEquals("2/2", panel.searchMatchesButton().getText());
			panel.nextSearchMatch(1_600_000_000L);
			assertEquals("Expired first press must not wrap", "2/2", panel.searchMatchesButton().getText());
			panel.render(guide, account, "Up to date", "", true);
			panel.nextSearchMatch(1_900_000_000L);
			assertEquals("Unchanged live observations must not break double Enter", "1/2", panel.searchMatchesButton().getText());
			panel.searchMatchesButton().doClick();
			assertEquals("2/2", panel.searchMatchesButton().getText());
			panel.nextSearchMatch(3_000_000_000L);
			panel.boundaryButton().doClick();
			panel.nextSearchMatch(3_200_000_000L);
			assertEquals("Manual navigation clears wrap arm", "2/2", panel.searchMatchesButton().getText());
			panel.nextSearchMatch(3_400_000_000L);
			assertEquals("1/2", panel.searchMatchesButton().getText());
			panel.searchField().setText("xyzzy");
			panel.searchField().postActionEvent();
			assertEquals("0/0", panel.searchMatchesButton().getText());
			assertFalse(panel.searchMatchesButton().isEnabled());
		});
	}

	@Test
	public void searchTraversalUsesStepNumbersEvenWhenStartRowsHaveLowerRelevance() throws Exception
	{
		List<GuideRow> route = new ArrayList<>();
		for (int step = 1; step <= 295; step++)
		{
			String title = step == 124 ? "In Search of the Myreque"
				: step == 257 ? "Start miniquest: In Search of Knowledge"
				: step == 295 ? "In Search of Knowledge (miniquest)" : "Unrelated step " + step;
			route.add(row(title, GuideRow.Kind.QUEST, null, ""));
		}
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(new GuideSnapshot("ordered", 1, 1, null, null, route), account, "", "", true);
			layout();
			panel.searchField().setText("in search");
			panel.searchNow();
			assertEquals("1/3", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 124"));
			panel.searchField().postActionEvent();
			assertEquals("2/3", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 257"));
			panel.searchField().postActionEvent();
			assertEquals("3/3", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 295"));
		});
	}

	@Test
	public void initialSearchStillChoosesBestMatchWithinRouteOrderedResults() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			List<GuideRow> route = List.of(row("Earlier needle reference", GuideRow.Kind.QUEST, null, ""),
				row("needle", GuideRow.Kind.QUEST, null, ""), row("needle cave", GuideRow.Kind.QUEST, null, ""));
			panel.render(new GuideSnapshot("best", 1, 1, null, null, route), account, "", "", true);
			layout();
			panel.searchField().setText("needle");
			panel.searchNow();
			assertEquals("2/3", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 2"));
			panel.searchField().postActionEvent();
			assertEquals("3/3", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 3"));
		});
	}

	@Test
	public void liveFiltersAndRouteReplacementReconcileMatchesWithoutStaleJumps() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.typeOption(GuideRow.Kind.QUEST).doClick();
			panel.stateOption(Completion.State.INCOMPLETE).doClick();
			layout();
			panel.searchField().setText("cook");
			panel.searchNow();
			assertEquals("1/1", panel.searchMatchesButton().getText());
			AccountProgress complete = new AccountProgress(account.getScope(), "Synthetic", "STANDARD", "Live RuneLite", 2, 2L,
				Map.of("COOKS_ASSISTANT", AccountProgress.QuestStatus.COMPLETE,
					"COOKING_CONTEST", AccountProgress.QuestStatus.COMPLETE,
					"CONTACT", AccountProgress.QuestStatus.IN_PROGRESS), Map.of(), account.getActivities(), Set.of());
			int position = panel.routeScrollPane().getVerticalScrollBar().getValue();
			panel.render(guide, complete, "", "", true);
			assertEquals("0/0", panel.searchMatchesButton().getText());
			assertEquals(position, panel.routeScrollPane().getVerticalScrollBar().getValue());
			panel.resetButton().doClick();
			panel.searchField().setText("cook");
			panel.searchNow();
			assertEquals("1/2", panel.searchMatchesButton().getText());
			GuideSnapshot replacement = new GuideSnapshot("new", 2, 2, null, null,
				List.of(row("Cooking replacement", GuideRow.Kind.QUEST, "REPLACEMENT", "")));
			panel.render(replacement, complete, "", "", true);
			layout();
			panel.searchField().postActionEvent();
			assertEquals("1/1", panel.searchMatchesButton().getText());
			assertTrue(panel.searchMessage().contains("Search match: step 1"));
			panel.dispose();
			panel.searchField().postActionEvent();
			assertFalse(panel.searchPending());
			assertFalse(panel.searchMatchesButton().isVisible());
		});
	}

	@Test
	public void iconFiltersSpanWidthAndStateOptionsMatchCardColors() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JButton type = panel.typeFilterButton(), state = panel.stateFilterButton(), reset = panel.resetButton();
			assertEquals("", type.getText());
			assertEquals("", state.getText());
			assertNotNull(type.getIcon());
			assertNotNull(state.getIcon());
			assertEquals(type.getWidth(), state.getWidth());
			assertTrue(Math.abs(state.getWidth() - reset.getWidth()) <= 1);
			assertTrue(type.getX() < state.getX());
			assertTrue(type.getParent().getWidth() - reset.getX() - reset.getWidth() <= 1);
			assertTrue(panel.typeOption(GuideRow.Kind.QUEST).getIconTextGap() >= 10);
			for (Completion.State status : List.of(Completion.State.COMPLETE, Completion.State.INCOMPLETE,
				Completion.State.IN_PROGRESS, Completion.State.UNKNOWN))
			{
				assertNotNull(panel.stateOption(status).getIcon());
				assertEquals(GuidePanel.stateColor(status), panel.stateOption(status).getForeground());
				assertTrue(panel.stateOption(status).getIconTextGap() <= 6);
			}
			panel.typeOption(GuideRow.Kind.QUEST).doClick();
			panel.typeOption(GuideRow.Kind.QUEST).doClick();
			assertTrue(panel.typeOption(GuideRow.Kind.QUEST).isSelected());
			panel.allTypeOption().doClick();
			assertEquals(5, panel.visibleRowCount());
			panel.searchField().setText("cook");
			panel.searchNow();
			layout();
			JButton counter = panel.searchMatchesButton();
			assertTrue(SwingUtilities.convertPoint(counter.getParent(), counter.getLocation(), panel.searchField().getParent()).x
				>= panel.searchField().getX() + panel.searchField().getWidth());
			assertNotNull(counter.getIcon());
		});
	}

	@Test
	public void projectedLevelsHaveNumberBeforeLocalIconAndAccessibleSkillNames() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.setSkillIcons(Map.of(Skill.COOKING, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)),
				Map.of(Skill.COOKING, new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB),
					Skill.HITPOINTS, new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB)));
			List<JLabel> labels = new ArrayList<>();
			labels(panel, labels);
			JLabel cooking = labels.stream().filter(label -> "Guide projected Cooking level 4".equals(
				label.getAccessibleContext().getAccessibleName())).findFirst().orElseThrow();
			assertEquals("4", cooking.getText());
			assertNotNull(cooking.getIcon());
			assertEquals(18, cooking.getIcon().getIconWidth());
			assertEquals(SwingConstants.LEFT, cooking.getHorizontalTextPosition());
			assertEquals(2, ProjectedLevels.parse("4 Cooking 10 Hitpoints").size());
			assertTrue(ProjectedLevels.parse("4 Cooking and an unknown annotation").isEmpty());
			assertTrue(ProjectedLevels.parse("999 Cooking").isEmpty());
		});
	}

	@Test
	public void rankingHandlesExactPrefixTokensTyposAndUnrelatedQueries()
	{
		assertEquals(0, StepSearch.score("Cook's Assistant", "cook s assistant"));
		assertTrue(StepSearch.score("Cooking Contest", "cooking contest") < StepSearch.score("Cooking", "cooking contest"));
		assertEquals(10, StepSearch.score("Cooking Contest", "cooking"));
		assertEquals(30, StepSearch.score("Recipe for Disaster", "disaster recipe"));
		assertTrue(StepSearch.score("Cooking Contest", "cookng") < Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, StepSearch.score("Cooking Contest", "xyzzy"));
		assertEquals(Integer.MAX_VALUE, StepSearch.score("Cooking Contest", ""));
		assertEquals(Integer.MAX_VALUE, StepSearch.score("Cooking Contest", "x".repeat(161)));
	}

	private void layout()
	{
		for (int i = 0; i < 3; i++)
		{
			layoutTree(panel);
			JViewport viewport = panel.routeScrollPane().getViewport();
			viewport.getView().setSize(viewport.getWidth(), viewport.getView().getPreferredSize().height);
			layoutTree((Container)viewport.getView());
		}
	}

	private static void layoutTree(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents())
		{
			if (child instanceof Container)
			{
				layoutTree((Container)child);
			}
		}
	}

	private static String texts(Container parent)
	{
		StringBuilder text = new StringBuilder();
		for (Component child : parent.getComponents())
		{
			if (child instanceof JTextArea)
			{
				text.append(((JTextArea)child).getText()).append('\n');
			}
			if (child instanceof Container)
			{
				text.append(texts((Container)child));
			}
		}
		return text.toString();
	}

	private static Component findText(Container parent, String value)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JTextArea && ((JTextArea)child).getText().equals(value))
			{
				return child;
			}
			if (child instanceof Container)
			{
				Component found = findText((Container)child, value);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	private static void labels(Container parent, List<JLabel> labels)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JLabel)
			{
				labels.add((JLabel)child);
			}
			if (child instanceof Container)
			{
				labels((Container)child, labels);
			}
		}
	}
}
