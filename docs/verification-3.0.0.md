# QuestCape 3.0.0 candidate verification — 2026-10-12

Status: ready for user acceptance, with the original Ubuntu freeze investigation and final compliance audit still pending. The exact source revision and JAR digest are recorded in the local OpenSpec acceptance record at handoff. No branch or PR has been published.

## Changes and evidence

The user requested single choices within Type and State filters. Selecting another option replaces the previous choice; All clears that group, and the two groups compose. Removing within-group multiple selection is breaking behavior under the existing version policy, so this candidate advances 2.0.0 to 3.0.0. Gradle, packaged metadata and User-Agent versions agree.

Search retains the pre-search card and offset across query edits, restores that view on clearing, and displays a current/total counter with a downward chevron inside the field. Enter advances ranked visible matches. At the last match, two presses within 500 ms wrap to the first. The chevron uses the same traversal. Query replacement, pending debounce, manual navigation, live filter changes, route replacement and disposal are guarded against stale jumps. Live membership changes update counts without forcing a scroll.

Icon-only Type, State and reset triggers span the panel width. Type options have a larger icon/name gap; State options use the card status glyphs and colors. These controls retain tooltips and accessible names. Projected level icons increase from 16px to 18px, prepared on the worker from bundled RuneLite images; Additional info icons remain 16px. The smaller Continue/play button aligns the earliest visible unfinished card at the viewport top and remains enabled while that card is visible but not aligned. Its target is clamped to the reachable scroll range.

No new dependencies, network requests, config keys, storage changes or game input automation are introduced by this refinement. Earlier automatic completion and storage resilience evidence remains in [2.0.0 verification](verification-2.0.0.md) and its referenced records.

## Automated verification

Windows, Temurin 11.0.22, Gradle 8.10, RuneLite 1.13.1. `./gradlew.bat --no-daemon --offline build --console=plain` passes 68 tests, including Java 11 distribution and metadata checks. Tests cover the real debounce timer, saved scroll restoration across replacements and pending queries, Enter/click traversal, wrap timing and cancellation, live updates, route replacement, single-choice filter composition, control spacing, status presentation, projected icon size and Continue alignment. Existing detector, parser, persistence, account/session and lifecycle regressions also pass.

Synthetic narrow Swing previews in `build/ui-evidence/` were inspected, including `sidebar-search-matches.png` showing 2/4 and Continue, and `sidebar-skill-info.png` showing 18px projections and inline skill icons. They contain synthetic character data and are not game screenshots. Build outputs are ignored. The build log is `.research/search-build.log`. Public guide coverage passed for the preceding candidate; this refinement does not change parsing or activity predicates.

## Pending user gates

Offer to launch the development client from the plugin root with `./gradlew run` (`./gradlew.bat run` on Windows), following [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts). Only the user performs game actions. Exercise all ten refinements alongside the existing automatic activity, persistence and affected Ubuntu responsiveness checklist. A successful build or JVM start is insufficient acceptance.

The Ubuntu permanent freeze remains without an affected-machine reproduction or confirmed cause. Resilience changes do not prove resolution. After explicit acceptance of the exact candidate, re-read current official RuneLite/Jagex guidance and audit the complete production plugin before opening the source PR to `master`. Functional fixes found by that audit require affected acceptance again. The Plugin Hub PR requires a separate later explicit instruction.
