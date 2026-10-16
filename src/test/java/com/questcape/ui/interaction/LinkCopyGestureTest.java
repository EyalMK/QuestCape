package com.questcape.ui.interaction;

import com.questcape.ui.GuideActions;
import com.questcape.ui.panel.TestGuideLinks;
import com.questcape.ui.style.UiCopy;
import com.questcape.ui.style.UiMetrics;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Synthetic Swing events only; clipboard service is mocked and the live game is never targeted. */
public class LinkCopyGestureTest
{
	@Test
	public void shiftRightCopiesOnceWithoutOpeningOrResizingAndFeedbackCleansUp() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			GuideActions actions = mock(GuideActions.class);
			String url = "https://oldschool.runescape.wiki/w/Lunar_Diplomacy";
			when(actions.copyLink(url)).thenReturn(true);
			JButton button = TestGuideLinks.link(url, UiCopy.QUEST_PAGE, actions);
			JPanel root = new JPanel();
			root.add(button);
			button.setSize(180, 30);
			Dimension before = button.getPreferredSize();
			gesture(button, 0, MouseEvent.BUTTON3);
			gesture(button, MouseEvent.SHIFT_DOWN_MASK, MouseEvent.BUTTON1);
			verify(actions, never()).copyLink(anyString());
			verify(actions).source(url);
			clearInvocations(actions);
			gesture(button, MouseEvent.SHIFT_DOWN_MASK, MouseEvent.BUTTON3);
			verify(actions).copyLink(url);
			verify(actions, never()).source(anyString());
			LinkCopyGesture feedback = LinkCopyGesture.of(button);
			assertTrue(feedback.feedbackActive());
			assertEquals(before, button.getPreferredSize());
			assertTrue(button.getAccessibleContext().getAccessibleDescription().contains(UiCopy.LINK_COPIED));
			assertTrue(UiMetrics.COPY_FEEDBACK_MILLIS >= 1000 && UiMetrics.COPY_FEEDBACK_MILLIS <= 2000);
			feedback.clear();
			assertFalse(feedback.feedbackActive());
			gesture(button, MouseEvent.SHIFT_DOWN_MASK, MouseEvent.BUTTON3);
			assertTrue(feedback.feedbackActive());
			LinkCopyGesture.disposeTree(root);
			assertFalse(feedback.feedbackActive());
			clearInvocations(actions);
			gesture(button, MouseEvent.SHIFT_DOWN_MASK, MouseEvent.BUTTON3);
			verifyNoInteractions(actions);
		});
	}

	@Test
	public void failedClipboardDoesNotClaimSuccessAndDragDoesNotCopy() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			GuideActions actions = mock(GuideActions.class);
			JButton button = TestGuideLinks.link("https://oldschool.runescape.wiki/w/Mining", "Mining", actions);
			button.setSize(180, 30);
			button.dispatchEvent(new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 0, MouseEvent.SHIFT_DOWN_MASK, 2, 2,
				1, false, MouseEvent.BUTTON3));
			button.dispatchEvent(
				new MouseEvent(button, MouseEvent.MOUSE_DRAGGED, 0, MouseEvent.SHIFT_DOWN_MASK, 8, 2, 0, false));
			button.dispatchEvent(new MouseEvent(button, MouseEvent.MOUSE_RELEASED, 0, MouseEvent.SHIFT_DOWN_MASK, 8, 2,
				1, false, MouseEvent.BUTTON3));
			verifyNoInteractions(actions);
			gesture(button, MouseEvent.SHIFT_DOWN_MASK, MouseEvent.BUTTON3);
			assertTrue(button.getAccessibleContext().getAccessibleDescription().contains(UiCopy.LINK_COPY_FAILED));
			LinkCopyGesture.of(button).clear();
		});
	}

	private static void gesture(JButton button, int modifiers, int mouseButton)
	{
		button
			.dispatchEvent(new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 0, modifiers, 2, 2, 1, false, mouseButton));
		button.dispatchEvent(
			new MouseEvent(button, MouseEvent.MOUSE_RELEASED, 0, modifiers, 2, 2, 1, false, mouseButton));
	}
}
