# Automatic activity detection in 1.0.0

All supported achievement diary and miniquest rows use local game state. They have no manual checkboxes. Unsupported future rows remain unknown. Existing unrelated unlock and hand-in checks remain manual, including the diary of Herbi Flax. Old diary/miniquest manual keys are retained in account files but ignored by completion evaluation. Cache schema and existing account/action keys stay unchanged; cached routes are reclassified on loading.

## Diaries

Canonical keys are `DIARY:REGION_TIER:complete` and `DIARY:REGION_TIER:reward`. Each fact is true only when its named `net.runelite.api.gameval.VarbitID` value equals 1. Completing tasks and collecting rewards are separate facts. Whole-tier instructions select `complete`; claim/collect/receive instructions select `reward`. Individual-task and ambiguous region/tier instructions remain unknown.

All four tiers (EASY, MEDIUM, HARD, ELITE) are mapped for ARDOUGNE, DESERT, FALADOR, FREMENNIK, KANDARIN, KARAMJA, KOUREND, LUMBRIDGE, MORYTANIA, VARROCK, WESTERN, WILDERNESS. Kourend & Kebos and Lumbridge & Draynor use KOUREND and LUMBRIDGE respectively. Western Provinces uses WESTERN.

| Region/tier | Task predicate constant | Reward predicate constant |
| --- | --- | --- |
| All except Karamja easy/medium/hard | `REGION_DIARY_TIER_COMPLETE` | `REGION_TIER_REWARD` |
| Karamja easy | `ATJUN_EASY_DONE` | `ATJUN_EASY_REWARD` |
| Karamja medium | `ATJUN_MED_DONE` | `ATJUN_MED_REWARD` |
| Karamja hard | `ATJUN_HARD_DONE` | `ATJUN_HARD_REWARD` |

The code lists all 48 pairs explicitly, with no numeric IDs. These follow RuneLite's [achievement diary plugin](https://github.com/runelite/runelite/tree/runelite-parent-1.13.1/runelite-client/src/main/java/net/runelite/client/plugins/achievementdiary) and [gameval constants](https://github.com/runelite/runelite/blob/runelite-parent-1.13.1/runelite-api/src/main/java/net/runelite/api/gameval/VarbitID.java), verified against the resolved 1.13.1 API. No skill threshold, later tier, quest prerequisite, or guide projection substitutes for a diary fact.

## Miniquests

API-covered rows use canonical `Quest` enum identities and `Quest.getState(Client)`. Names are normalized with explicit aliases; stage prefixes and the guide's miniquest suffix are removed only for lookup. An exact title lookup runs first to preserve names with punctuation. A start row completes at IN_PROGRESS or COMPLETE; a finish row requires COMPLETE.

Current route revision 15358869 contains 24 diary rows and 14 miniquest rows. The API-covered miniquest mappings are:

| Guide title (without miniquest label) | `Quest` identity |
| --- | --- |
| Daddy's Home | `DADDYS_HOME` |
| Alfred Grimhand's Barcrawl | `ALFRED_GRIMHANDS_BARCRAWL` |
| Enter the Abyss | `ENTER_THE_ABYSS` |
| Skippy and the Mogres | `SKIPPY_AND_THE_MOGRES` |
| Lair of Tarn Razorlor | `LAIR_OF_TARN_RAZORLOR` |
| Curse of the Empty Lord | `CURSE_OF_THE_EMPTY_LORD` |
| The General's Shadow | `THE_GENERALS_SHADOW` |
| His Faithful Servants | `HIS_FAITHFUL_SERVANTS` |
| In Search of Knowledge (start and finish) | `IN_SEARCH_OF_KNOWLEDGE` |
| Hopespear's Will | `HOPESPEARS_WILL` |
| Into the Tombs | `INTO_THE_TOMBS` |

Two guide activities are absent from the `Quest` enum and use these explicit additional predicates:

| Identity | Incomplete | In progress | Complete |
| --- | --- | --- | --- |
| `NATURAL_HISTORY_QUIZ` | Neither start nor reward flag set | `VM_NATHIS_AGREED == 1`, reward unset | `VM_NATHIS_REWARD_GIVEN == 1` |
| `KNIGHT_WAVES_TRAINING_GROUNDS` | `KR_KNIGHTWAVES_STATE <= 0` | State 1–7 | State >= 8 |

