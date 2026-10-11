package com.questcape.progress;

import java.util.*;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;

/** Explicit local predicates; see docs/activity-detection.md for source and state semantics. */
public final class ActivityProgress
{
	// MORTTONMULTI is a bitmap; no individual gameval varbit exists for this hand-in.
	private static final int HERBI_FLAX_HANDED_IN_BIT = 30;
	private static final Map<String, Integer> BALLOONS = Map.of(
		"crafting guild", VarbitID.ZEP_MULTI_CRAFT, "varrock", VarbitID.ZEP_MULTI_VARR,
		"castle wars", VarbitID.ZEP_MULTI_CAST, "grand tree", VarbitID.ZEP_MULTI_GNO);
	private static final Map<String, int[]> DIARIES = new LinkedHashMap<>();
	static
	{
		add("ARDOUGNE_EASY", VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE, VarbitID.ARDOUGNE_EASY_REWARD);
		add("ARDOUGNE_MEDIUM", VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE, VarbitID.ARDOUGNE_MEDIUM_REWARD);
		add("ARDOUGNE_HARD", VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE, VarbitID.ARDOUGNE_HARD_REWARD);
		add("ARDOUGNE_ELITE", VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE, VarbitID.ARDOUGNE_ELITE_REWARD);
		add("DESERT_EASY", VarbitID.DESERT_DIARY_EASY_COMPLETE, VarbitID.DESERT_EASY_REWARD);
		add("DESERT_MEDIUM", VarbitID.DESERT_DIARY_MEDIUM_COMPLETE, VarbitID.DESERT_MEDIUM_REWARD);
		add("DESERT_HARD", VarbitID.DESERT_DIARY_HARD_COMPLETE, VarbitID.DESERT_HARD_REWARD);
		add("DESERT_ELITE", VarbitID.DESERT_DIARY_ELITE_COMPLETE, VarbitID.DESERT_ELITE_REWARD);
		add("FALADOR_EASY", VarbitID.FALADOR_DIARY_EASY_COMPLETE, VarbitID.FALADOR_EASY_REWARD);
		add("FALADOR_MEDIUM", VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE, VarbitID.FALADOR_MEDIUM_REWARD);
		add("FALADOR_HARD", VarbitID.FALADOR_DIARY_HARD_COMPLETE, VarbitID.FALADOR_HARD_REWARD);
		add("FALADOR_ELITE", VarbitID.FALADOR_DIARY_ELITE_COMPLETE, VarbitID.FALADOR_ELITE_REWARD);
		add("FREMENNIK_EASY", VarbitID.FREMENNIK_DIARY_EASY_COMPLETE, VarbitID.FREMENNIK_EASY_REWARD);
		add("FREMENNIK_MEDIUM", VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE, VarbitID.FREMENNIK_MEDIUM_REWARD);
		add("FREMENNIK_HARD", VarbitID.FREMENNIK_DIARY_HARD_COMPLETE, VarbitID.FREMENNIK_HARD_REWARD);
		add("FREMENNIK_ELITE", VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE, VarbitID.FREMENNIK_ELITE_REWARD);
		add("KANDARIN_EASY", VarbitID.KANDARIN_DIARY_EASY_COMPLETE, VarbitID.KANDARIN_EASY_REWARD);
		add("KANDARIN_MEDIUM", VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE, VarbitID.KANDARIN_MEDIUM_REWARD);
		add("KANDARIN_HARD", VarbitID.KANDARIN_DIARY_HARD_COMPLETE, VarbitID.KANDARIN_HARD_REWARD);
		add("KANDARIN_ELITE", VarbitID.KANDARIN_DIARY_ELITE_COMPLETE, VarbitID.KANDARIN_ELITE_REWARD);
		add("KARAMJA_EASY", VarbitID.ATJUN_EASY_DONE, VarbitID.ATJUN_EASY_REWARD);
		add("KARAMJA_MEDIUM", VarbitID.ATJUN_MED_DONE, VarbitID.ATJUN_MED_REWARD);
		add("KARAMJA_HARD", VarbitID.ATJUN_HARD_DONE, VarbitID.ATJUN_HARD_REWARD);
		add("KARAMJA_ELITE", VarbitID.KARAMJA_DIARY_ELITE_COMPLETE, VarbitID.KARAMJA_ELITE_REWARD);
		add("KOUREND_EASY", VarbitID.KOUREND_DIARY_EASY_COMPLETE, VarbitID.KOUREND_EASY_REWARD);
		add("KOUREND_MEDIUM", VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE, VarbitID.KOUREND_MEDIUM_REWARD);
		add("KOUREND_HARD", VarbitID.KOUREND_DIARY_HARD_COMPLETE, VarbitID.KOUREND_HARD_REWARD);
		add("KOUREND_ELITE", VarbitID.KOUREND_DIARY_ELITE_COMPLETE, VarbitID.KOUREND_ELITE_REWARD);
		add("LUMBRIDGE_EASY", VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE, VarbitID.LUMBRIDGE_EASY_REWARD);
		add("LUMBRIDGE_MEDIUM", VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE, VarbitID.LUMBRIDGE_MEDIUM_REWARD);
		add("LUMBRIDGE_HARD", VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE, VarbitID.LUMBRIDGE_HARD_REWARD);
		add("LUMBRIDGE_ELITE", VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE, VarbitID.LUMBRIDGE_ELITE_REWARD);
		add("MORYTANIA_EASY", VarbitID.MORYTANIA_DIARY_EASY_COMPLETE, VarbitID.MORYTANIA_EASY_REWARD);
		add("MORYTANIA_MEDIUM", VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE, VarbitID.MORYTANIA_MEDIUM_REWARD);
		add("MORYTANIA_HARD", VarbitID.MORYTANIA_DIARY_HARD_COMPLETE, VarbitID.MORYTANIA_HARD_REWARD);
		add("MORYTANIA_ELITE", VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE, VarbitID.MORYTANIA_ELITE_REWARD);
		add("VARROCK_EASY", VarbitID.VARROCK_DIARY_EASY_COMPLETE, VarbitID.VARROCK_EASY_REWARD);
		add("VARROCK_MEDIUM", VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE, VarbitID.VARROCK_MEDIUM_REWARD);
		add("VARROCK_HARD", VarbitID.VARROCK_DIARY_HARD_COMPLETE, VarbitID.VARROCK_HARD_REWARD);
		add("VARROCK_ELITE", VarbitID.VARROCK_DIARY_ELITE_COMPLETE, VarbitID.VARROCK_ELITE_REWARD);
		add("WESTERN_EASY", VarbitID.WESTERN_DIARY_EASY_COMPLETE, VarbitID.WESTERN_EASY_REWARD);
		add("WESTERN_MEDIUM", VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE, VarbitID.WESTERN_MEDIUM_REWARD);
		add("WESTERN_HARD", VarbitID.WESTERN_DIARY_HARD_COMPLETE, VarbitID.WESTERN_HARD_REWARD);
		add("WESTERN_ELITE", VarbitID.WESTERN_DIARY_ELITE_COMPLETE, VarbitID.WESTERN_ELITE_REWARD);
		add("WILDERNESS_EASY", VarbitID.WILDERNESS_DIARY_EASY_COMPLETE, VarbitID.WILDERNESS_EASY_REWARD);
		add("WILDERNESS_MEDIUM", VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE, VarbitID.WILDERNESS_MEDIUM_REWARD);
		add("WILDERNESS_HARD", VarbitID.WILDERNESS_DIARY_HARD_COMPLETE, VarbitID.WILDERNESS_HARD_REWARD);
		add("WILDERNESS_ELITE", VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE, VarbitID.WILDERNESS_ELITE_REWARD);
	}

