package com.questcape.ui;

import com.questcape.guide.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Isolated Swing geometry only; no events target RuneScape. */
public class DisclosurePositionTest
{
	@Test
	public void largeAdviceCycleKeepsCardsScreenPositionAndSettlesTheRangeBeforeRestore() throws Exception
	{
		GuidePanel[] panel = new GuidePanel[1];
		GuideRowCard[] selected = new GuideRowCard[1];
		int[] original = new int[1];
		LookAndFeel[] previous = new LookAndFeel[1];
		try
		{
			SwingUtilities.invokeAndWait(() ->
			{
				previous[0] = UIManager.getLookAndFeel();
				assertTrue(net.runelite.client.ui.laf.RuneLiteLAF.setup());
				List<GuideRow> source = new ArrayList<>();
				for (int index = 0; index < 24; index++)
				{
					source
						.add(GuideAssembler.row("row" + index, index, GuideRow.Kind.QUEST, "Cook's Assistant " + index,
							"", "COOKS_ASSISTANT", Map.of(),
							index == 8 ? Map.of("Additional info", "Long route advice with wrapping. ".repeat(100))
								: Map.of(),
							Map.of()));
				}
				panel[0] = new GuidePanel(mock(GuidePanel.Actions.class),
					com.questcape.integration.TestTrainingLinks.local());
				panel[0].setSize(242, 820);
				panel[0].render(new GuideSnapshot("fixture", 1, 1, null, null, source), null, "", "", false);
				layout(panel[0]);
				selected[0] = cards(panel[0]).get(8);
				JScrollPane scroll = panel[0].routeScrollPane();
				int y = SwingUtilities.convertPoint(selected[0].getParent(), selected[0].getLocation(),
					scroll.getViewport().getView()).y;
				scroll.getVerticalScrollBar().setValue(y - 110);
				original[0] = screenY(selected[0], scroll);
				selected[0].getActionMap().get("details").actionPerformed(null);
			});
			SwingUtilities.invokeAndWait(() ->
			{
				assertEquals(original[0], screenY(selected[0], panel[0].routeScrollPane()));
				selected[0].getActionMap().get("details").actionPerformed(null);
				// Simulate the scrollbar clamping in response to the shorter content before
				// the queued restoration: it must use the final geometry, not this interim value.
				panel[0].routeScrollPane().getVerticalScrollBar().setValue(0);
			});
			SwingUtilities.invokeAndWait(() ->
			{
				assertEquals(original[0], screenY(selected[0], panel[0].routeScrollPane()));
				selected[0].getActionMap().get("details").actionPerformed(null);
				panel[0].getActionMap().get("routeTop").actionPerformed(null);
			});
			SwingUtilities
				.invokeAndWait(() -> assertEquals("Newer user navigation wins over queued disclosure restoration", 0,
					panel[0].routeScrollPane().getVerticalScrollBar().getValue()));
		}
		finally
		{
			SwingUtilities.invokeAndWait(() ->
			{
				if (panel[0] != null)
				{
					panel[0].dispose();
				}
				try
				{
					if (previous[0] != null)
					{
						UIManager.setLookAndFeel(previous[0]);
					}
				}
				catch (UnsupportedLookAndFeelException failure)
				{
					throw new AssertionError(failure);
				}
			});
		}
	}

	static int screenY(Component card, JScrollPane scroll)
	{
		return SwingUtilities.convertPoint(card.getParent(), card.getLocation(), scroll.getViewport().getView()).y
			- scroll.getViewport().getViewPosition().y;
	}

	static List<GuideRowCard> cards(Container parent)
	{
		List<GuideRowCard> result = new ArrayList<>();
		for (Component child : parent.getComponents())
		{
			if (child instanceof GuideRowCard)
			{
				result.add((GuideRowCard)child);
			}
			else if (child instanceof Container)
			{
				result.addAll(cards((Container)child));
			}
		}
		return result;
	}

	static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents())
		{
			if (child instanceof Container)
			{
				layout((Container)child);
			}
		}
	}
}
