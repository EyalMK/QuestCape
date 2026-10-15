package com.questcape.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.util.ImageUtil;

/** Worker-prepared local assets. No account facts, scheduling, remote images or widget mutation. */
public final class SidebarAssets
{
	public static final int NAVIGATION_SIZE = 32, HEADER_SIZE = 22, SKILL_SIZE = 16, PROJECTION_SIZE = 18;
	private final BufferedImage navigation, header, github;
	private final Map<Skill, BufferedImage> skills, projections;

	private SidebarAssets(BufferedImage navigation, BufferedImage header, BufferedImage github,
		Map<Skill, BufferedImage> skills, Map<Skill, BufferedImage> projections)
	{
		this.navigation = navigation;
		this.header = header;
		this.github = github;
		this.skills = Map.copyOf(skills);
		this.projections = Map.copyOf(projections);
	}

	public static SidebarAssets prepare(SkillIconManager manager) throws IOException
	{
		BufferedImage navigation = SidebarImages.load("/quest-route-icon.png"),
			github = SidebarImages.load("/github-mark.png");
		Map<Skill, BufferedImage> skills = new EnumMap<>(Skill.class), projections = new EnumMap<>(Skill.class);
		for (Skill skill : Skill.values())
		{
			BufferedImage image = manager.getSkillImage(skill, false);
			if (image != null)
			{
				skills.put(skill, ImageUtil.resizeImage(image, SKILL_SIZE, SKILL_SIZE));
				projections.put(skill, ImageUtil.resizeImage(image, PROJECTION_SIZE, PROJECTION_SIZE));
			}
		}
		return new SidebarAssets(navigation, ImageUtil.resizeImage(navigation, HEADER_SIZE, HEADER_SIZE), github,
			skills, projections);
	}

	public BufferedImage navigation()
	{
		return navigation;
	}

	public void apply(GuidePanel panel)
	{
		panel.setHeaderIcons(header, github);
		panel.setSkillIcons(skills, projections);
	}
}
