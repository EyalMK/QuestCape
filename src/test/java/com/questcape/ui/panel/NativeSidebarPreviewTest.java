package com.questcape.ui.panel;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.integration.OperationOutcome.State;
import com.questcape.persistence.StoredView.SaveState;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressTest;
import com.questcape.ui.GuideActions;
import com.questcape.ui.SidebarStatus;
import com.questcape.ui.sidebar.SidebarImages;
import com.questcape.ui.style.UiCopy;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.util.ImageUtil;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Actual Swing rendering with source fixtures/synthetic observations; never a game capture. */
public class NativeSidebarPreviewTest
{
	@Test
	public void nativeNarrowShortAndExpandedViewsWrapAndRetainActualImages() throws Exception
	{
		List<GuideRow> captured = new GuideParser().parse(GuideParserTest.fixture());
		GuideRow lunar = captured.stream().filter(row -> row.getTitle().equals("Lunar Diplomacy")).findFirst()
			.orElseThrow();
		List<GuideRow> training = new GuideParser()
			.parse(GuideParserTest.table(GuideParserTest.activity("Train Woodcutting from level 52 to level 55")
				+ GuideParserTest.activity("Train Defence from level 60 to level 65")));
		GuideSnapshot route = new GuideSnapshot("fixture-15336101", 1, 1, null, null,
			List.of(training.get(0), lunar, training.get(1)));
		AccountProgress account = ProgressTest.observation("synthetic:preview", "Synthetic fixture", "STANDARD",
			Map.of("LUNAR_DIPLOMACY", AccountProgress.QuestStatus.INCOMPLETE),
			Map.of("WOODCUTTING", 54, "DEFENCE", 65));
		Map<Skill, BufferedImage> icons = new EnumMap<>(Skill.class), projections = new EnumMap<>(Skill.class);
		SkillIconManager source = new SkillIconManager();
		for (Skill skill : Skill.values())
		{
			BufferedImage image = source.getSkillImage(skill, false);
			if (image != null)
			{
				icons.put(skill, ImageUtil.resizeImage(image, 16, 16));
				projections.put(skill, ImageUtil.resizeImage(image, 18, 18));
			}
		}
		BufferedImage plugin = ImageUtil.resizeImage(SidebarImages.load("/quest-route-icon.png"), 22, 22),
			github = SidebarImages.load("/github-mark.png");
		Path output = Paths.get("build", "ui-evidence", "native-sidebar-" + System.getProperty("questcape.version"));
		Files.createDirectories(output);
		SwingUtilities.invokeAndWait(() ->
		{
			LookAndFeel original = UIManager.getLookAndFeel();
			assertTrue(net.runelite.client.ui.laf.RuneLiteLAF.setup());
			try
			{
				for (int[] size : new int[][]
				{
						{ 240, 1364 },
						{ 225, 640 },
						{ 320, 900 } })
				{
					long began = System.nanoTime();
					GuidePanel panel = new GuidePanel(mock(GuideActions.class),
						com.questcape.integration.links.TestTrainingLinks.local());
					try
					{
						panel.setHeaderIcons(plugin, github);
						panel.setSkillIcons(icons, projections);
						panel.render(route, account, SidebarStatus.idle("", ""), true);
						panel.setSize(size[0], size[1]);
						layout(panel);
						for (Component child : ((Container)panel.routeScrollPane().getViewport().getView())
							.getComponents())
						{
							if (child instanceof Container)
							{
								for (Component card : ((Container)child).getComponents())
								{
									if (card instanceof GuideRowCard
										&& ((GuideRowCard)card).row.getKind() == GuideRow.Kind.QUEST)
									{
										((JComponent)card).getActionMap().get("details").actionPerformed(null);
									}
								}
							}
						}
						layout(panel);
						assertTextFits(panel);
						assertTrue(panel.progressButton().getWidth() > panel.boundaryButton().getWidth());
						BufferedImage rendered = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
						Graphics2D graphics = rendered.createGraphics();
						try
						{
							panel.printAll(graphics);
						}
						finally
						{
							graphics.dispose();
						}
						ImageIO.write(rendered, "png",
							output.resolve("sidebar-" + size[0] + "x" + size[1] + ".png").toFile());
						System.out.printf(Locale.ROOT,
							"Synthetic EDT composition/layout/paint/PNG %dx%d: %.2f ms; no game timing claim.%n",
							size[0], size[1], (System.nanoTime() - began) / 1_000_000.0);
						if (size[0] == 320)
						{
							panel.setSize(320, 1364);
							assertTrue(activate(panel, UiCopy.RELATED));
							layout(panel);
							BufferedImage selection = new BufferedImage(320, 1364, BufferedImage.TYPE_INT_ARGB);
							Graphics2D selected = selection.createGraphics();
							try
							{
								panel.printAll(selected);
							}
							finally
							{
								selected.dispose();
							}
							ImageIO.write(selection, "png",
								output.resolve("sidebar-related-open.png").toFile());
						}
						if (size[0] == 240)
						{
							// Device-transform evidence only; Windows display scaling still requires user acceptance.
							for (double scale : new double[]
							{ 1.25, 1.5 })
							{
								BufferedImage scaled = new BufferedImage((int)Math.ceil(size[0] * scale),
									(int)Math.ceil(size[1] * scale), BufferedImage.TYPE_INT_ARGB);
								Graphics2D device = scaled.createGraphics();
								try
								{
									device.scale(scale, scale);
									panel.printAll(device);
								}
								finally
								{
									device.dispose();
								}
								ImageIO.write(scaled, "png",
									output.resolve("sidebar-device-scale-" + scale + ".png").toFile());
							}
						}
					}
					finally
					{
						panel.dispose();
					}
				}
				GuidePanel edge = new GuidePanel(mock(GuideActions.class),
					com.questcape.integration.links.TestTrainingLinks.local());
				try
				{
					GuideRow compound = com.questcape.guide.classification.GuideAssembler.row("compound-preview", 0,
						GuideRow.Kind.TRAINING,
						"Train Woodcutting from level 52 to level 55 and Magic to level 65 "
							+ "(retain this longer source recommendation while comparing each skill separately)",
						"", null, Map.of(), Map.of("Additional info", "This longer synthetic recommendation must wrap completely. "
							+ "No skill images are supplied in this preview, so stable placeholders must retain the text and geometry."),
						Map.of("https://oldschool.runescape.wiki/w/Woodcutting", "Woodcutting",
							"https://oldschool.runescape.wiki/w/Magic", "Magic"));
					edge.setHeaderIcons(plugin, github);
					edge.render(new GuideSnapshot("edge-fixture", 1, 1, null, null, List.of(compound, lunar)), account,
						new SidebarStatus(State.FAILED, State.IDLE, SaveState.SAVED, SaveState.UNSAVED,
							"Synthetic route refresh unavailable.", ""), true);
					edge.setSize(320, 900);
					layout(edge);
					expandCards(edge);
					layout(edge);
					assertTextFits(edge);
					BufferedImage rendered = new BufferedImage(320, 900, BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = rendered.createGraphics();
					try { edge.printAll(graphics); } finally { graphics.dispose(); }
					ImageIO.write(rendered, "png", output.resolve("sidebar-compound-notices-placeholders.png").toFile());
				}
				finally { edge.dispose(); }
			}
			catch (Exception failure)
			{
				throw new AssertionError(failure);
			}
			finally
			{
				try
				{
					UIManager.setLookAndFeel(original);
				}
				catch (Exception failure)
				{
					throw new AssertionError(failure);
				}
			}
		});
	}

	private static void expandCards(Container parent)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof GuideRowCard) { ((JComponent)child).getActionMap().get("details").actionPerformed(null); }
			else if (child instanceof Container) { expandCards((Container)child); }
		}
	}

	private static boolean activate(Container parent, String prefix)
	{
		for (Component child : parent.getComponents())
		{
			if (!child.isVisible())
			{
				continue;
			}
			if (child instanceof JButton && ((JButton)child).getText().startsWith(prefix))
			{
				((JButton)child).doClick(0);
				return true;
			}
			if (child instanceof Container && activate((Container)child, prefix))
			{
				return true;
			}
		}
		return false;
	}

	private static void layout(Container parent)
	{
		for (int i = 0; i < 4; i++)
		{
			invalidateTree(parent);
			layoutTree(parent);
		}
	}

	private static void invalidateTree(Container parent)
	{
		parent.invalidate();
		for (Component child : parent.getComponents())
		{
			if (child instanceof Container)
			{
				invalidateTree((Container)child);
			}
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

	private static void assertTextFits(Container parent) throws Exception
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JTextComponent && child.isVisible() && child.getWidth() > 0)
			{
				JTextComponent text = (JTextComponent)child;
				if (!text.getText().isEmpty())
				{
					java.awt.geom.Rectangle2D last = text.modelToView2D(text.getDocument().getLength() - 1);
					if (last != null)
					{
						assertTrue(text.getText() + " height=" + text.getHeight() + " last=" + last,
							last.getMaxY() <= text.getHeight() + 1);
					}
				}
			}
			if (child instanceof Container)
			{
				assertTextFits((Container)child);
			}
		}
	}
}
