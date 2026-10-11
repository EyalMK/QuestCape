# QuestCape 1.1.0 candidate verification

Date: 2026-10-12 (Asia/Jerusalem). Extends the unpublished 1.0.0 candidate `8456d028ad9592a8d44744fe9caa940770fc3b1c` on `codex/automatic-activity-progress`. The exact expanded candidate revision is recorded in the local OpenSpec acceptance record at handoff. No PR or remote branch has been published.

## Changes and checks

The compatible navigation additions bump the candidate to 1.1.0. Type options display local icons before names. Multiple types match alternatives, multiple states match alternatives, and the groups combine together. Unknown is separate from Incomplete. Filters preserve route numbering and whole-route totals, hide informational notes and make next-step navigation use visible unfinished steps. The reset icon clears filters and search.

Search waits 300 ms after typing pauses, then scrolls to the best visible title without hiding more rows. Ranking uses exact/prefix/substring/token/limited-typo matches with route-order ties. Empty/no-match searches do not jump. Clearing/resetting, manual navigation and disposal invalidate pending work. The one-shot Swing Timer and owned popups are cleaned up on plugin shutdown.

Recognized projected levels display a number followed by a bundled 16px RuneLite skill icon, with named tooltip/accessibility text. Unrecognized/annotated source text remains visible. SkillIconManager images load on the worker and publish on the EDT under generation checks. Projection rendering never changes live facts, guide fields, account keys or cache schemas. No runtime dependencies, config keys, network requests or game input are added.

Automated environment: Windows host, Temurin 11.0.22, Gradle 8.10, RuneLite client/API 1.13.1. `./gradlew.bat --no-daemon --offline build` verifies tests, Java 11 bytecode and the actual 1.1.0 distribution JAR/metadata. Coverage includes combined filters, live manual-state changes and logout, original numbering/global totals, actual debounce/coalescing, filtered no-match behavior, reset/manual-navigation/disposal cancellation, search ranking/limits and projected number/icon/accessibility semantics. Synthetic Swing previews use bundled skill images. Existing activity/storage/account/lifecycle regressions remain included.

Synthetic 242px previews under `build/ui-evidence/` were visually inspected. The dark search field, compact Types/State buttons, reset icon, route cards and projected numbers/icons fit the sidebar. These previews are not RuneScape interaction or proof of in-game behavior. Earlier public route coverage is recorded in [1.0.0 verification](verification-1.0.0.md); these additions do not change parsing, activity mappings or HTTP contracts.

## Acceptance and delivery gates

Result: **54 automated tests passed; Java 11 build and distribution packaging passed**. Distribution: `build/libs/questcape-1.1.0.jar`. Diagnostic logs are ignored local files under `.research/filter-final-build.log` and `.research/filter-tests.log`.

User acceptance remains pending for the expanded candidate: combine types and states; check Unknown/empty results and logout; type exact/partial/typo queries rapidly; verify filtered search and cancellation; reset during pending search; inspect projected skill icons/tooltips and normal keyboard/narrow-width behavior; repeat restart/disable-enable and earlier automatic-progress checks.

The original Ubuntu freeze remains unverified; no affected-machine evidence or confirmed root cause has been supplied. Repeat sustained responsiveness checks before/after world entry, Sync, guide refresh and restart on the affected Ubuntu/Jagex Launcher installation. Do not claim that UI/build tests establish freeze resolution.

Only the user interacts with RuneScape. Offer `./gradlew run` (`./gradlew.bat run` on Windows) and use the official [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) instructions. After explicit acceptance, conduct the fresh complete RuneLite/Jagex rules audit for the accepted revision, then open the source PR to the user-confirmed `master`. The Plugin Hub PR still requires the user's separate later instruction.