The quiz requires collecting the reward; `VM_NATHIS_DISPLAY_ALLDONE` alone does not finish it. Knight Waves follows the eight-wave completion threshold in Quest Helper's [quest definitions at 633ab56e](https://github.com/Zoinkwiz/quest-helper/blob/633ab56e2eb3eb363f21da3fd75f6f2bc0fa073a/src/main/java/com/questhelper/questinfo/QuestHelperQuest.java); QuestCape does not depend on or control Quest Helper. Verify these predicates against the in-game journal/reward state during acceptance.

Reads occur on ClientThread after the first logged-in tick and established RS profile/hash/mode readiness. Immutable observations cross to the serial worker. Stat/varbit events coalesce; idle ticks perform no scans. Logout/hops/reconnects reject stale session work. Explicit Sync reads locally and forces a cache recovery attempt, even for unchanged values. No character data is sent over HTTP.

The captured route is revision 15336101. The public contract check also requires every diary/miniquest in the fetched current guide to have an automatic identity. Run `./gradlew verifyRemoteContracts` to recheck current coverage; this does not verify in-game state.

## Unlocks and hand-in (2.0.0)

Only `Unlock: Varrock Museum Kudos` and `Unlock: More Varrock Museum Kudos` retain manual checks, as explicitly requested. All other retired checks are inert. Unsupported future unlocks/hand-ins stay unknown with no checkbox. Cached rows are reclassified offline; Kudos action keys do not change.

| Guide action | Authoritative completion predicate |
| --- | --- |
| Balloon to Crafting Guild | `ZEP_MULTI_CRAFT == 1` |
| Balloon to Varrock | `ZEP_MULTI_VARR == 1` |
| Balloon to Castle Wars | `ZEP_MULTI_CAST == 1` |
| Balloon to Grand Tree | `ZEP_MULTI_GNO == 1` |
| Stronghold of Security | `SOS_EMOTE_FLAP == 1 && SOS_EMOTE_DOH == 1 && SOS_EMOTE_IDEA == 1` |
| Museum Camp | `FOSSIL_TASKREWARDS_CAMP == 1` |
| Herbi Flax diary hand-in | `(MORTTONMULTI & (1 << 30)) != 0` |

The four balloon flags and equality-to-one completion values follow Quest Helper's [QuestVarbits](https://github.com/Zoinkwiz/quest-helper/blob/633ab56e2eb3eb363f21da3fd75f6f2bc0fa073a/src/main/java/com/questhelper/questinfo/QuestVarbits.java) and [QuestHelperQuest](https://github.com/Zoinkwiz/quest-helper/blob/633ab56e2eb3eb363f21da3fd75f6f2bc0fa073a/src/main/java/com/questhelper/questinfo/QuestHelperQuest.java) definitions. Each destination has a separate stable identity. No flight puzzle, widget, inventory or prerequisite inference is used.

The guide asks for 10,000 Stronghold coins from the first three floors. Quest Helper's [Stronghold helper](https://github.com/Zoinkwiz/quest-helper/blob/633ab56e2eb3eb363f21da3fd75f6f2bc0fa073a/src/main/java/com/questhelper/helpers/mischelpers/strongholdofsecurity/StrongholdOfSecurity.java) associates these flags with each chest's coin/emote reward. The fourth-floor Stamp/boots flag is deliberately not required.

Museum Camp uses Peter's camp reward, distinct from camp access, individual improvements and other island task rewards. This predicate follows Task Checker's [payment/reward definitions](https://github.com/bopsec/task-checker-plugin/blob/ddb4e230256ed459459f120d0f7f25b6f22fce51/src/main/java/com/andmcadams/taskchecker/tasklist/PaymentsAndInstantExperienceTaskList.java) and its equality-to-one [switchVar implementation](https://github.com/bopsec/task-checker-plugin/blob/ddb4e230256ed459459f120d0f7f25b6f22fce51/src/main/java/com/andmcadams/taskchecker/Task.java). Herbi Flax uses its [redeemable item definition](https://github.com/bopsec/task-checker-plugin/blob/ddb4e230256ed459459f120d0f7f25b6f22fce51/src/main/java/com/andmcadams/taskchecker/tasklist/RedeemableQuestItemsTaskList.java): named `VarPlayerID.MORTTONMULTI`, bit 30. No dedicated hand-in gameval varbit exists in RuneLite 1.13.1, so the documented bit position is named in code. Other bitmap bits, quest completion and Herblore levels do not complete the hand-in.

The captured/current guide has seven such automatic actions and two manual Kudos actions. Fresh parsing preserves all destination text. For an old cached title ending in “to”, recovery requires exactly one supported destination among retained validated Wiki links; ambiguous or absent evidence stays unknown. In-game acceptance of each predicate remains required.
