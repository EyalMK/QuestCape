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
			row("Unlock: balloon", GuideRow.Kind.UNLOCK, null, ""));
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
			panel.typeOption(GuideRow.Kind.DIARY).doClick();
			panel.stateOption(Completion.State.INCOMPLETE).doClick();
			assertEquals(2, panel.visibleRowCount());
			assertFalse(findText(panel, "Guide note").getParent().isVisible());
			assertTrue(panel.progressButton().getToolTipText().startsWith("Step 2: Cooking Contest"));
			panel.stateOption(Completion.State.COMPLETE).doClick();
			assertEquals(3, panel.visibleRowCount());
			assertTrue(texts(panel).contains("1 of 5 complete"));
			assertTrue(texts(panel).contains("002 / Quest"));
			panel.render(guide, null, "Up to date", "", false);
			assertEquals(0, panel.visibleRowCount());
			assertFalse(panel.progressButton().isEnabled());
			panel.resetButton().doClick();
			assertEquals(5, panel.visibleRowCount());
			assertTrue(findText(panel, "Guide note").getParent().isVisible());
			panel.stateOption(Completion.State.UNKNOWN).doClick();
			assertEquals(5, panel.visibleRowCount());
			panel.render(guide, account, "Up to date", "", true);
			assertEquals(1, panel.visibleRowCount());
			panel.render(guide, account.withManual(Set.of("Unlock: balloon")), "Up to date", "", true);
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
			if (panel.filterMessage().contains("Step 2"))
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
				assertFalse(panel.filterMessage().contains("Step 2"));
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
				assertTrue(panel.filterMessage().contains("No matching step"));
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
	public void projectedLevelsHaveNumberBeforeLocalIconAndAccessibleSkillNames() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.setSkillIcons(Map.of(Skill.COOKING, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
				Skill.HITPOINTS, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)));
			List<JLabel> labels = new ArrayList<>();
			labels(panel, labels);
			JLabel cooking = labels.stream().filter(label -> "Guide projected Cooking level 4".equals(
				label.getAccessibleContext().getAccessibleName())).findFirst().orElseThrow();
			assertEquals("4", cooking.getText());
			assertNotNull(cooking.getIcon());
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
