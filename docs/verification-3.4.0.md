# QuestCape 3.4.0 Ctrl selection candidate

Status: **awaiting user in-game acceptance and the subsequent final rules audit**.
Local branch `codex/questcape-sidebar-ux` remains unpushed and unmerged. The
previous refactor/sidebar/interaction candidates remain historical; this combined
candidate retains their intended behavior and requires user acceptance.

## Selected interaction — 16 October 2026

The user selected **option A: hold Ctrl to select**. Ordinary title/body/advice
clicks disclose immediately with a hand cursor. Hold Ctrl while dragging or
double-clicking text to select it without folding. Release Ctrl to restore the
hand cursor while retaining the selection/focus for **Ctrl+C**. Escape, ordinary
disclosure, focus leaving the card, replacement or disposal clears selection.
The permanent Select text/Done selecting action is removed. A text hover hint
and card accessibility description teach the modifier, copying and Escape.

`CardInteraction` owns the ephemeral EDT state. Local pointer/focused-key
listeners preserve the installed native caret and copy bindings; ordinary text
gestures are consumed before the caret can steal focus/scroll, while Ctrl
gestures pass through unchanged. No global dispatcher/game input hook, production
synthetic event, selection timer, persisted preference or runtime dependency.
When keyboard focus is outside the sidebar, the next pointer event refreshes
the modifier cursor. Projections remain read-only; links, Shift-right-copy,
checkboxes and subsections keep their independent actions.

Version **3.4.0** is a compatible minor increment: a newly selected interaction
replaces the permanent selection action without changing persisted data, config
keys, public interfaces or account ownership. Build/plugin properties, packaged
metadata and derived HTTP product identity agree. The previous fold-position,
training-circle/percentage-only hover and link-copy work is retained; see
[3.3.0 evidence](verification-3.3.0.md) for its design and specific checks.

## Cleanup and retained evidence

At the user's request, remove all ten historical `docs/ui/sidebar-*` images,
superseded research mockups/text, unused formatter binaries, intermediate logs,
old request payloads and one-off migration/delivery scripts. Current test captures
already target `build/ui-evidence/`; no output-path change is needed.

Retain the community review, source-design follow-up, refactor decisions and
current UI review; all five comparator source/discussion snapshots; original live
captures/metadata/hashes; the regression attachment; approved revision-3 mockup
with assets/composition/provenance; referenced historical verification logs;
reusable formatter source/scripts; branding and nested repositories, including
their working changes. Cleanup removed 222 checked file/directory targets. The ignored
`.research/cleanup-record.json` records categories, retained-evidence integrity
checks and record limitations; the adjacent `cleanup-record.md` explains the
selection. Deletion used checked absolute workspace-contained paths and rejected
reparse points/nested Git repositories. Original captures, approved assets and
pinned decisions/attachment match independent pre-existing hashes; all five
comparator snapshots retain their recorded file counts. Synthetic renders are never live evidence.

## Automated checks

On 16 October 2026, Temurin **11.0.22**, Gradle **8.10**, Java `--release 11`,
cached RuneLite client/API **1.13.2**:
`.\gradlew.bat --offline --no-daemon clean build resolvedVersions --console=plain`
passed **121 tests across 27 suites**, zero failures/errors/skips, plus ordinary
JAR/resource/PNG/Java 11/metadata checks. The resolved runtime classpath is unchanged.
Existing Gradle deprecation/unchecked-test notices remain; offline resolution does
not prove compatibility with newest upstream. No new public endpoint check was
needed for this UI-only change. Ignored full log:
`.tmp/ctrl-selection-final-verification.log`.

`CtrlTextSelectionTest`
exercises native caret drag and word selection on plain/styled text under
RuneLiteLAF, modifier-release retention, copying into an isolated **in-memory**
clipboard, unchanged native caret/copy bindings, instant ordinary disclosure,
Escape/focus/disposal cleanup and independent controls/read-only projections.
Tests never write the OS clipboard, open the browser or interact with RuneScape.
Existing parser/progress/lifecycle/cache/resource/interaction suites remain part
of the Java 11 build. Native previews are actual Swing with synthetic fixtures,
not game screenshots or OS focus/scaling acceptance. The separate Ubuntu freeze
and native environments remain unverified.

## User acceptance checklist

I can launch the development client with `./gradlew run` (`.\gradlew.bat run`
on Windows). Log in using [RuneLite's official Jagex-account instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Only the user operates RuneScape.

1. Plain-click titles, advice, the middle and edges of cards: expansion/folding
   is immediate. Long advice folding preserves card position except unavoidable
   end clamping. Links, related subsections and projected tooltips act independently.
2. Hold Ctrl over text, drag a passage or double-click a word. It must select
   without folding. Release Ctrl, then Ctrl+C and paste into an external text
   editor. Check wrapped advice and skill-icon titles. Escape, ordinary folding,
   focus outside the card and plugin restart must clear stale highlights/cursors.
3. Confirm retained training circles show the current level, percentage-only
   hover and complete checkmarks; the four Prayer/Farming rows remain correct.
   Shift-right-click links should copy with a 1.6-second local confirmation and
   no browser launch; normal links still open normally. Never paste into game chat.
4. Exercise search/filter/clear/Continue, icon-only resync, related-link defaults,
   account switch/restart and live updates while reading/selecting. Full earlier
   refactor acceptance remains in [3.2.0](verification-3.2.0.md); newer interaction
   choices here supersede its permanent selection mode.

Passing automated checks does not establish in-game acceptance. Keep both
OpenSpec changes active and acceptance/final audit pending; no archive, push,
PR, merge or Hub update is authorized.

Reviewed actual RuneLiteLAF component previews at 225×640 and the expanded
320×1364 related-links view: no permanent selection action remains, content wraps
and retained training/header/footer controls fit. The suite also generates
240×1364/320×900, compound/missing fixtures and representative 1.25×/1.5× device
transforms. These checks do not validate live game input, native OS focus, popup
placement, OS clipboard access or true display scaling.

## Exact candidate evidence

Tested implementation: `c8ad2bdd8807a1b9a1f4dc1445928908d022f75c`.
Production `src/main` tree: `98b6f525b74a22472daf0475d9c18d5e6ef53371`.
The following evidence-only update changes no source, tests, build or resources.
Master remains `40eb54a520970db9edb77720d254d5b0f1b2f207`.

Artifact: `build/libs/questcape-3.4.0.jar`, SHA-256:
`AF81C373E436680029567685BCA52426B25FEAA792A4202C3DDFDFAE4DC55B21`.
Packaged metadata generates `QuestCape/3.4.0 (RuneLite companion)`.
Both active OpenSpec changes pass strict validation and `git diff --check`
passes. The refactor change has **47/49** tasks checked; only 9.4 combined
in-game acceptance and 9.5 subsequent final rules audit remain pending.