	private ActivityProgress()
	{
	}

	private static void add(String identity, int complete, int reward)
	{
		DIARIES.put(identity, new int[] {complete, reward});
	}

	/** Only whole-tier instructions are mapped; individual tasks must not inherit tier completion. */
	public static String diary(String title)
	{
		String text = Identities.normalize(title);
		if (!text.contains("diary") || text.matches(".*\\btasks?\\b.*"))
		{
			return null;
		}
		String match = null;
		for (String identity : DIARIES.keySet())
		{
			String[] parts = identity.toLowerCase(Locale.ROOT).split("_");
			if (text.matches(".*\\b" + parts[0] + "\\b.*") && text.matches(".*\\b" + parts[1] + "\\b.*"))
			{
				if (match != null)
				{
					return null;
				}
				match = "DIARY:" + identity;
			}
		}
		boolean reward = text.startsWith("claim ") || text.startsWith("collect ") || text.startsWith("receive ");
		return match == null ? null : match + (reward ? ":reward" : ":complete");
	}

	public static Map<String, Boolean> readDiaries(Client client)
	{
		Map<String, Boolean> facts = new HashMap<>();
		DIARIES.forEach((identity, vars) ->
		{
			facts.put("DIARY:" + identity + ":complete", client.getVarbitValue(vars[0]) == 1);
			facts.put("DIARY:" + identity + ":reward", client.getVarbitValue(vars[1]) == 1);
		});
		return facts;
	}

