# QuestCape development and usage

## Development setup

Use a JDK compatible with Gradle 8.10. Java 11 bytecode is generated. The 3.1.0 candidate uses Temurin 11.0.22 and cached RuneLite 1.13.2, resolved from the template's `latest.release` selector. The wrapper comes from `runelite/example-plugin` commit `5370caa0f5f6a5bba4fbb42931722ca535ad3fd5`.

```powershell
git clone https://github.com/EyalMK/QuestCape.git
cd QuestCape
./gradlew.bat build
./gradlew.bat run
```

On macOS/Linux use `./gradlew`. Enable **QuestCape** in the development client's plugin settings. Character sync requires no other plugin. For Jagex accounts, follow [RuneLite's development-client login instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts); credentials are never entered into this plugin.

If you have multiple JDKs installed, set `JAVA_HOME` to your Java 11 installation before running Gradle.

The Java package and Gradle group are `com.questcape`. RuneLite loads `QuestCapePlugin`, and the development `run` task starts `QuestCapeLauncher`. Local data uses RuneLite's `getPluginDirectory()`/`Filepath` boundary under `.runelite/plugin-data/questcape/`. The descriptor retains `legacyDataDirectory="questcape"` so RuneLite can migrate `.runelite/questcape/` when the destination does not yet exist. Existing destination data is not merged or overwritten by that migration.

## Using the sidebar

- Open **QuestCape** from the sidebar's gold map with a blue route arrow.
- Progress updates after the first logged-in game tick and account/profile readiness. The opposing-arrows icon inside **Resync Progress** forces a fresh local reading and retries saving it; the surrounding label and shell are inert. Account/mode and separate progress/route timestamps remain under **Source and progress details** at the route footer. Hops and reconnects wait for readiness again; account scopes remain isolated.
- The circular-arrow icon beside **OSRS Wiki** refreshes the route. The quietly outlined GitHub icon beside QuestCape opens the issues page; its small floating tooltip says **Report issues or contribute**. Route refresh and local resync remain separate operations. Unavailable/unsaved/failure messages stay visible outside the route scroller.
- Green plus a checkmark means complete; gold marks in-progress quests and supported training ranges. Type borders are purple for unlocks, gray for unknown steps, green for diaries, teal for training, and orange for activities. Quest and miniquest borders retain their existing styling apart from gold in-progress highlighting. All completed steps stay visible.
- A type icon sits beside each step number. Miniquests use the quest icon with a small gold marker. Guide comments and milestone messages appear as text between dividers; they are excluded from step numbering, completion totals, and next-step navigation.
- Click a card's title, body text, background, metadata or status to expand/collapse details immediately with a hand cursor. **Details**, Enter or Space on the focused card do the same. Hold **Ctrl** over card text to select it by dragging or double-clicking without folding the card. Release Ctrl to restore the hand cursor; the highlight remains available for **Ctrl+C**. An ordinary disclosure click, focus leaving the card, replacement or disposal clears selection. A text hover hint appears after one second and explains this gesture; there is no Escape cancellation action; no permanent selection button remains. Links, checkboxes, projected-level regions and subsection controls retain independent behavior. Related wiki pages start folded beside other meaningful details, or unfolded when they are the only content; blank/N/A placeholders do not count. The subsection remembers your choice across parent folds and ordinary live updates.
- Text uses Segoe UI with RuneLite's font fallback for symbols and systems where the font is unavailable. Status, metadata and notes use smooth text rendering. Sizes are 13 pt metadata, 14 pt body/navigation, 16 pt titles, and an 18 pt header. The top progress bar is 22 pixels tall and shows the completion percentage.
- The single circular up/down icon above full-width **Continue** jumps to the **top/bottom** based on your position. **Continue** jumps to the earliest visible unfinished action, including unchecked or unknown steps. Keyboard: Ctrl+Home, Ctrl+End, Ctrl+J. Ordinary updates do not move the viewport.
- Training links open the relevant Theoatrix guide. Compound steps expose each skill. Unmapped destinations use the explicitly labeled all-guides fallback. Normal successful browser opens are silent. **Shift + right-click** a training, wiki, license or GitHub link to copy its configured destination without opening a browser or issuing a request. A small local **✓ Link copied** popup lasts 1.6 seconds; unavailable clipboard access shows a failure instead. Browser failures retain RuneLite's native recovery and actionable errors.

