package com.questcape.guide;

import com.questcape.progress.Identities;
import java.util.Arrays;

/** Source header aliases are interpreted once, independently of display copy. */
public enum GuideFieldRole
{
	TITLE("Quest/Activity"), QUICK_GUIDE("Quick Guide"), PROJECTIONS("New levels after quest", "New levels"),
	QUEST_POINTS("Quest points"), GUIDE_QP("Total QP"), ADVICE("Additional info", "Additional information"),
	LOCATION("Location"), UNKNOWN();

	private final String[] aliases;

	GuideFieldRole(String... aliases)
	{
		this.aliases = aliases;
	}

	public static GuideFieldRole of(String header)
	{
		for (GuideFieldRole role : values())
		{
			if (Arrays.stream(role.aliases)
				.anyMatch(alias -> Identities.normalize(alias).equals(Identities.normalize(header))))
			{
				return role;
			}
		}
		return UNKNOWN;
	}
}
