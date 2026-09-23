# Capability scenario audit — 2026-09-23

The user accepted local character sync at `1e8ffb8` and the panel/classification and cross-plugin removal changes at `84330bc` on 2026-09-23. The [in-game checklist](verification.md) passed, and the user authorized the source merge and Hub re-enable submission.

| Capability | Scenarios and evidence |
| --- | --- |
| Automatic local sync | ProgressTest and PluginLifecycleTest: established profile/hash/mode, first tick, quest/skill changes, idle ticks, missing observations, and real rather than boosted skills. |
| Explicit Sync | Fresh timestamp and observation, manual checks retained, readiness required, no character HTTP request, prior-session callbacks discarded. |
| Account isolation | Profile/mode scopes, different display names, account switch, logout during disk writes, restart persistence, and reversible manual checks. |
| Route loading | GuideParserTest and GuideRepositoryTest: ordered mixed table content, normalized/reordered headers, bounded valid responses, duplicate refreshes, stale/offline cache, and cancellation. |
| Category recognition | Training wording and combat targets; optional recommendation exclusion; explicit miniquest labels independent of Quest API coverage; unknown future actions remain visible. |
| Comments and milestones | Structural/language classification, renamed/moved notes, no numbered cards or completion contribution, reclassification of saved content without changing established manual keys. |
| Panel presentation | GuidePanelTest and docs/ui: type border colors/icons, gold in-progress state, readable typography, comments between dividers, and fixed-width layout. |
| Panel actions | Title/background/metadata and keyboard detail toggles, independent wiki/training links and manual checks, dismissible sync notices; no cross-plugin controls. |
| Navigation | Top/bottom and Next step direction, exclusion of comments, current-quest priority, and scroll preservation across login and repeated live updates. |
| Training links | IntegrationTest: aliases, known destinations, all-guides fallback, redirect allowlist, browser recovery, and no automatic progress change. |
| Shutdown | Queued sync invalidation, request cancellation, executor shutdown without waiting, and removal of QuestCape’s own navigation button. |
| Packaging | PackagingTest: Java 11 classes in the plugin package, plugin metadata, valid small PNG, classpath resources, licenses, and exclusion of fixtures. |

The prohibited cross-plugin UI implementation and its unused resume/dependency scaffolding have been removed, including their obsolete tests. No supported cross-plugin feature is included in this revision. Stored values for retired controls remain untouched.

All 36 current tests pass, and the user separately confirmed in-game acceptance. The user approved source merge and Hub correction submission. RuneLite maintainers retain final approval of the Hub update.