## Filters, search and projected levels

Use the funnel/list icon to select one step type, and the funnel/check icon to select one Complete, Incomplete, In progress or Unknown state. One Type and one State can apply together. Selecting another option replaces the previous choice. Each menu has an All option. State choices use the same glyphs/colors as the cards; Type choices have a larger icon/name gap. Both filter triggers and reset are evenly spaced across the available width. Unknown includes unverified manual actions and unavailable character facts; it is separate from Incomplete. Filters hide informational notes, retain original step numbers, and limit next-step navigation to visible unfinished steps. The main progress totals always cover the entire route; the filter count shows visible steps.

**Search steps** scrolls to the best visible title match about 300 ms after typing pauses. Exact titles take priority, followed by prefixes, substrings, token matches and limited typo tolerance; ties use route order. Relevance determines only the initial jump; counters and traversal use ascending step numbers, so the initial counter can be other than 1/N. Search does not hide more steps or override filters. The field shows borderless current/total with a downward chevron beside a separate lightly outlined **Clear search** X. Match feedback is below the field; the shown-step count and global progress remain independent. Clear only removes the query and retains filters. Enter resolves pending search, then advances matches in ascending step-number order; at the last match, press Enter twice within 500 ms to return to the first. No match shows 0/0 and a message. Clearing the query restores the pre-search card/offset, including after query replacements. Ordinary navigation cancels deferred jumps; live changes update membership/counts without scrolling. The **reset icon** clears both filter groups and the query, returning to the full route and restoring the pre-search position when available. Controls are session-only and introduce no config migration or network requests.

Expanded **Guide projected levels** show each recognized level as a number followed by RuneLite's local 18px skill icon. Hover/accessibility text names the skill. Unknown/annotated source text is preserved. These are read-only guide projections: hovering retains skill tooltips, clicking does not fold the card or navigate, and they never affect live completion. Icons load on a worker; the debounce timer/popups are cleaned up on shutdown.

Additional info displays actual local skill icons before whole skill names, preserving the original wording and wrapping. Skill images are prepared on the worker before sidebar publication, without waiting behind guide-cache work. Pending images never substitute the generic training icon. Wrapped titles use the installed text UI to measure height, preserving long balloon destinations under RuneLite's look and feel. Runecrafting/Runecraft and Defense/Defence aliases use the same skill icons. No guide images are loaded from the network.

The full-width **Continue** button (play icon, Ctrl+J) aligns the earliest visible unfinished card at the viewport top. It stays enabled if that card is visible but not aligned, and disables once the reachable target is reached. A centered circular arrow above it switches between the route's top and bottom destinations.

## Progress and synchronization

Character sync reads the quest, miniquest and subquest states exposed by RuneLite's `Quest` API, every real skill level, all 48 diary tiers' completion/reward facts, and explicit Natural history quiz/Knight Waves, balloon, Stronghold, Museum Camp and Herbi Flax predicates. It uses `Quest.getState(Client)`, `Client.getRealSkillLevel(Skill)` and named gameval varbits on `ClientThread`. See [activity mappings](activity-detection.md). No character information is sent to a server, and no external account service is required. Each established RuneLite RS profile and mode is supported; the displayed route remains the standard wiki route.

Progress is keyed by the established profile and mode, not the displayed name. Eligible Kudos checks and local progress files keep their keys; other retired manual checks remain inert. Older external account caches are no longer read. Every explicit Sync refreshes the observation timestamp, even when values are unchanged. Logged-out or unready characters cannot sync, and queued work from an earlier login cannot replace the current character. Unknown values stay unknown; boosted/drained levels are never used for training thresholds.

