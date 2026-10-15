# QuestCape 3.3.0 interaction follow-up

Status: **awaiting user in-game acceptance and the subsequent final rules audit**.
Local branch `codex/questcape-sidebar-ux` remains unpushed and unmerged. The
3.2.0 refactor and previous sidebar changes remain unaccepted historical candidates;
their relevant checks must be exercised against this corrected candidate.

## Scope and decisions — 16 October 2026

1. **Folding long advice:** disclosure anchors the clicked card in pixels. Before
   restoring it, the navigator resolves the new row geometry and viewport range,
   rather than relying on Swing validation queued after the disclosure callback.
   If the user was deep inside removed details, the collapsed card is brought
   back into view; viewport end clamping remains necessary. A newer explicit
   navigation intent cancels the deferred restoration.
2. **Training circles:** replace training status/comparison text, including the
   duplicate Complete label. Unfinished circles display only the current real
   level; hovering shows **only the percentage**, per the user's clarification.
   Completion uses a full green ring/checkmark with a `100%` tooltip. Compound
   targets have named individual circles. Unknown levels/intervals use an em dash;
   target-only unfinished steps cannot invent a percentage. Unsupported required
   clauses cannot acquire a completed checkmark. Source instructions, explanations,
   status filters, overall counts and Continue still use the same domain result.
3. **Link feedback:** normal successful browser opening is silent. Shift + right-click
   copies the configured destination directly, with no browser launch or HTTP.
   A small local `✓ Link copied` popup lasts **1.6 seconds** and changes no layout.
   Clipboard failure gets failure feedback, not a false success. Available on
   quest/related/wiki/training/license/GitHub links; route-refresh/resync controls
   remain distinct actions. A copied training URL is the configured provider URL,
   not an unverified guessed redirect. Normal left-click keeps existing destination
   verification and RuneLite browser-error recovery.
4. **Text selection:** no new interaction has been selected. Existing Select text /
   Done selecting / Escape remains available. Alternatives are presented separately
   for user choice after implementing the three requested changes.

3.3.0 is a compatible **minor** increment from 3.2.0 because the training-circle
presentation and explicit copy gesture are new features alongside fixes. Build,
root properties, packaged metadata, generated HTTP product version and current
docs align. No runtime dependency or persisted schema/config/account-key change.

## Boundaries and checks

- `GuideNavigator` owns pixel anchoring, settled geometry and latest navigation
  intent. No percentage-based scroll restoration, idle timer or extra route cache.
- `TrainingEvaluation.Comparison` supplies per-target completion; `TrainingProgress`
  only paints prepared comparisons. Circles retain the same component instances
  and dimensions during live changes, preserving installed click handlers and
  tooltips. Accessible progress values/descriptions also expose current real levels.
  Existing generic quest/status labels are unchanged.
- `LinkCopyGesture` owns the explicit pointer gesture, local popup and one-shot
  Swing timer. Drag/release-outside/disabled/disposed controls do not copy; normal
  opening and copy stay independent. Timers/popups stop on hiding, replacement and
  panel disposal. The per-run action/controller validates destinations and retains
  original run ownership; `BrowserLinks` owns clipboard/browser platform access.
- Clipboard tests inject a fake copier; no test writes the user's actual clipboard,
  opens their browser or interacts with RuneScape. No global game hotkey/input
  hook is added. Copying is user initiated and never pastes into chat.
- `DisclosurePositionTest` runs under RuneLiteLAF with long wrapped advice,
  checks stable card position through expansion/folding and simulated interim
  scrollbar changes, then verifies newer navigation wins. `TrainingProgressTest`
  checks numeric center/percentage-only tooltip, completed checkmarks, stable
  click surfaces, compound/missing/target-only/unsupported cases and absence of
  duplicate status text. `LinkCopyGestureTest` and `IntegrationTest` cover gesture
  separation, feedback lifecycle, failure honesty, unchanged dimensions, URL
  policies, closed-run rejection, silence and no request/browser on copying.
