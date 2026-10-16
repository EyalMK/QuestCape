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
  display the prepared **percentage number without `%`**, with no current-level number.
  Hover retains the percentage. Completed targets keep the full green ring and
  checkmark; unknown/target-only percentages show an em dash, not invented progress.
  Source X→Y and real level still drive the same shared domain evaluation, filters,
  totals and Continue. For level 54 in 52→55 the center shows **66** and hover shows **66%**.
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

### Numeric-only center follow-up

Per the user's latest request, the center now shows `66`, while hover retains
`66%`. Checkmarks, unknown markers, counterclockwise arcs and version 3.1.0 remain.
The three training-progress tests and native-preview test pass on Java 11; the
225-pixel preview was inspected. The 123-test full-suite result above belongs
to the preceding implementation. This one-line display follow-up was verified
with the affected four tests, not another full-suite run. OpenSpec strict
validation and `git diff --check` pass. User acceptance remains pending.

Checked production tree: `27f0a6404feb5905ba15d74ac2204b4efe0aedd0`.
Updated ordinary JAR SHA-256:
`7F735F22937466941B76350AA379E2B0B4113D4CDD2E7E1418BAE66E2C89C8F3`.
Ignored log: `.tmp/training-circle-number-verification.log`.


### Responsibility-based package organization — latest candidate

The user requested maintainable subfolders in guide, integration, progress and UI.
Moved **111 existing Java files: 85 production and 26 test files**, with folders
matching their package declarations. The [maintainer package map](package-layout.md)
explains the 22 responsibility groups, retained section-root contracts and common
change-entry points. `GuideActions` is the only new production contract, extracted
from the former nested panel interface. `TestGuideLinks` is test-source-only.
Stateful panel collaborators, classifier implementation and interaction timer
internals retain package-private encapsulation. Shared style/interaction entry
points and immutable producer boundaries use only needed cross-package visibility.
Icons reference the same theme constants directly instead of panel aliases.

The pre-move baseline at `18d3ccd` passed **123 tests across 28 suites**.
After organization, Temurin 11.0.22 / Gradle 8.10 / Java --release 11 / cached
RuneLite client/API 1.13.2 passed
`.\gradlew.bat --offline --no-daemon clean build resolvedVersions --console=plain`:
**123 tests across 28 suites, zero failures/errors/skips**, including legacy cache
readback, lifecycle/authority, all progress/UI regressions and ordinary-JAR checks.
No dependency, entry point, version, config key, schema, predicate or resource-name
change. Packaged relocated classes exist under their new names; old moved names
and the test-only link fixture are absent from the clean distribution.

A token comparison covers **all 127 pre-existing Java files**, allowing only
package/import/access changes and documented equivalent substitutions: the
extracted action-interface name, direct theme aliases, the same classifier
producer boundary and the same real panel-link factory through a test fixture.
Executable logic and assertions otherwise match. All **seven** regenerated native
preview PNGs match pre-move SHA-256 values exactly, including narrow, wide,
related-detail, compound/missing and scaled fixtures. This is synthetic layout
parity, not in-game acceptance. Package-to-folder checks, both strict OpenSpec
validations and `git diff --check` pass. Existing Gradle/unchecked-test notices
remain; offline dependency resolution does not prove newest upstream compatibility.

Ignored evidence: `.tmp/package-organization-baseline.log`,
`.tmp/package-organization-final.log`, and
`.research/package-organization-2026-10-16/{moves.json,baseline.json,verify.py}`.
The verifier obtains original source from the pinned Git revision; it does not
need duplicate source snapshots. Reusable checks require no production reflection,
process execution, extra runtime dependency or game input.

Branch/version remain local `codex/questcape-sidebar-ux` / **3.1.0**. Package moves
do not replace combined human acceptance or the later full rules audit. Offer
`./gradlew run` with the official Jagex-account instructions above. The user should
check startup/cached loading, real local progress and resync, search/filter/Continue,
long-card disclosure, link/copy/selection behavior and disable/re-enable/account
isolation on this exact candidate. No launch is itself a passing test; only the
user operates RuneScape. No push, merge, PR, archive or Hub update.

Checked package-organization production tree:
`63d5b784cf958d29db8ee8bf9bc905e00fa266eb`.
Ordinary `questcape-3.1.0.jar` SHA-256:
`9A6A38F478AFC95AE5E333C6BDF4C52E374D5B8D3C72CAA0E3CEB89DE09F67B7`.
Master remains `40eb54a520970db9edb77720d254d5b0f1b2f207`.
OpenSpec records **53/55** checked tasks; only 9.4 human acceptance and 9.5 the
subsequent final rules audit remain pending.