Quests, subquests, diaries, miniquests and pure training thresholds use authoritative observations. Combat training uses RuneLite's combat-level calculation from the character's real skills; optional parenthetical recommendations are not completion requirements. A separate **start** stage becomes complete once that quest is in progress; its later finish stage remains unfinished. Diaries and miniquests have no manual controls, including unsupported future rows, which remain unknown. Retired manual keys cannot override automatic facts. Only the two Varrock Museum Kudos steps keep reversible **manual** checks. Balloon destinations, the first three Stronghold coin rewards, Museum Camp reward and Herbi Flax hand-in use verified automatic predicates; unsupported unlocks and hand-ins remain unknown without a checkbox. Unresolved training targets remain unknown. Skill levels never prove a reward was collected. Indistinguishable repeated activities remain visible, but cannot share a manual check. Manual records follow unchanged action identities across reordering; altered actions or targets are not guessed to match old records.

## Local cache

Data is under `.runelite/plugin-data/questcape/`: versioned `guide/` content and separate `progress/` account records. Directory initialization/migration, bounded UTF-8 reads, atomic writes and temporary-file cleanup run on background workers. Navigation icon decoding uses a memory image stream on the worker, with no ImageIO temporary disk cache. Startup/shutdown do not wait for storage or network operations. The plugin loads validated cache, updates its row classifications using the stored guide content, revalidates after 24 hours, and keeps the previous snapshot when requests/parsing fail. Refreshes coalesce and use a five-second cooldown, conditional headers, bounded response sizes, and timeouts. Unknown schemas and unreadable originals are retained rather than overwritten. Unsupported atomic moves leave the old file intact. Valid live observations and fetched routes remain usable in memory when saving fails, with an unsaved status. Progress retries after at least 60 seconds on a later observation, or immediately on explicit Sync; guide refresh retries caching. Resolve an unreadable/unsupported cache before recovery can replace it. HTTP failures show age/status; no-data responses are not interpreted as zero completion.

The reported Ubuntu 22 failure is a permanent freeze shortly after login through Jagex Launcher for Linux (Beta). The user confirmed the entire OS stops responding and requires a PC restart. These changes address verified storage/startup weaknesses; the freeze's root cause and Linux resolution remain unconfirmed until the affected-machine test. Collect the failed boot's system/kernel journal and RuneLite logs first, following [Ubuntu freeze diagnostics](ubuntu-freeze-diagnostics.md). A navigation-layout hypothesis was rejected because its regression passed with the original code. See [candidate verification](verification-1.0.0.md) for evidence and limitations.

## Verification and distribution

```powershell
./gradlew.bat build
./gradlew.bat verifyRemoteContracts
./gradlew.bat resolvedVersions
```

`verifyRemoteContracts` checks only the public wiki guide HTTP adapter and accepts no player names. It does not start RuneLite or perform game actions. Automated tests cover local progress, isolation, explicit sync, lifecycle cancellation, persistence, parser/cache behavior, external links, resources, and scrolling. Character fixtures are synthetic and use the fictional name `maple scout`. Narrow Swing previews are generated at `build/ui-evidence/`. See [verification evidence](verification.md), the [scenario audit](scenario-audit.md), and the [developer rules review](developer-rules-review.md).

The historical 3.0.1 in-game acceptance was confirmed on 2026-10-12. The corrected 3.1.0 candidate is **awaiting new user acceptance**; those earlier checks do not validate this implementation. At the user's direction, the unresolved Ubuntu whole-system freeze is a separate investigation; it is not claimed fixed by this release. The post-acceptance [compliance review](compliance-3.0.1.md) records the accepted code, checks and limitations. See [release policy](release-policy.md).

The distribution is `build/libs/questcape-3.1.0.jar`. `build=standard` is retained. Main code uses only Java 11 and the client-provided classpath; the inert HTML DOM uses the JDK parser, with no jsoup/custom runtime dependency. Tests and any development fat JAR are not Plugin Hub artifacts. The ordinary JAR includes only this plugin's classes, resources, metadata, and notices. Packaged resources use `getResourceAsStream`.

