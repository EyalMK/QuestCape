# QuestCape 3.1.0 — current combined private-branch candidate

Status: **awaiting user in-game acceptance and the subsequent full rules audit**.
Branch: `codex/questcape-sidebar-ux`; master remains unchanged. No push, PR,
merge, archive or Hub update is authorized.

## Branch version decision

The user selected **3.1.0 once for the entire private branch**, against accepted
base 3.0.1. The complete compatible sidebar/training/selection feature scope
justifies a minor increment. Follow-up implementation iterations retain that
version. The previous 3.2.0–3.4.0 labels were unreleased private iterations;
their documents retain original fingerprints as historical evidence, not releases
or current acceptance. `AGENTS.md` and [release policy](release-policy.md) record
the corrected rule. Build, root properties, packaged metadata, ordinary artifact
and metadata-derived HTTP product identity all use 3.1.0.

## Latest presentation and interaction decisions

- Training arcs start at the top and fill **counterclockwise**. Unfinished centers
  display the prepared **percentage with `%`**, with no current-level number.
  Hover retains the percentage. Completed targets keep the full green ring and
  checkmark; unknown/target-only percentages show an em dash, not invented progress.
  Source X→Y and real level still drive the same shared domain evaluation, filters,
  totals and Continue. For level 54 in 52→55 the center/hover shows **66%**.
- Hold Ctrl to drag-select or double-click words, then Ctrl+C to copy. Releasing
  Ctrl retains the highlight/focus and restores hand cursors. Ordinary clicks
  still disclose immediately. **No Escape selection-cancellation action exists**.
  Ordinary disclosure, focus loss, replacement and disposal clear selection.
- The small hint says **“Hold Ctrl to select text. Ctrl+C to copy.”** It appears
  after **1000 ms** of hovering over a non-control card surface. `SelectionHint`
  owns one one-shot Swing timer/popup per card; leaving, pressing, dragging,
  hiding, ancestor movement or disposal cancels it. It does not change the global
  `ToolTipManager` delay or replace independent link/projection/progress tooltips.
  No production input event, global keyboard hook, network work or blocking wait.

The layered refactor, four Prayer/Farming fixes, long-advice fold anchoring,
independent related-links/projection/resync controls, typography and approved
header/search/filter/footer direction remain. Shift-right-click link copying
retains the 1.6-second local confirmation; normal browser opening is silent.
See the historical [refactor evidence](verification-3.2.0.md),
[circle/link/scroll evidence](verification-3.3.0.md) and
[Ctrl/cleanup evidence](verification-3.4.0.md) for prior tested revisions. Latest
choices here supersede their current-level center, Escape action and version plan.

## Automated verification

On 16 October 2026, Temurin **11.0.22**, Gradle **8.10**, Java `--release 11`,
cached RuneLite client/API **1.13.2**:
`.\gradlew.bat --offline --no-daemon clean build resolvedVersions --console=plain`
passed **123 tests across 28 suites**, zero failures/errors/skips, including the
ordinary-JAR Java 11/resource/PNG/metadata checks. Ignored full log:
`.tmp/branch-3.1.0-verification.log`. Existing unchecked-test/Gradle deprecation
notices remain. Offline checks do not establish newest upstream compatibility;
no new public endpoint check was needed for this presentation-only correction.
`TrainingProgressTest` checks painted counterclockwise orientation using the filled
versus unfilled arc colors, plus percentage/completion/unknown/retained-handler
behavior. The test accounts for antialiased round caps rather than requiring one
endpoint pixel to equal the exact stroke RGB. `SelectionHintTest` controls timer
expiry and uses a fake popup/screen to check the 1000-ms one-shot configuration,
no early popup, placement, cancellation, stale callbacks, small copy without
Escape and unchanged shared timing. It performs no wall-clock wait or native
popup/game input. `CtrlTextSelectionTest` retains native caret/word/drag/copy
coverage with an in-memory clipboard, independent regions and no Escape binding.

The full Java 11 parser/progress/lifecycle/cache/interaction/package suite remains
required. Native previews are actual RuneLiteLAF components with source fixtures
and synthetic observations, not game screenshots or user acceptance. OS focus,
real clipboard access, native tooltip timing/scaling and the separate Ubuntu
freeze remain unverified until the appropriate human/environment checks.

## User acceptance checklist

I can launch the development client using `./gradlew run` (`.\gradlew.bat run`
on Windows). Follow [RuneLite's official Jagex-account login instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Only the user operates RuneScape.

1. On an in-progress training card, check that the ring advances counterclockwise
   from the top and its center shows a percentage, with no level. Verify completed
   checkmarks and unknown percentages remain honest. Hover should show percentage.
2. Hover on card text/background for less than one second: no selection hint.
   Remain for one second: a small Ctrl-selection/copy hint appears, without Escape
   wording. Leave/click/scroll/fold/restart: no stale popup. Other tooltips keep
   their existing behavior.
3. Ctrl-drag and Ctrl-double-click wrapped text, release Ctrl and copy into an
   external editor. Ordinary clicks must disclose immediately; focus leaving or
   folding must clear selection. Escape is not a selection-cancellation shortcut.
4. Recheck long-advice folding/scroll stability, independent projections/links,
   related-link defaults, icon-only resync, search/filter/clear/Continue, four
   earlier training regressions and live updates/account restart. Retained
   Shift-right-click link copying should confirm locally without browser opening.

Automated success is not in-game acceptance. Combined acceptance and the following
official-rules audit remain pending for this corrected candidate.

Inspected actual component previews at 225×640 and representative 1.5× device
transform: centered percentages fit the rings, arcs fill counterclockwise and
the retained header/footer layout wraps. The suite also generates normal/wide,
completed, compound and missing-progress variants. Both active OpenSpec changes
pass strict validation and the diff passes `git diff --check`.

## Exact checked candidate

Implementation: `bb5a44d53389f9039bd6746412228bd9aa69f8f4`.
Production `src/main` tree: `c633d22fec8bd3331402756ac3fb36764acfdf50`.
Master remains `40eb54a520970db9edb77720d254d5b0f1b2f207`.
The following evidence-only update changes no source, tests, build or resources.

Artifact: `build/libs/questcape-3.1.0.jar`, SHA-256:
`CE603A9F38BA1A16DF695F70ED0AAED579625AB70F8291B60373C5869A683DDE`.
Packaged metadata derives `QuestCape/3.1.0 (RuneLite companion)`.
OpenSpec records **51/53** tasks checked; 9.4 combined in-game acceptance and
9.5 subsequent full rules audit remain pending. No merge or publication.
