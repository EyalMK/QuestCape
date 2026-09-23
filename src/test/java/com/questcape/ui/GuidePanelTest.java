package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.DefaultCaret;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GuidePanelTest
{
	private static GuideSnapshot snapshot;
	private static AccountProgress account;
	private GuidePanel panel;
	private GuidePanel.Actions actions;

	@BeforeClass
	public static void fixtures() throws Exception
	{
		List<GuideRow> all = new GuideParser().parse(com.questcape.guide.GuideParserTest.fixture());
		int start = 0;
		for (int i = 0; i < all.size(); i++)
		{
			if (all.get(i).getTitle().contains("from level 34 to level 40"))
			{
				start = i;
				break;
			}
		}
		snapshot = new GuideSnapshot("15336101", 1789210000000L, 1789210000000L, null, null,
			all.subList(start, Math.min(start + 18, all.size())));
		account = new AccountProgress("live:test:STANDARD", "maple scout", "STANDARD", "Live RuneLite",
			1789210000000L, 1789210000000L,
			Map.of("COOKS_ASSISTANT", AccountProgress.QuestStatus.COMPLETE, "CONTACT",
				AccountProgress.QuestStatus.IN_PROGRESS,
				"SHADES_OF_MORTTON", AccountProgress.QuestStatus.COMPLETE),
			Map.of("FIREMAKING", 49, "HERBLORE", 25, "CRAFTING", 40, "ATTACK", 60, "STRENGTH", 60,
				"DEFENCE", 60, "HITPOINTS", 60, "MAGIC", 60, "RANGED", 60, "PRAYER", 60), Map.of(), Set.of());
	}

	@Before
	public void create() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			actions = mock(GuidePanel.Actions.class);
			panel = new GuidePanel(actions, new TrainingGuideResolver(null));
			panel.setSize(242, 820);
			panel.render(snapshot, account, "Up to date", "Character synced locally", true);
			layout();
		});
		SwingUtilities.invokeAndWait(this::layout);
	}

	private void layout()
	{
		for (int i = 0; i < 3; i++)
		{
			invalidateTree(panel);
			layoutTree(panel);
			JViewport viewport = panel.routeScrollPane().getViewport();
			Component body = viewport.getView();
			body.setSize(viewport.getWidth(), body.getPreferredSize().height);
			layoutTree((Container)body);
		}
	}

	private static void invalidateTree(Container c)
	{
		for (Component child : c.getComponents())
		{
			if (child instanceof Container)
			{
				invalidateTree((Container)child);
			}
		}
		c.invalidate();
	}

	private static void layoutTree(Container c)
	{
		c.doLayout();
		for (Component child : c.getComponents())
		{
			if (child instanceof Container)
			{
				layoutTree((Container)child);
			}
		}
	}

	@Test
	public void loggedOutClickThenRepeatedLiveRendersNeverMoveTheViewport() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(snapshot, null, "Up to date", "", false);
			panel.message("Log in before syncing character progress.");
			layout();
			panel.routeScrollPane().getVerticalScrollBar().setValue(650);
		});
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(snapshot, account, "Up to date", "Character synced locally", true);
			layout();
			panel.routeScrollPane().getVerticalScrollBar().setValue(650);
		});
		for (int i = 0; i < 40; i++)
		{
			SwingUtilities.invokeAndWait(() ->
			{
				panel.render(snapshot, account, "Up to date", "Character synced locally", true);
				layout();
				assertEquals("Live render must preserve user scroll", 650,
					panel.routeScrollPane().getVerticalScrollBar().getValue());
			});
		}
		SwingUtilities.invokeAndWait(() -> assertNeverScrollCaret(panel));
	}

	private static void assertNeverScrollCaret(Container c)
	{
		for (Component child : c.getComponents())
		{
			if (child instanceof JTextArea)
			{
				assertEquals(DefaultCaret.NEVER_UPDATE,
					((DefaultCaret)((JTextArea)child).getCaret()).getUpdatePolicy());
			}
			if (child instanceof Container)
			{
				assertNeverScrollCaret((Container)child);
			}
		}
	}

	@Test
	public void titleAndWholeCardToggleDetailsWhileSourceLinksKeepTheirOwnActions() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JTextArea title = (JTextArea)findTitle(panel, "Shades of Mort");
			Container card = title.getParent();
			layout();
			JButton expand = null;
			for (Component child : card.getComponents())
			{
				if (child instanceof JButton && ((JButton)child).getText().startsWith("Details"))
				{
					expand = (JButton)child;
				}
			}
			assertNotNull(expand);
			assertTrue(expand.getText().contains("▾"));
			card.dispatchEvent(
				new MouseEvent(card, MouseEvent.MOUSE_CLICKED, 1, 0, 3, 3, 1, false, MouseEvent.BUTTON1));
			assertTrue(expand.getText().contains("▴"));
			title.dispatchEvent(
				new MouseEvent(title, MouseEvent.MOUSE_CLICKED, 2, 0, 3, 3, 1, false, MouseEvent.BUTTON1));
			assertTrue("Title must toggle details", expand.getText().contains("▾"));
			verifyNoInteractions(actions);
			Container heading = (Container)card.getComponent(0);
			assertNull(findTitle(panel, "Open"));
			JTextArea metadata = (JTextArea)findTitle(heading, "");
			metadata.dispatchEvent(
				new MouseEvent(metadata, MouseEvent.MOUSE_CLICKED, 2, 0, 2, 2, 1, false, MouseEvent.BUTTON1));
			assertTrue(expand.getText().contains("▴"));
			((JComponent)card).getActionMap().get("details").actionPerformed(null);
			assertTrue(expand.getText().contains("▾"));
			((JComponent)card).getActionMap().get("details").actionPerformed(null);
			assertTrue(expand.getText().contains("▴"));
			Component sourceLink = findTitle(card, "Shades of Mort'ton ↗");
			assertTrue("Expected the independent wiki source link", sourceLink instanceof JButton);
			((JButton)sourceLink).doClick();
			verify(actions).source(any());
			assertTrue(expand.getText().contains("▴"));
		});
	}

	@Test
	public void syncMessageCanBeDismissedAndEveryLabelIsReadable() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			panel.message("Log in before syncing character progress.");
			layout();
			Component notice = findTitle(panel, "Log in before syncing");
			assertNotNull(notice);
			assertTrue(notice.getParent().isVisible());
			assertReadable(panel);
			panel.message("");
			assertFalse(notice.getParent().isVisible());
		});
	}

	private static void assertReadable(Container container)
	{
		for (Component c : container.getComponents())
		{
			if (c instanceof JTextArea || c instanceof AbstractButton && !((AbstractButton)c).getText().isEmpty())
			{
				assertTrue("Small font: " + c.getFont(), c.getFont().getSize() >= 13);
			}
			if (c instanceof JProgressBar)
			{
				assertTrue(c.getHeight() >= 22);
			}
			if (c instanceof Container)
			{
				assertReadable((Container)c);
			}
		}
	}

	@Test
	public void categoryBordersIconsAndGoldProgressKeepCommentsOutOfCardsAndNumbering() throws Exception
	{
		List<GuideRow> sample = new ArrayList<>();
		for (GuideRow.Kind kind : GuideRow.Kind.values())
		{
			if (kind != GuideRow.Kind.INFORMATION)
			{
				sample.add(new GuideRow(kind.name(), sample.size(), kind, kind.name() + " example", "",
					kind == GuideRow.Kind.QUEST ? "CONTACT" : null, Map.of(), Map.of(), Map.of()));
			}
		}
		sample.add(1, new GuideRow("note", 1, GuideRow.Kind.INFORMATION, "An explanation between steps.", "",
			null, Map.of(), Map.of(), Map.of()));
		sample.add(new GuideRow("milestone", 8, GuideRow.Kind.INFORMATION, "A different milestone!", "",
			null, Map.of(), Map.of(), Map.of()));
		GuideSnapshot mixed = new GuideSnapshot("sample", 1000, 1000, null, null, sample);
		SwingUtilities.invokeAndWait(() ->
		{
			panel.render(mixed, account, "Up to date", "", true);
			layout();
			assertEquals(7, panel.renderedRowCount());
			assertNotNull(findTitle(panel, "OSRS Wiki · 7 steps"));
			assertNull(findTitle(panel, "Open"));
			assertNull(findTitle(panel, "Integration status"));
			assertNull(findTitle(panel, "Clear remembered quest"));
			int number = 0;
			for (GuideRow row : sample)
			{
				JComponent block = (JComponent)findTitle(panel, row.getTitle()).getParent();
				MatteBorder border = (MatteBorder)((CompoundBorder)block.getBorder()).getOutsideBorder();
				if (!row.isActionable())
				{
					assertEquals(new Insets(1, 0, 1, 0), border.getBorderInsets());
					assertNull(findTitle(block, "Details"));
					assertNull(findTitle(block, "Open"));
					assertFalse(block.isOpaque());
					continue;
				}
				assertNotNull(findTitle(block, String.format("%03d /", ++number)));
				assertNotNull(stepIcon(block).getIcon());
				assertEquals(16, stepIcon(block).getIcon().getIconWidth());
				Color expected = row.getKind() == GuideRow.Kind.QUEST ? GuidePanel.GOLD : GuidePanel.kindColor(row.getKind());
				assertEquals(expected, border.getMatteColor());
			}
			JComponent quest = (JComponent)findTitle(panel, "QUEST example").getParent();
			assertEquals(GuidePanel.GOLD, findTitle(quest, "◐ In progress").getForeground());
		});
	}

	@Test
	public void standardSpritesReplaceFixedSizeFallbacksAndMiniquestRetainsItsOwnMark() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JComponent card = (JComponent)findTitle(panel, "Shades of Mort").getParent();
			Icon fallback = stepIcon(card).getIcon();
			panel.setStepSprite(GuideRow.Kind.QUEST, new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB));
			Icon loaded = stepIcon(card).getIcon();
			assertNotSame(fallback, loaded);
			assertTrue(loaded instanceof ImageIcon);
			assertEquals(16, loaded.getIconWidth());
			assertEquals(16, loaded.getIconHeight());
			GuideRow miniquest = new GuideRow("mini", 0, GuideRow.Kind.MINIQUEST, "Miniquest example", "", null,
				Map.of(), Map.of(), Map.of());
			panel.render(new GuideSnapshot("test", 1, 1, null, null, List.of(miniquest)), account, "", "", true);
			JComponent mini = (JComponent)findTitle(panel, "Miniquest example").getParent();
			assertTrue(stepIcon(mini).getIcon() instanceof StepIcons);
			assertNotSame(loaded, stepIcon(mini).getIcon());
		});
	}

	@Test
	public void updatedClassificationAndCommentPreviews() throws Exception
	{
		List<GuideRow> all = new GuideParser().parse(com.questcape.guide.GuideParserTest.fixture());
		List<GuideRow> sample = new ArrayList<>();
		for (GuideRow.Kind kind : Arrays.asList(GuideRow.Kind.UNLOCK, GuideRow.Kind.DIARY, GuideRow.Kind.ACTIVITY))
		{
			sample.add(all.stream().filter(row -> row.getKind() == kind).findFirst().orElseThrow());
		}
		sample.add(1, new GuideRow("unknown-example", 1, GuideRow.Kind.UNKNOWN, "Unmapped future step", "", null,
			Map.of(), Map.of(), Map.of()));
		sample.add(all.stream().filter(row -> row.getTitle().startsWith("Natural history")).findFirst().orElseThrow());
		sample.add(all.stream().filter(row -> row.getTitle().startsWith("Train Combat")).findFirst().orElseThrow());
		sample.add(all.stream().filter(row -> "CONTACT".equals(row.getQuestIdentity())).findFirst().orElseThrow());
		sample.addAll(all.subList(all.size() - 4, all.size()));
		AtomicReference<Throwable> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				panel.render(new GuideSnapshot("15336101", 1789210000000L, 1789210000000L, null, null, sample),
					account, "Up to date", "Character synced locally", true);
				layout();
				panel.routeScrollPane().getVerticalScrollBar().setValue(0);
				capture("sidebar-categories.png");
				captureRow("Unmapped future", false, "sidebar-unknown.png");
				captureRow("Natural history", false, "sidebar-miniquest-combat.png");
				captureRow("All quest XP", false, "sidebar-comment.png");
				panel.routeScrollPane().getVerticalScrollBar().setValue(Integer.MAX_VALUE);
				capture("sidebar-ending.png");
				assertFalse(panel.routeScrollPane().getHorizontalScrollBar().isVisible());
			}
			catch (Throwable error)
			{
				failure.set(error);
			}
		});
		if (failure.get() != null)
		{
			throw new AssertionError(failure.get());
		}
	}

	private static JLabel stepIcon(Container container)
	{
		for (Component child : container.getComponents())
		{
			if (child instanceof JLabel && ((JLabel)child).getIcon() != null)
			{
				return (JLabel)child;
			}
			if (child instanceof Container)
			{
				JLabel found = stepIcon((Container)child);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	@Test
	public void navigationAndNarrowPreview() throws Exception
	{
		AtomicReference<Throwable> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				assertEquals(snapshot.getRows().size(), panel.renderedRowCount());
				panel.routeScrollPane().getVerticalScrollBar().setValue(0);
				layout();
				assertEquals("Bottom ↓", panel.boundaryButton().getText());
				capture("sidebar-top.png");
				panel.boundaryButton().doClick();
				assertEquals("Top ↑", panel.boundaryButton().getText());
				capture("sidebar-bottom.png");
				panel.progressButton().doClick();
				layout();
				capture("sidebar-progress.png");
				captureRow("Hand in", false, "sidebar-hand-in.png");
				captureRow("Shades of Mort", true, "sidebar-details.png");
				assertFalse(panel.routeScrollPane().getHorizontalScrollBar().isVisible());
				panel.boundaryButton().doClick();
			}
			catch (Throwable e)
			{
				failure.set(e);
			}
		});
		if (failure.get() != null)
		{
			throw new AssertionError(failure.get());
		}
	}

	private void capture(String filename) throws IOException
	{
		BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		panel.printAll(graphics);
		graphics.dispose();
		Path directory = Paths.get("build", "ui-evidence");
		Files.createDirectories(directory);
		ImageIO.write(image, "png", directory.resolve(filename).toFile());
	}

	private void captureRow(String match, boolean expand, String filename) throws IOException
	{
		Component title = findTitle(panel.routeScrollPane().getViewport().getView(), match);
		assertNotNull("Missing reference row: " + match, title);
		Container card = title.getParent();
		if (expand)
		{
			for (Component child : card.getComponents())
			{
				if (child instanceof JButton && ((JButton)child).getText().startsWith("Details"))
				{
					((JButton)child).doClick();
				}
			}
		}
		layout();
		Component body = panel.routeScrollPane().getViewport().getView();
		int y = SwingUtilities.convertPoint(card, 0, 0, body).y;
		panel.routeScrollPane().getVerticalScrollBar().setValue(y);
		if (expand)
		{
			assertTrue("Expanded notes must increase card height", card.getHeight() > 400);
		}
		capture(filename);
	}

	private static Component findTitle(Component c, String match)
	{
		if (c instanceof JTextArea && ((JTextArea)c).getText().startsWith(match))
		{
			return c;
		}
		if (c instanceof JButton && ((JButton)c).getText().contains(match))
		{
			return c;
		}
		if (c instanceof Container)
		{
			for (Component child : ((Container)c).getComponents())
			{
				Component found = findTitle(child, match);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}
}
