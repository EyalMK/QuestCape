package com.questcape.ui;

import com.questcape.guide.GuideRow;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;
import net.runelite.api.Skill;

/** Prepared local images and active view bindings; all mutations are EDT-owned. */
final class GuideAssets
{
	final Map<GuideRow.Kind, Icon> steps = new EnumMap<>(GuideRow.Kind.class);
	final Map<Skill, Icon> skills = new EnumMap<>(Skill.class), projections = new EnumMap<>(Skill.class);
	final Map<Skill, List<JLabel>> projectedLabels = new EnumMap<>(Skill.class);
	final List<SkillText> texts = new ArrayList<>();

	void clearBindings()
	{
		texts.clear();
		projectedLabels.clear();
	}

	void update(Map<Skill, BufferedImage> images, Map<Skill, BufferedImage> larger)
	{
		images.forEach((skill, image) -> skills.put(skill, new ImageIcon(image)));
		larger.forEach((skill, image) -> projections.put(skill, new ImageIcon(image)));
		projectedLabels.forEach((skill, labels) -> labels.forEach(label -> label.setIcon(projections.get(skill))));
		texts.forEach(text -> text.updateIcons(skills));
	}
}