Source repository: [EyalMK/QuestCape](https://github.com/EyalMK/QuestCape). The root `icon.png` is a transparent 32 x 32 PNG, within the Plugin Hub's 48 x 72 pixel limit. Plugin Hub updates require a separate manifest update and maintainer review, following the [official publishing instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin). Local build checks do not constitute Plugin Hub approval.

Code is BSD-2-Clause. Wiki content is attributed to OSRS Wiki contributors under CC BY-NC-SA 3.0 and applicable additional terms; see [THIRD-PARTY-NOTICES.md](../THIRD-PARTY-NOTICES.md). Synthetic character and captured content fixtures are test resources, excluded from the distribution JAR.


## Combined candidate 3.1.0

Details place full route advice before projections. Route advice, Quest points
and Guide QP total share 14-point bold headings and 14-point body values.
**Wiki quick guide** remains the primary expanded action; **Quest page** is under
**Related wiki pages**. Unknown fields and N/A remain inspectable. Wiki skill and
Theoatrix actions identify their providers and retain their separate URL checks.

Training titles show RuneLite's actual skill images. For a supported source X→Y
and real unboosted level L, below X is Incomplete, X≤L<Y is In progress, and L≥Y
is Complete. Training circles replace the old status/comparison text: **66** appears inside an unfinished circle, with **66%** on hover for the floored, clamped position in the 52→55 interval, not XP or time.
Arcs start at the top and advance counterclockwise. Completed targets show a full green circle/checkmark with **100%** on hover.
Missing bases/observations show an undefined percentage rather than inventing it;
compound requirements have named comparisons and cannot complete with missing
evidence. Legacy caches remain browsable offline; no account/config reset is used.

See [candidate verification and manual checklist](verification-3.1.0-current.md). Native
Swing previews use source fixtures and synthetic observations, not live game
captures. The local development branch must remain unmerged and unpushed unless
the user gives a new explicit instruction.


## Source and runtime boundaries

`GuideParser` retains bounded `SourceRow` evidence; `GuideClassifier` reconciles
identity/action candidates and `GuideAssembler` copies prepared values into
`GuideRow`. `CompletionPolicy` owns finite evaluation behavior. Typed quest,
skill-target and activity keys use `LegacyKeyCodec` only at persistence boundaries.
Unknown/ambiguous actions remain visible without invented completion. Role-specific
training/projection/mention interpreters share lexical/catalog recognition; Swing
receives prepared annotations rather than interpreting advice as requirements.

`ActivityRegistry` composes diary/balloon families and five exceptional detectors;
per-key observation failures remain unknown. `PendingManualEdits` retains scoped
true and false choices until their exact saved revisions succeed. Persisted
content and save outcomes publish together. Cache recovery never uses set union
to resurrect a cleared check or overwrites unreadable/unknown-schema originals.

`QuestCapePlugin` delegates to a per-enable `QuestCapeSession`. `PluginRun` owns
bounded executors and idempotent cleanup; progress, guide refresh, sidebar assets/
publication and explicit training-link workflows have separate cohesive owners.
Callbacks retain their originating run and account authority across asynchronous
work. Client reads remain on ClientThread, IO/images on workers and UI mutations
on EDT. Shutdown cancels without waiting. Existing filter/search/navigation
algorithms and user scroll intent remain in their established owners.

Root `runelite-plugin.properties` is packaged once via `processResources` for both
development and the ordinary JAR. `PluginMetadata.Loader` validates/loads it once
on the first HTTP worker request; User-Agent uses its product name/version. Invalid
metadata causes a controlled request failure while cached/local browsing remains.
No new runtime dependency, reflection or external process is introduced.

Long-card disclosure restores the clicked card's pixel position after updated row
heights and scrollbar range have settled. Scrollbar end clamping still applies;
newer user navigation cancels the queued restoration. The selected hold-Ctrl gesture uses only local sidebar pointer/focused-key listeners and preserves the installed native caret/copy actions. No global game key hook is installed. When the sidebar does not own keyboard focus, the next pointer event updates its modifier cursor.
