package com.questcape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.text.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Native caret gestures in isolated Swing fixtures only; never game input or the OS clipboard. */
public class CtrlTextSelectionTest
{
	private LookAndFeel previous;

	@Before
	public void setup() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			previous = UIManager.getLookAndFeel();
			assertTrue(net.runelite.client.ui.laf.RuneLiteLAF.setup());
		});
	}

	@After
	public void restore() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				UIManager.setLookAndFeel(previous);
			}
			catch (UnsupportedLookAndFeelException failure)
			{
				throw new AssertionError(failure);
			}
		});
	}

	@Test
	public void nativeDragWorksForPlainAndStyledTextWithoutReplacingCaretOrCopyBinding() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			for (JTextComponent text : List.of(new JTextArea("Alpha beta gamma delta"),
				new SkillText("Alpha beta gamma delta", new Font(Font.DIALOG, Font.PLAIN, 14),
					Color.WHITE, Map.of(), List.of())))
			{
				text.setEditable(false);
				text.setSize(220, 100);
				Caret original = text.getCaret();
				Object copyBinding = text.getInputMap().get(KeyStroke.getKeyStroke("ctrl C"));
				assertNotNull(copyBinding);
				AtomicInteger toggles = new AtomicInteger();
				JPanel card = new JPanel();
				card.add(text);
				CardInteraction interaction = new CardInteraction(card, toggles::incrementAndGet, () ->
				{
				});
				interaction.install();
				mouse(text, MouseEvent.MOUSE_MOVED, 1, KeyEvent.CTRL_DOWN_MASK, 0);
				assertEquals(Cursor.TEXT_CURSOR, text.getCursor().getType());
				mouse(text, MouseEvent.MOUSE_PRESSED, 8, KeyEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK, 2);
				mouse(text, MouseEvent.MOUSE_RELEASED, 8, KeyEvent.CTRL_DOWN_MASK, 2);
				mouse(text, MouseEvent.MOUSE_CLICKED, 8, KeyEvent.CTRL_DOWN_MASK, 2);
				assertEquals("beta", text.getSelectedText());
				mouse(text, MouseEvent.MOUSE_PRESSED, 1, KeyEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK, 1);
				mouse(text, MouseEvent.MOUSE_DRAGGED, 10, KeyEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK, 0);
				mouse(text, MouseEvent.MOUSE_RELEASED, 10, KeyEvent.CTRL_DOWN_MASK, 1);
				assertEquals("lpha beta", text.getSelectedText());
				Clipboard clipboard = new Clipboard("isolated-test-only");
				text.getTransferHandler().exportToClipboard(text, clipboard, TransferHandler.COPY);
				try
				{
					assertEquals("lpha beta", clipboard.getData(DataFlavor.stringFlavor));
				}
				catch (Exception failure)
				{
					throw new AssertionError(failure);
				}
				assertTrue(text.isFocusable());
				key(text, false);
				assertEquals("lpha beta", text.getSelectedText());
				assertEquals(Cursor.HAND_CURSOR, text.getCursor().getType());
				assertSame(original, text.getCaret());
				assertEquals(copyBinding, text.getInputMap().get(KeyStroke.getKeyStroke("ctrl C")));
				assertNotNull(text.getActionMap().get(copyBinding));
				assertEquals(0, toggles.get());
				mouse(text, MouseEvent.MOUSE_PRESSED, 3, MouseEvent.BUTTON1_DOWN_MASK, 1);
				mouse(text, MouseEvent.MOUSE_RELEASED, 3, 0, 1);
				mouse(text, MouseEvent.MOUSE_CLICKED, 3, 0, 1);
				assertEquals(1, toggles.get());
				assertNull(text.getSelectedText());
				assertFalse(text.isFocusable());
				interaction.dispose();
			}
		});
	}

	@Test
	public void focusAndDisposalClearSelectionWhileIndependentControlsKeepTheirRoles() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JPanel card = new JPanel();
			JTextArea text = new JTextArea("Selectable advice");
			JLabel projection = new JLabel("54");
			projection.setToolTipText("Projected Mining level");
			InteractionRole.readOnly(projection);
			JButton link = new JButton("Wiki quick guide");
			AtomicInteger toggles = new AtomicInteger(), links = new AtomicInteger();
			link.addActionListener(e -> links.incrementAndGet());
			card.add(text);
			card.add(projection);
			card.add(link);
			CardInteraction interaction = new CardInteraction(card, toggles::incrementAndGet, () ->
			{
			});
			interaction.install();
			key(text, true);
			text.select(0, 10);
			link.doClick(0);
			assertEquals(1, links.get());
			assertEquals(0, toggles.get());
			assertEquals(Cursor.DEFAULT_CURSOR, projection.getCursor().getType());
			assertEquals("Projected Mining level", projection.getToolTipText());
			assertNull("Hint uses its own delayed popup, not shared tooltip timing", text.getToolTipText());
			for (FocusListener listener : text.getFocusListeners())
			{
				listener.focusLost(new FocusEvent(text, FocusEvent.FOCUS_LOST, false, new JTextField()));
			}
			assertNull(text.getSelectedText());
			assertFalse(text.isFocusable());
			key(text, true);
			text.select(0, 5);
			assertNull(
				card.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke("ESCAPE")));
			interaction.endSelection();
			assertNull(text.getSelectedText());
			key(text, true);
			text.select(0, 5);
			interaction.dispose();
			key(text, true);
			assertNull(text.getSelectedText());
			assertFalse(text.isFocusable());
			assertEquals(Cursor.HAND_CURSOR, text.getCursor().getType());
		});
	}

	private static void mouse(JTextComponent text, int id, int offset, int modifiers, int count)
	{
		try
		{
			Rectangle2D position = text.modelToView2D(offset);
			int button = id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED ? MouseEvent.NOBUTTON
				: MouseEvent.BUTTON1;
			text.dispatchEvent(new MouseEvent(text, id, 1, modifiers, (int)position.getX(),
				(int)position.getY() + (int)position.getHeight() / 2, count, false, button));
		}
		catch (BadLocationException failure)
		{
			throw new AssertionError(failure);
		}
	}

	private static void key(JTextComponent text, boolean pressed)
	{
		KeyEvent event = new KeyEvent(text, pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED,
			1, pressed ? KeyEvent.CTRL_DOWN_MASK : 0, KeyEvent.VK_CONTROL, KeyEvent.CHAR_UNDEFINED);
		for (KeyListener listener : text.getKeyListeners())
		{
			if (pressed)
			{
				listener.keyPressed(event);
			}
			else
			{
				listener.keyReleased(event);
			}
		}
		assertFalse(event.isConsumed());
	}
}