	public static String unlock(String title)
	{
		String text = Identities.normalize(title);
		String prefix = "unlock: balloon transport system to ";
		if (text.startsWith(prefix) && BALLOONS.containsKey(text.substring(prefix.length())))
		{
			return balloonIdentity(text.substring(prefix.length()));
		}
		if (text.equals("unlock: stronghold of security"))
		{
			return "UNLOCK:STRONGHOLD_COINS";
		}
		if (text.equals("unlock: museum camp"))
		{
			return "UNLOCK:MUSEUM_CAMP_REWARD";
		}
		return null;
	}

	public static String handIn(String title)
	{
		String text = Identities.normalize(title);
		return text.startsWith("hand in the diary of herbi flax to the apothecary")
			? "HAND_IN:HERBI_FLAX" : null;
	}

	public static boolean isKudos(String title)
	{
		String text = Identities.normalize(title);
		return text.equals("unlock: varrock museum kudos") || text.equals("unlock: more varrock museum kudos");
	}

	private static String balloonIdentity(String destination)
	{
		return "UNLOCK:BALLOON:" + destination.toUpperCase(Locale.ROOT).replace(' ', '_');
	}

	public static Map<String, Boolean> readActivities(Client client)
	{
		Map<String, Boolean> facts = readDiaries(client);
		BALLOONS.forEach((destination, varbit) ->
			facts.put(balloonIdentity(destination), client.getVarbitValue(varbit) == 1));
		// This guide asks for the 10,000 coins on floors 1–3, not the fourth-floor boots.
		facts.put("UNLOCK:STRONGHOLD_COINS", client.getVarbitValue(VarbitID.SOS_EMOTE_FLAP) == 1
			&& client.getVarbitValue(VarbitID.SOS_EMOTE_DOH) == 1
			&& client.getVarbitValue(VarbitID.SOS_EMOTE_IDEA) == 1);
		// Peter's camp reward is distinct from access, individual improvements, and other island tasks.
		facts.put("UNLOCK:MUSEUM_CAMP_REWARD", client.getVarbitValue(VarbitID.FOSSIL_TASKREWARDS_CAMP) == 1);
		facts.put("HAND_IN:HERBI_FLAX", (client.getVarpValue(VarPlayerID.MORTTONMULTI)
			& (1 << HERBI_FLAX_HANDED_IN_BIT)) != 0);
		return facts;
	}

	public static void readMiniquests(Client client, Map<String, AccountProgress.QuestStatus> quests)
	{
		quests.put("NATURAL_HISTORY_QUIZ", client.getVarbitValue(VarbitID.VM_NATHIS_REWARD_GIVEN) == 1
			? AccountProgress.QuestStatus.COMPLETE
			: client.getVarbitValue(VarbitID.VM_NATHIS_AGREED) == 1
				? AccountProgress.QuestStatus.IN_PROGRESS : AccountProgress.QuestStatus.INCOMPLETE);
		int waves = client.getVarbitValue(VarbitID.KR_KNIGHTWAVES_STATE);
		quests.put("KNIGHT_WAVES_TRAINING_GROUNDS", waves >= 8 ? AccountProgress.QuestStatus.COMPLETE
			: waves > 0 ? AccountProgress.QuestStatus.IN_PROGRESS : AccountProgress.QuestStatus.INCOMPLETE);
	}
}
