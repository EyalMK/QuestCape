package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.progress.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
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
    @BeforeClass public static void fixtures() throws Exception
    {
        List<GuideRow> all = new GuideParser().parse(com.questcape.guide.GuideParserTest.fixture());
        int start = 0;
        for (int i = 0; i < all.size(); i++) if (all.get(i).getTitle().contains("from level 34 to level 40")) { start = i; break; }
        snapshot = new GuideSnapshot("15336101", 1789210000000L, 1789210000000L, null, null, all.subList(start, Math.min(start + 18, all.size())));
        String json;
        try (InputStream in = GuidePanelTest.class.getResourceAsStream("/fixtures/wikisync-maple-scout.json"))
        { json = new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8); }
        AccountProgress parsed = new WikiSyncProgressProvider(null).parse("maple scout", json, 1789210000000L);
        account = new AccountProgress("live:test:STANDARD", "maple scout", "STANDARD", "Live RuneLite", parsed.getRetrievedAt(), parsed.getObservedAt(), parsed.getQuests(), parsed.getLevels(), Map.of(), Set.of());
    }
    @Before public void create() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            actions = mock(GuidePanel.Actions.class);
            panel = new GuidePanel(actions, new TrainingGuideResolver(null)); panel.setSize(242, 820);
            panel.render(snapshot, account, null, "Up to date", "WikiSync synced", "Integration test fixture", true); layout();
        });
        SwingUtilities.invokeAndWait(this::layout);
    }
    private void layout()
    {
        for (int i = 0; i < 3; i++)
        {
            invalidateTree(panel);
            layoutTree(panel);
            JViewport viewport = panel.routeScrollPane().getViewport(); Component body = viewport.getView();
            body.setSize(viewport.getWidth(), body.getPreferredSize().height); layoutTree((Container)body);
        }
    }
    private static void invalidateTree(Container c) { for (Component child : c.getComponents()) if (child instanceof Container) invalidateTree((Container)child); c.invalidate(); }
    private static void layoutTree(Container c) { c.doLayout(); for (Component child : c.getComponents()) if (child instanceof Container) layoutTree((Container)child); }
    @Test public void loggedOutClickThenRepeatedLiveRendersNeverMoveTheViewport() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            panel.render(snapshot, null, null, "Up to date", "", "", false);
            panel.message("Log in before selecting a quest helper."); layout();
            panel.routeScrollPane().getVerticalScrollBar().setValue(650);
        });
        SwingUtilities.invokeAndWait(() ->
        {
            panel.render(snapshot, account, null, "Up to date", "WikiSync synced", "", true); layout();
            panel.routeScrollPane().getVerticalScrollBar().setValue(650);
        });
        for (int i = 0; i < 40; i++)
        {
            final int iteration = i;
            SwingUtilities.invokeAndWait(() ->
            {
                panel.render(snapshot, account, null, "Up to date", "WikiSync synced", "Update " + iteration, true); layout();
                assertEquals("Live render must preserve user scroll", 650, panel.routeScrollPane().getVerticalScrollBar().getValue());
            });
        }
        SwingUtilities.invokeAndWait(() -> assertNeverScrollCaret(panel));
    }
    private static void assertNeverScrollCaret(Container c)
    {
        for (Component child : c.getComponents())
        {
            if (child instanceof JTextArea) assertEquals(DefaultCaret.NEVER_UPDATE, ((DefaultCaret)((JTextArea)child).getCaret()).getUpdatePolicy());
            if (child instanceof Container) assertNeverScrollCaret((Container)child);
        }
    }
    @Test public void titleAndWholeCardToggleDetailsWhileOpenButtonAndLinksKeepTheirOwnActions() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            JTextArea title = (JTextArea)findTitle(panel, "Shades of Mort"); Container card = title.getParent();
            Icon helperIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
            panel.setQuestHelperIcon(helperIcon); layout();
            JButton expand = null;
            for (Component child : card.getComponents()) if (child instanceof JButton && ((JButton)child).getText().startsWith("Details")) expand = (JButton)child;
            assertNotNull(expand); assertTrue(expand.getText().contains("▾"));
            card.dispatchEvent(new MouseEvent(card, MouseEvent.MOUSE_CLICKED, 1, 0, 3, 3, 1, false, MouseEvent.BUTTON1));
            assertTrue(expand.getText().contains("▴"));
            title.dispatchEvent(new MouseEvent(title, MouseEvent.MOUSE_CLICKED, 2, 0, 3, 3, 1, false, MouseEvent.BUTTON1));
            assertTrue("Title must toggle details", expand.getText().contains("▾")); verify(actions, never()).quest(any());
            Container heading = (Container)card.getComponent(0);
            JButton open = (JButton)findTitle(heading, "Open"); assertNotNull(open); assertSame(helperIcon, open.getIcon());
            assertEquals(SwingConstants.LEFT, open.getHorizontalTextPosition());
            assertTrue("Open must fit the header", open.getX() + open.getWidth() <= heading.getWidth());
            open.doClick(); verify(actions).quest(any()); assertTrue("Open must not toggle details", expand.getText().contains("▾"));
            JTextArea metadata = (JTextArea)heading.getComponent(0);
            metadata.dispatchEvent(new MouseEvent(metadata, MouseEvent.MOUSE_CLICKED, 2, 0, 2, 2, 1, false, MouseEvent.BUTTON1));
            assertTrue(expand.getText().contains("▴"));
            ((JComponent)card).getActionMap().get("details").actionPerformed(null); assertTrue(expand.getText().contains("▾"));
            ((JComponent)card).getActionMap().get("details").actionPerformed(null); assertTrue(expand.getText().contains("▴"));
            Component sourceLink = findTitle(card, "Shades of Mort'ton ↗");
            assertTrue("Expected the independent wiki source link", sourceLink instanceof JButton);
            ((JButton)sourceLink).doClick(); verify(actions).source(any());
            assertTrue(expand.getText().contains("▴"));
        });
    }
    @Test public void questMessageIsAboveClickedCardAndEveryLabelIsReadable() throws Exception
    {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() ->
        {
            try
            {
                GuideRow target = snapshot.getRows().stream().filter(r -> r.getTitle().contains("Shades of Mort")).findFirst().orElseThrow();
                panel.questMessage(target.getKey(), "Log in before opening Quest Helper."); layout();
                Container card = findTitle(panel, "Shades of Mort").getParent(); Container entry = card.getParent();
                assertEquals(2, entry.getComponentCount()); assertSame(card, entry.getComponent(1));
                assertTrue(entry.getComponent(0).isVisible()); assertTrue(entry.getComponent(0).getY() < card.getY());
                assertNotNull(findTitle(entry.getComponent(0), "Log in before opening"));
                assertReadable(panel);
                panel.routeScrollPane().getVerticalScrollBar().setValue(SwingUtilities.convertPoint(entry, 0, 0, panel.routeScrollPane().getViewport().getView()).y);
                capture("sidebar-quest-message.png");
                panel.message(""); assertFalse(entry.getComponent(0).isVisible());
            }
            catch (Throwable e) { failure.set(e); }
        });
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
    private static void assertReadable(Container container)
    {
        for (Component c : container.getComponents())
        {
            if (c instanceof JTextArea || c instanceof AbstractButton && !((AbstractButton)c).getText().isEmpty())
                assertTrue("Small font: " + c.getFont(), c.getFont().getSize() >= 13);
            if (c instanceof JProgressBar) assertTrue(c.getHeight() >= 22);
            if (c instanceof Container) assertReadable((Container)c);
        }
    }
    @Test public void navigationAndNarrowPreview() throws Exception
    {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() ->
        {
            try
            {
                assertEquals(snapshot.getRows().size(), panel.renderedRowCount());
                panel.routeScrollPane().getVerticalScrollBar().setValue(0); layout();
                assertEquals("Bottom ↓", panel.boundaryButton().getText()); capture("sidebar-top.png");
                panel.boundaryButton().doClick(); assertEquals("Top ↑", panel.boundaryButton().getText()); capture("sidebar-bottom.png");
                panel.progressButton().doClick(); layout(); capture("sidebar-progress.png");
                captureRow("Hand in", false, "sidebar-hand-in.png");
                captureRow("Shades of Mort", true, "sidebar-details.png");
                assertFalse(panel.routeScrollPane().getHorizontalScrollBar().isVisible());
                panel.boundaryButton().doClick();
            }
            catch (Throwable e) { failure.set(e); }
        });
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
    private void capture(String filename) throws IOException
    {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics(); panel.printAll(graphics); graphics.dispose();
        Path directory = Paths.get("build", "ui-evidence"); Files.createDirectories(directory); ImageIO.write(image, "png", directory.resolve(filename).toFile());
    }
    private void captureRow(String match, boolean expand, String filename) throws IOException
    {
        Component title = findTitle(panel.routeScrollPane().getViewport().getView(), match);
        assertNotNull("Missing reference row: " + match, title);
        Container card = title.getParent();
        if (expand) for (Component child : card.getComponents())
            if (child instanceof JButton && ((JButton)child).getText().startsWith("Details")) ((JButton)child).doClick();
        layout();
        Component body = panel.routeScrollPane().getViewport().getView();
        int y = SwingUtilities.convertPoint(card, 0, 0, body).y;
        panel.routeScrollPane().getVerticalScrollBar().setValue(y);
        if (expand) assertTrue("Expanded notes must increase card height", card.getHeight() > 400);
        capture(filename);
    }
    private static Component findTitle(Component c, String match)
    {
        if (c instanceof JTextArea && ((JTextArea)c).getText().startsWith(match)) return c;
        if (c instanceof JButton && ((JButton)c).getText().contains(match)) return c;
        if (c instanceof Container) for (Component child : ((Container)c).getComponents())
        { Component found = findTitle(child, match); if (found != null) return found; }
        return null;
    }
}
