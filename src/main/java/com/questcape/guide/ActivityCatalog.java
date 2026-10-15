package com.questcape.guide;

import com.questcape.progress.activity.*;
import java.util.*;
import java.util.regex.*;
import static com.questcape.guide.SourceTextNormalizer.normalize;

/** Source-side reviewed action aliases. Detectors never inspect Wiki text. */
public final class ActivityCatalog
{
	private static final Pattern DIARY_ACTION = Pattern.compile(
		"^(?:(?:complete|claim|collect|receive) (?:the )?)?(easy|medium|hard|elite) ([a-z &]+) diary(?: reward)?\\.?$");
	private static final String BALLOON_PREFIX = "unlock: balloon transport system to ";

	public static ActivityKey diary(String source)
	{
		String text = normalize(source);
		Matcher match = DIARY_ACTION.matcher(text);
		if (!match.matches())
		{
			return null;
		}
		DiaryKey.Tier tier = Arrays.stream(DiaryKey.Tier.values()).filter(t -> t.getSource().equals(match.group(1)))
			.findFirst().orElse(null);
		String region = match.group(2);
		DiaryKey.Region area = Arrays.stream(DiaryKey.Region.values()).filter(r -> region.equals(r.getSource())
			|| region.equals("kourend & kebos") && r == DiaryKey.Region.KOUREND
			|| region.equals("lumbridge & draynor") && r == DiaryKey.Region.LUMBRIDGE
			|| region.equals("western provinces") && r == DiaryKey.Region.WESTERN).findFirst().orElse(null);
		if (area == null || tier == null)
		{
			return null;
		}
		boolean reward = text.startsWith("claim ") || text.startsWith("collect ") || text.startsWith("receive ");
		return ActivityKey.diary(new DiaryKey(area, tier, reward ? DiaryKey.Action.REWARD : DiaryKey.Action.TASKS));
	}

	public static ActivityKey unlock(String source)
	{
		String text = normalize(source);
		if (text.startsWith(BALLOON_PREFIX))
		{
			for (BalloonDestination destination : BalloonDestination.values())
			{
				if (text.substring(BALLOON_PREFIX.length()).equals(destination.getSource()))
				{
					return ActivityKey.balloon(destination);
				}
			}
		}
		if (text.equals("unlock: stronghold of security"))
		{
			return ActivityKey.special(ActivityKey.Special.STRONGHOLD_COINS);
		}
		if (text.equals("unlock: museum camp"))
		{
			return ActivityKey.special(ActivityKey.Special.MUSEUM_CAMP_REWARD);
		}
		return null;
	}

	public static ActivityKey handIn(String source)
	{
		return normalize(source).startsWith("hand in the diary of herbi flax to the apothecary")
			? ActivityKey.special(ActivityKey.Special.HERBI_FLAX)
			: null;
	}

	public static boolean kudos(String source)
	{
		String value = normalize(source);
		return value.equals("unlock: varrock museum kudos") || value.equals("unlock: more varrock museum kudos");
	}

	public static ActivityKey rowId(String source, boolean reward)
	{
		String id = normalize(source);
		for (BalloonDestination destination : BalloonDestination.values())
		{
			if (id.equals(destination.getSource() + " balloon"))
			{
				return ActivityKey.balloon(destination);
			}
		}
		int split = id.indexOf('#');
		if (split > 0)
		{
			return diary((reward ? "claim " : "") + id.substring(split + 1) + " " + id.substring(0, split));
		}
		return null;
	}

	private ActivityCatalog()
	{
	}
}
