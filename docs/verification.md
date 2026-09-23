# Verification evidence — panel changes and review corrections, 2026-09-23

## Build and source checks

`./gradlew.bat build --no-daemon` passed with Temurin 11.0.22, Gradle 8.10, and RuneLite client/API 1.12.39: **37 tests, zero failures, errors, or skips**. Tests for deleted integration features were removed; the remaining suite covers supported behavior. The packaging test verifies Java 11 bytecode, plugin metadata, notices, and classpath resources in the ordinary distribution JAR.

GitHub CodeQL default setup is enabled for Java to satisfy the repository's existing code-scanning merge rule. The initial setup scan passed on 2026-09-23; pull-request scans run before protected-branch merges.

The required PR scan identified exponential backtracking in the training-target regular expression. On 2026-09-24, possessive matching removed the ambiguous retries. A regression covers an 80,009-character malformed training instruction within a two-second timeout, plus comma/and/ampersand skill lists and separate target levels. The full suite passed after this post-acceptance fix.

QuestCape now has no cross-plugin UI adapter, registry, launch/resume bridge, resume setting, or remembered-quest controls. Production code contains no references to the removed integration classes, external account service, foreign sidebar search, or programmatic button clicks. Old resume configuration values are left untouched and unused. README, this checklist, the scenario audit, developer review, and current screenshots describe only supported behavior.

All production and test Java bodies remain expanded for readability. The previous formatting change was isolated in its own commit. The [developer review](developer-rules-review.md) records retained API-based simplifications and corrects the earlier assessment of cross-plugin Swing manipulation.

## Automated coverage

- Local character sync: real skills and RuneLite quest states; profile/hash/mode readiness; explicit freshness; manual persistence; account isolation; logout during a delayed disk write.
- Lifecycle: first-tick readiness, event-triggered reads, idle ticks without repeated reads, explicit sync without HTTP, queued work discarded after logout/hop/shutdown, and request/navigation cleanup.
- Content: table/header validation, safe links, partial/invalid responses, conditional cache refresh, cancellation during persistence, and offline cache reclassification.
- Categories: alternate training wording, combat thresholds, optional recommendations, unsupported skills, labeled miniquests without automatic identity, generic comments and milestones, unknown imperative actions, and stable manual keys.
- Swing: category colors and icons, gold in-progress state, comment dividers, actionable numbering/totals, no Open button, card and keyboard detail toggles, independent links, dismissible notices, text size, narrow layouts, and scroll stability across 40 live renders.
- External links: bounded allowlisted requests/redirects, training destination fallbacks, and browser-error recovery without changing completion.

The captured guide revision **15336101** retains all **351 rows**: 109 Training, 193 Quest, 14 Miniquest, 24 Diary, 8 Unlock, 1 Activity, and 2 informational notes. That yields **349 actionable steps** and no Unknown categories in this fixture. Counts are observations, not parser limits. A synthetic future step exercises Unknown presentation. The parser uses structure and language rather than exact example text or final-row position.

Ten current screenshots in `docs/ui/` render the actual panel with synthetic data at **242 × 820**. They show categories, details, comments, the ending milestone, and navigation. They are standalone Swing renders, not in-game acceptance. Production quest/diary/skills icons load through [SpriteManager.getSpriteAsync](https://github.com/runelite/runelite/blob/runelite-parent-1.12.39/runelite-client/src/main/java/net/runelite/client/game/SpriteManager.java) with gameval constants; previews use the fixed-size vector fallbacks. Combat targets use [Experience.getCombatLevel](https://github.com/runelite/runelite/blob/runelite-parent-1.12.39/runelite-api/src/main/java/net/runelite/api/Experience.java) on real skill snapshots.

## Previous acceptance

The user confirmed all local-character-sync in-game checks passed on 2026-09-23 for source commit `1e8ffb8b9cc9b164c899cbe788e2cced5dc53eda`: initial login, explicit Sync, automatic quest/skill updates, real rather than boosted levels, manual-check persistence, account/mode isolation, reconnects, plugin restart, and cached use during external network failure.

## Current in-game acceptance — passed

On 2026-09-23 the user confirmed that everything below passed for source commit `84330bc240d568d25fb78c224cb3aa3d3912efff` and authorized merging the source PR and submitting the Hub re-enable update. The acceptance-record update changes documentation only.

1. Enable QuestCape and log in. Confirm the character and route load, explicit Sync refreshes local progress, and a quest/skill change still updates automatically. Hop/reconnect and disable/re-enable the plugin to check recovery.
2. Confirm quest and miniquest cards have **no Open button**. There should be no integration-status section, remembered-quest control, or resume setting. Click titles, card backgrounds, and Details; they should only expand/collapse QuestCape content. With Quest Helper installed, its selection, search, and panel should remain unchanged by QuestCape actions, login, and hops.
3. Check borders: Unlock purple, Unknown gray when present, Diary green, Training teal, Activity orange. Normal quest/miniquest styling remains; in-progress text and border are gold/yellow, and completed cards retain their green background.
4. Confirm a type icon beside every actionable step number. Quest, diary, and training use standard RuneLite icons; miniquests have a gold quest marker; other categories show a question mark, lock, or task icon. Check the normal sidebar width and plugin restart.
5. Confirm “Train Combat level to 85” is Training and its completion matches the real combat level. The parenthetical Slayer recommendation must not add a requirement. Natural history quiz and Knight Waves Training Grounds should be Miniquest steps with manual completion. Check one, Sync, restart, and verify it persists for the same character.
6. The XP-order explanation and closing cape message should appear as unnumbered text between dividers, with no card, status, checkbox, or Details control. Numbering, completion totals, and Next step must exclude them.
7. Compare the existing cached route with a successful refresh. Categories should agree and existing manual checks survive. Check scrolling, top/bottom and Next step navigation, keyboard detail toggles, wiki links, and training links.

Run `./gradlew.bat run` on Windows (`./gradlew run` elsewhere). For development-client login follow [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts). Only the user performs game actions and confirms acceptance.

## Submission gate

[Plugin Hub PR #16488](https://github.com/runelite/plugin-hub/pull/16488) was merged with `disabled=Requires changes from code review`. The [review](https://github.com/runelite/plugin-hub/pull/16488#issuecomment-5799882730) prohibits manipulating another plugin’s UI. The corrections have now passed user acceptance.

**The user approved source merge and Hub submission on 2026-09-23.** The Hub update must reference the latest source master commit, remove `disabled`, and retain the external-request warning. Its PR should briefly explain the two integration removals requested during the original review, link that review, and use the existing `plugin change` label. Final Hub acceptance remains with RuneLite maintainers.