- The full existing parser/progress/cache/lifecycle/search/filter/navigation and
  ordinary-JAR/resource/metadata tests remain part of verification. No production
  reflection, process execution, injected game input or blocking lifecycle wait
  is introduced. Client/worker/EDT responsibilities remain as in the refactor.

Native previews under `build/ui-evidence/native-sidebar-3.3.0/` use actual Swing
components, RuneLiteLAF, packaged artwork/local skill images and synthetic source/
account fixtures. Narrow/short/wide/scaled previews include completed/in-progress
and compound/missing circles. They are **not live game screenshots or acceptance**.
The short-lived popup's native placement and OS clipboard must be confirmed by
the user. Linux/affected Ubuntu freeze behavior remains unverified and separate.

## User acceptance checklist

I can launch the development client with `./gradlew run` (`.\gradlew.bat run` on
Windows). Use [RuneLite's official Jagex-account instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
to log in. Only the user operates RuneScape.

1. Expand a quest with long route advice, then fold using its title/body/Details.
   The card should keep its screen position except necessary end-of-route clamping.
   Scroll within long details before folding; the collapsed card should remain
   usable. Rapid toggle followed by manual navigation must respect the newer action.
2. Inspect a training circle: current level as a number inside, **percentage only**
   on hover. Completed circle: green full ring/checkmark, `100%` hover. No separate
   standard Complete/status line. Compound targets remain individually identified;
   unavailable progress is honest. The four earlier Prayer/Farming regressions
   and interval/real-level behavior still require combined acceptance where available.
3. Normal link click should open the browser with no success banner. Shift +
   right-click wiki/quest/training/GitHub/footer links: no browser opening, correct
   URL available to paste into an external text editor, clear local feedback for
   roughly 1–2 seconds. Verify expired feedback and hiding/restart leave no popup.
   Do not paste into RuneScape chat for testing. Errors should remain actionable.
4. Exercise search/filter/clear/Continue and live resync/restart while reading.
   Existing disclosure, projected read-only tooltips, related-link subsection and
   Select text mode should remain independent and should not steal scroll/focus.

The exact checked revision, artifact fingerprint and final automated results are
recorded below. In-game acceptance and the post-acceptance official-rules audit
remain pending; do not archive, push, open a PR or merge this local branch.

## Final automated evidence

Tested implementation: `ece81b82a25834e811b7e4abb2cb1a8a7fb3e0b2`.
Production `src/main` tree: `067fe571161f56da827cebf64365cb400932743d`.
The subsequent evidence-only update changes no source, tests, build or resources.
Master remains `40eb54a520970db9edb77720d254d5b0f1b2f207`.

On 16 October 2026, Temurin **11.0.22**, Gradle **8.10**, Java `--release 11`,
cached RuneLite client/API **1.13.2**:
`.\gradlew.bat --offline --no-daemon clean build resolvedVersions --console=plain`
passed **119 tests across 26 suites**, with zero failures/errors/skips. Ordinary
JAR Java 11 class/resource/PNG/metadata checks passed; no new runtime dependency.
Both current OpenSpec changes pass strict validation and the diff passes
`git diff --check`. Existing Gradle deprecation/test unchecked-operation notices
remain. Offline resolution does not establish newest upstream compatibility.
No new public endpoint request was needed for this UI/clipboard-only follow-up;
the prior public guide check is historical and unchanged, not a new network pass.

Artifact: `build/libs/questcape-3.3.0.jar`, SHA-256:
`8BD15389CCA82D8ADB0E037A4E13D5D9799EF5FC7A07547D6073B1023F65BB9A`.
Ignored full log: `.tmp/ui-followup-committed-verification.log`.
Current metadata generates `QuestCape/3.3.0 (RuneLite companion)`.

Reviewed actual component previews at 225×640, 240×1364, 320×900, compound/missing
content and representative 1.5× device transform; prior fixture variants are
regenerated by the suite. The percentage-only tooltip is asserted directly by
the Swing test; a screenshot does not prove hover or native clipboard behavior.
Combined acceptance and the official-rules audit remain unconfirmed, as do actual
OS display scaling, popup placement and the separate affected Ubuntu environment.
