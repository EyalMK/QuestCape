# QuestCape

<img src="icon.png" alt="QuestCape gold map and blue route arrow" width="32" height="32">

A native RuneLite sidebar for the OSRS Wiki's standard optimal quest route. It loads the complete ordered table at runtime, keeps quests and interleaved training/activities visible, and tracks the current RuneLite account.

## Features

- Follow quests, miniquests, training and activities in the wiki's recommended order.
- Identify step types by their icons and border colors; read guide notes between dividers.
- See live quest and skill progress, with built-in local character sync.
- Expand row details, open training guides and check off manual activities.
- Jump to your next step or either end of the route, with cached content available offline.

<img src="docs/ui/sidebar-top.png" alt="QuestCape sidebar with progress, training steps and navigation" width="242">

The preview uses synthetic test data. The [Plugin Hub submission](https://github.com/runelite/plugin-hub/pull/16488) was merged with QuestCape disabled pending review changes. The corrections passed user in-game acceptance on 2026-09-23; re-enabling the Hub entry remains subject to maintainer review. Build and run it using the instructions below.

## Development setup

Use a JDK compatible with Gradle 8.10. Java 11 bytecode is generated. This build was verified with Temurin 11.0.22 and RuneLite 1.12.39, resolved from the template's `latest.release` selector. The wrapper comes from `runelite/example-plugin` commit `5370caa0f5f6a5bba4fbb42931722ca535ad3fd5`.

```powershell
git clone https://github.com/EyalMK/QuestCape.git
cd QuestCape
./gradlew.bat build
./gradlew.bat run
```

On macOS/Linux use `./gradlew`. Enable **QuestCape** in the development client's plugin settings. Character sync requires no other plugin. For Jagex accounts, follow [RuneLite's development-client login instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts); credentials are never entered into this plugin.

If you have multiple JDKs installed, set `JAVA_HOME` to your Java 11 installation before running Gradle.

The Java package and Gradle group are `com.questcape`. RuneLite loads `QuestCapePlugin`, and the development `run` task starts `QuestCapeLauncher`. Local data is stored under `.runelite/questcape/`.

## Using the sidebar

- Open **QuestCape** from the sidebar's gold map with a blue route arrow.
- Your current character appears after the first logged-in game tick and account/profile readiness. Quest and skill changes update progress automatically. The account's **Sync icon** forces a fresh local reading and saves it on this computer. Hops and reconnects wait for character readiness again.
- The top **refresh icon** retrieves the latest wiki route. Account sync and route freshness are separate.
- Green plus a checkmark means complete; gold marks in-progress quests. Type borders are purple for unlocks, gray for unknown steps, green for diaries, teal for training, and orange for activities. Quest and miniquest borders retain their existing styling apart from gold in-progress highlighting. All completed steps stay visible.
- A type icon sits beside each step number. Miniquests use the quest icon with a small gold marker. Guide comments and milestone messages appear as text between dividers; they are excluded from step numbering, completion totals, and next-step navigation.
- Click a card's title, background, metadata or status to expand/collapse notes and source fields. **Details**, Enter or Space on the focused card do the same. Links and checkboxes retain their separate actions. Wiki projections never determine your real progress.
- Text is larger throughout: 13 pt metadata, 14 pt body/navigation, 16 pt titles, and an 18 pt header. The top progress bar is 22 pixels tall and shows the completion percentage.
- The fixed bottom controls jump to the **top/bottom** based on your position, or to the **next step** (first in-progress quest, otherwise earliest unfinished action). Arrows show which direction it is. Keyboard: Ctrl+Home, Ctrl+End, Ctrl+J. Ordinary updates do not move the viewport.
- Training links open the relevant Theoatrix guide. Compound steps expose each skill. Unmapped destinations use the explicitly labeled all-guides fallback. Native browser failures use RuneLite's copy-link recovery; synchronous errors also show a retry/copy message.

## Progress and synchronization

Character sync reads the quest, miniquest and subquest states exposed by RuneLite's `Quest` API, plus every real skill level. It uses `Quest.getState(Client)` and `Client.getRealSkillLevel(Skill)` on `ClientThread`. No character information is sent to a server, and no external account service is required. Each established RuneLite RS profile and mode is supported; the displayed route remains the standard wiki route.

Progress is keyed by the established profile and mode, not the displayed name. Existing manual checks and local progress files keep their keys. Older external account caches are no longer read. Every explicit Sync refreshes the observation timestamp, even when values are unchanged. Logged-out or unready characters cannot sync, and queued work from an earlier login cannot replace the current character. Unknown values stay unknown; boosted/drained levels are never used for training thresholds.

Quests, subquests, and pure training thresholds use authoritative observations. Combat training uses RuneLite's combat-level calculation from the character's real skills; optional parenthetical recommendations are not completion requirements. A separate **start** stage becomes complete once that quest is in progress; its later finish stage remains unfinished. Miniquest classification follows the guide's label even when RuneLite has no corresponding quest identity; those miniquests, unlocks, diaries and reward hand-ins use reversible **manual** checks. Unresolved training targets remain unknown. Skill levels never prove a reward was collected. Indistinguishable repeated activities remain visible, but cannot share a manual check. Manual records follow unchanged action identities across reordering; altered actions or targets are not guessed to match old records.

## Local cache

Data is under `.runelite/questcape/`: versioned `guide/` content and separate `progress/` account records. The plugin loads validated cache immediately, updates its row classifications using the stored guide content, revalidates after 24 hours, and keeps the previous snapshot when requests/parsing fail. Refreshes coalesce and use a five-second cooldown, conditional headers, bounded response sizes, and timeouts. Unknown schemas are retained rather than deleted. HTTP failures show age/status; no-data responses are not interpreted as zero completion.

## Verification and distribution

```powershell
./gradlew.bat build
./gradlew.bat verifyRemoteContracts
./gradlew.bat resolvedVersions
```

`verifyRemoteContracts` checks only the public wiki guide HTTP adapter and accepts no player names. It does not start RuneLite or perform game actions. Automated tests cover local progress, isolation, explicit sync, lifecycle cancellation, persistence, parser/cache behavior, external links, resources, and scrolling. Character fixtures are synthetic and use the fictional name `maple scout`. Narrow Swing previews are generated at `build/ui-evidence/`. See [verification evidence](docs/verification.md), the [scenario audit](docs/scenario-audit.md), and the [developer rules review](docs/developer-rules-review.md).

**The user accepted all in-game checks on 2026-09-23**, including local character sync, category styling, icons, comments, combat training, miniquest classification, and removal of cross-plugin controls. See the verification document for the accepted source revision and checklist.

The distribution is `build/libs/questcape-0.1.0.jar`. `build=standard` is retained. Main code uses only Java 11 and the client-provided classpath; the inert HTML DOM uses the JDK parser, with no jsoup/custom runtime dependency. Tests and any development fat JAR are not Plugin Hub artifacts. The ordinary JAR includes only this plugin's classes, resources, metadata, and notices. Packaged resources use `getResourceAsStream`.

Source repository: [EyalMK/QuestCape](https://github.com/EyalMK/QuestCape). The root `icon.png` is a transparent 32 x 32 PNG, within the Plugin Hub's 48 x 72 pixel limit. Re-enabling the disabled Plugin Hub entry requires a separate manifest update and maintainer review, following the [official publishing instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin). Local build checks do not constitute Plugin Hub approval.

Code is BSD-2-Clause. Wiki content is attributed to OSRS Wiki contributors under CC BY-NC-SA 3.0 and applicable additional terms; see [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md). Synthetic character and captured content fixtures are test resources, excluded from the distribution JAR.
