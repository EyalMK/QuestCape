package com.questcape.ui.panel;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.progress.evaluation.Completion;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressTest;
import com.questcape.ui.GuideActions;
import com.questcape.ui.style.UiMetrics;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TrainingProgressTest
{
	@Test
	public void percentageArcStartsAtTopAndAdvancesCounterclockwise() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			TrainingProgress.Circle circle = new TrainingProgress.Circle();
			circle.update(25, false);
			circle.setSize(UiMetrics.TRAINING_CIRCLE, UiMetrics.TRAINING_CIRCLE);
			java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(circle.getWidth(), circle.getHeight(),
				java.awt.image.BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try
			{
				circle.paint(graphics);
			}
			finally
			{
				graphics.dispose();
			}
			Color left = new Color(image.getRGB(3, 18)), right = new Color(image.getRGB(33, 18));
			// The round end cap is antialiased: compare filled versus unfilled color instead of exact RGB.
			assertTrue("A quarter advances from the top toward the left", left.getRed() > right.getRed() + 80);
			assertTrue("The right side is still unfilled", left.getGreen() > right.getGreen() + 60);
			assertEquals("25%", circle.getToolTipText());
			assertFalse(circle.getAccessibleContext().getAccessibleDescription().contains("level:"));
		});
	}

	private GuideRow row(String title) throws Exception
	{
		return new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity(title))).get(0);
	}

	private AccountProgress account(Map<String, Integer> levels)
	{
		return ProgressTest.observation("fixture:circle", "Fixture", "STANDARD", Map.of(), levels);
	}

	@Test
	public void circleReplacesTextKeepsNumericTooltipAndRetainsItsClickSurfaceAcrossUpdates() throws Exception
	{
		GuideRow row = row("Train Woodcutting from level 52 to level 55");
		GuideSnapshot route = new GuideSnapshot("fixture", 1, 1, null, null, List.of(row));
		SwingUtilities.invokeAndWait(() ->
		{
			GuidePanel panel = new GuidePanel(mock(GuideActions.class),
				com.questcape.integration.links.TestTrainingLinks.local());
			try
			{
				panel.render(route, account(Map.of("WOODCUTTING", 54)), "", "", true);
				GuideRowCard card = DisclosurePositionTest.cards(panel).get(0);
				TrainingProgress.Circle circle = circles(card).get(0);
				assertEquals("66%", circle.getToolTipText());
				assertEquals(Integer.valueOf(66), circle.percent);
				assertEquals(Cursor.HAND_CURSOR, circle.getCursor().getType());
				assertEquals(Integer.valueOf(66),
					circle.getAccessibleContext().getAccessibleValue().getCurrentAccessibleValue());
				assertFalse(circle.complete);
				assertNoLegacyTrainingText(card);
				panel.render(route, account(Map.of("WOODCUTTING", 60)), "", "", true);
				assertSame(circle, circles(card).get(0));
				assertTrue(circle.complete);
				assertEquals("100%", circle.getToolTipText());
				assertNoLegacyTrainingText(card);
				circle.dispatchEvent(
					new MouseEvent(circle, MouseEvent.MOUSE_CLICKED, 0, 0, 2, 2, 1, false, MouseEvent.BUTTON1));
				assertTrue(card.getAccessibleContext().getAccessibleStateSet()
					.contains(javax.accessibility.AccessibleState.EXPANDED));
			}
			finally
			{
				panel.dispose();
			}
		});
	}

	@Test
	public void compoundMissingTargetOnlyAndUnsupportedTargetsDoNotInventProgress() throws Exception
	{
		List<GuideRow> rows = List.of(row("Train Woodcutting from level 52 to level 55 and Magic to level 65"),
			row("Train Mining to level 55"), row("Train Prayer from level 33 to level 37 and collect a reward"));
		SwingUtilities.invokeAndWait(() ->
		{
			GuidePanel panel = new GuidePanel(mock(GuideActions.class),
				com.questcape.integration.links.TestTrainingLinks.local());
			try
			{
				panel.render(new GuideSnapshot("fixture", 1, 1, null, null, rows),
					account(Map.of("WOODCUTTING", 55, "MINING", 54, "PRAYER", 50)), "", "", true);
				List<GuideRowCard> cards = DisclosurePositionTest.cards(panel);
				assertTrue(circles(cards.get(0)).get(0).complete);
				assertNull(circles(cards.get(0)).get(1).percent);
				assertFalse(circles(cards.get(0)).get(1).complete);
				assertEquals("—", circles(cards.get(1)).get(0).getToolTipText());
				assertNull(circles(cards.get(1)).get(0).percent);
				assertFalse(circles(cards.get(2)).get(0).complete);
				assertEquals(Completion.State.UNKNOWN, cards.get(2).last.getState());
			}
			finally
			{
				panel.dispose();
			}
		});
	}

	static List<TrainingProgress.Circle> circles(Container parent)
	{
		List<TrainingProgress.Circle> result = new ArrayList<>();
		for (Component child : parent.getComponents())
		{
			if (child instanceof TrainingProgress.Circle)
			{
				result.add((TrainingProgress.Circle)child);
			}
			else if (child instanceof Container)
			{
				result.addAll(circles((Container)child));
			}
		}
		return result;
	}

	private static void assertNoLegacyTrainingText(Container parent)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof javax.swing.text.JTextComponent)
			{
				String text = ((javax.swing.text.JTextComponent)child).getText();
				assertFalse(text.startsWith("Current level:"));
				assertFalse(text.equals("✓ Complete"));
				assertFalse(text.equals("◐ In progress"));
			}
			if (child instanceof Container)
			{
				assertNoLegacyTrainingText((Container)child);
			}
		}
	}
}
