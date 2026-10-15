package com.questcape.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;
import javax.swing.text.StyleConstants;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class SkillTextTest
{
	@Test
	public void skillMentionsKeepWordingAndIconsBeforeNamesIncludingAliases() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			Icon icon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
			String source = "Use Herblore, HERBLORE and Runecrafting; Defense. Farmingdale and magical stay plain.";
			SkillText text = new SkillText(source, new Font("Dialog", Font.PLAIN, 14), Color.WHITE,
				Map.of(Skill.HERBLORE, icon, Skill.RUNECRAFT, icon, Skill.DEFENCE, icon),
				com.questcape.guide.SkillMentionInterpreter.interpret(source));
			assertEquals(source, text.getAccessibleContext().getAccessibleName());
			assertEquals(source, text.getText().replace("\ufffc ", ""));
			int count = 0;
			for (int i = 0; i < text.getText().length(); i++)
			{
				Icon actual = StyleConstants.getIcon(text.getStyledDocument().getCharacterElement(i).getAttributes());
				if (actual != null)
				{
					assertSame(icon, actual);
					assertEquals('\ufffc', text.getText().charAt(i));
					assertEquals(' ', text.getText().charAt(i + 1));
					count++;
				}
			}
			assertEquals(4, count);
			assertFalse(text.isEditable());
		});
	}

	@Test
	public void lateIconsReplacePlaceholdersWithoutChangingTextAndWrapToWidth() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			Icon herblore = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
			Icon craft = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
			SkillText text = new SkillText("Herblore or Runecraft. Use Herblore again.",
				new Font("Dialog", Font.PLAIN, 14), Color.WHITE, Map.of(),
				com.questcape.guide.SkillMentionInterpreter.interpret("Herblore or Runecraft. Use Herblore again."));
			String original = text.getText();
			assertFalse("Pending mentions must never show the generic training icon",
				StyleConstants.getIcon(text.getStyledDocument().getCharacterElement(0).getAttributes()) instanceof StepIcons);
			text.updateIcons(Map.of(Skill.HERBLORE, herblore, Skill.RUNECRAFT, craft));
			assertEquals(original, text.getText());
			for (int offset = original.indexOf('\ufffc'); offset >= 0; offset = original.indexOf('\ufffc', offset + 1))
			{
				assertSame(original.startsWith("\ufffc Runecraft", offset) ? craft : herblore,
					StyleConstants.getIcon(text.getStyledDocument().getCharacterElement(offset).getAttributes()));
			}
			text.setSize(250, 200);
			int wide = text.getPreferredSize().height;
			text.setSize(100, 200);
			assertTrue(text.getPreferredSize().height > wide);
		});
	}
}
