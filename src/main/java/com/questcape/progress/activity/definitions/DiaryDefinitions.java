package com.questcape.progress.activity.definitions;

import java.util.*;
import lombok.Value;
import net.runelite.api.gameval.VarbitID;

/** Verified mappings from docs/activity-detection.md; data is shared by recognizers and detectors. */
public final class DiaryDefinitions
{
	@Value
	public static class Definition
	{
		DiaryKey.Region region;
		DiaryKey.Tier tier;
		int tasks;
		int reward;
		int expected;

		public DiaryKey key(DiaryKey.Action action)
		{
			return new DiaryKey(region, tier, action);
		}
	}

	public static final List<Definition> ALL = Collections.unmodifiableList(Arrays.asList(
		new Definition(DiaryKey.Region.ARDOUGNE, DiaryKey.Tier.EASY, VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE,
			VarbitID.ARDOUGNE_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.ARDOUGNE, DiaryKey.Tier.MEDIUM, VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE,
			VarbitID.ARDOUGNE_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.ARDOUGNE, DiaryKey.Tier.HARD, VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE,
			VarbitID.ARDOUGNE_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.ARDOUGNE, DiaryKey.Tier.ELITE, VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE,
			VarbitID.ARDOUGNE_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.DESERT, DiaryKey.Tier.EASY, VarbitID.DESERT_DIARY_EASY_COMPLETE,
			VarbitID.DESERT_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.DESERT, DiaryKey.Tier.MEDIUM, VarbitID.DESERT_DIARY_MEDIUM_COMPLETE,
			VarbitID.DESERT_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.DESERT, DiaryKey.Tier.HARD, VarbitID.DESERT_DIARY_HARD_COMPLETE,
			VarbitID.DESERT_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.DESERT, DiaryKey.Tier.ELITE, VarbitID.DESERT_DIARY_ELITE_COMPLETE,
			VarbitID.DESERT_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FALADOR, DiaryKey.Tier.EASY, VarbitID.FALADOR_DIARY_EASY_COMPLETE,
			VarbitID.FALADOR_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FALADOR, DiaryKey.Tier.MEDIUM, VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE,
			VarbitID.FALADOR_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FALADOR, DiaryKey.Tier.HARD, VarbitID.FALADOR_DIARY_HARD_COMPLETE,
			VarbitID.FALADOR_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FALADOR, DiaryKey.Tier.ELITE, VarbitID.FALADOR_DIARY_ELITE_COMPLETE,
			VarbitID.FALADOR_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FREMENNIK, DiaryKey.Tier.EASY, VarbitID.FREMENNIK_DIARY_EASY_COMPLETE,
			VarbitID.FREMENNIK_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FREMENNIK, DiaryKey.Tier.MEDIUM, VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE,
			VarbitID.FREMENNIK_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FREMENNIK, DiaryKey.Tier.HARD, VarbitID.FREMENNIK_DIARY_HARD_COMPLETE,
			VarbitID.FREMENNIK_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.FREMENNIK, DiaryKey.Tier.ELITE, VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE,
			VarbitID.FREMENNIK_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KANDARIN, DiaryKey.Tier.EASY, VarbitID.KANDARIN_DIARY_EASY_COMPLETE,
			VarbitID.KANDARIN_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KANDARIN, DiaryKey.Tier.MEDIUM, VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE,
			VarbitID.KANDARIN_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KANDARIN, DiaryKey.Tier.HARD, VarbitID.KANDARIN_DIARY_HARD_COMPLETE,
			VarbitID.KANDARIN_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KANDARIN, DiaryKey.Tier.ELITE, VarbitID.KANDARIN_DIARY_ELITE_COMPLETE,
			VarbitID.KANDARIN_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KARAMJA, DiaryKey.Tier.EASY, VarbitID.ATJUN_EASY_DONE,
			VarbitID.ATJUN_EASY_REWARD, GamePredicates.KARAMJA_TASKS_COMPLETE),
		new Definition(DiaryKey.Region.KARAMJA, DiaryKey.Tier.MEDIUM, VarbitID.ATJUN_MED_DONE,
			VarbitID.ATJUN_MED_REWARD, GamePredicates.KARAMJA_TASKS_COMPLETE),
		new Definition(DiaryKey.Region.KARAMJA, DiaryKey.Tier.HARD, VarbitID.ATJUN_HARD_DONE,
			VarbitID.ATJUN_HARD_REWARD, GamePredicates.KARAMJA_TASKS_COMPLETE),
		new Definition(DiaryKey.Region.KARAMJA, DiaryKey.Tier.ELITE, VarbitID.KARAMJA_DIARY_ELITE_COMPLETE,
			VarbitID.KARAMJA_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KOUREND, DiaryKey.Tier.EASY, VarbitID.KOUREND_DIARY_EASY_COMPLETE,
			VarbitID.KOUREND_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KOUREND, DiaryKey.Tier.MEDIUM, VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE,
			VarbitID.KOUREND_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KOUREND, DiaryKey.Tier.HARD, VarbitID.KOUREND_DIARY_HARD_COMPLETE,
			VarbitID.KOUREND_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.KOUREND, DiaryKey.Tier.ELITE, VarbitID.KOUREND_DIARY_ELITE_COMPLETE,
			VarbitID.KOUREND_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.LUMBRIDGE, DiaryKey.Tier.EASY, VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE,
			VarbitID.LUMBRIDGE_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.LUMBRIDGE, DiaryKey.Tier.MEDIUM, VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE,
			VarbitID.LUMBRIDGE_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.LUMBRIDGE, DiaryKey.Tier.HARD, VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE,
			VarbitID.LUMBRIDGE_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.LUMBRIDGE, DiaryKey.Tier.ELITE, VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE,
			VarbitID.LUMBRIDGE_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.MORYTANIA, DiaryKey.Tier.EASY, VarbitID.MORYTANIA_DIARY_EASY_COMPLETE,
			VarbitID.MORYTANIA_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.MORYTANIA, DiaryKey.Tier.MEDIUM, VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE,
			VarbitID.MORYTANIA_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.MORYTANIA, DiaryKey.Tier.HARD, VarbitID.MORYTANIA_DIARY_HARD_COMPLETE,
			VarbitID.MORYTANIA_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.MORYTANIA, DiaryKey.Tier.ELITE, VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE,
			VarbitID.MORYTANIA_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.VARROCK, DiaryKey.Tier.EASY, VarbitID.VARROCK_DIARY_EASY_COMPLETE,
			VarbitID.VARROCK_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.VARROCK, DiaryKey.Tier.MEDIUM, VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE,
			VarbitID.VARROCK_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.VARROCK, DiaryKey.Tier.HARD, VarbitID.VARROCK_DIARY_HARD_COMPLETE,
			VarbitID.VARROCK_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.VARROCK, DiaryKey.Tier.ELITE, VarbitID.VARROCK_DIARY_ELITE_COMPLETE,
			VarbitID.VARROCK_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WESTERN, DiaryKey.Tier.EASY, VarbitID.WESTERN_DIARY_EASY_COMPLETE,
			VarbitID.WESTERN_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WESTERN, DiaryKey.Tier.MEDIUM, VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE,
			VarbitID.WESTERN_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WESTERN, DiaryKey.Tier.HARD, VarbitID.WESTERN_DIARY_HARD_COMPLETE,
			VarbitID.WESTERN_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WESTERN, DiaryKey.Tier.ELITE, VarbitID.WESTERN_DIARY_ELITE_COMPLETE,
			VarbitID.WESTERN_ELITE_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WILDERNESS, DiaryKey.Tier.EASY, VarbitID.WILDERNESS_DIARY_EASY_COMPLETE,
			VarbitID.WILDERNESS_EASY_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WILDERNESS, DiaryKey.Tier.MEDIUM, VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE,
			VarbitID.WILDERNESS_MEDIUM_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WILDERNESS, DiaryKey.Tier.HARD, VarbitID.WILDERNESS_DIARY_HARD_COMPLETE,
			VarbitID.WILDERNESS_HARD_REWARD, GamePredicates.SET),
		new Definition(DiaryKey.Region.WILDERNESS, DiaryKey.Tier.ELITE, VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE,
			VarbitID.WILDERNESS_ELITE_REWARD, GamePredicates.SET)));

	private DiaryDefinitions()
	{
	}
}
