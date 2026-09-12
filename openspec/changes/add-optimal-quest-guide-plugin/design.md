## Context

Branding update: the plugin is named **QuestCape** in its descriptor, sidebar tooltip/header and Hub metadata; the Gradle project/distribution is `questcape`. Keep the existing Java entry point, `optimalquestguide` configuration group and data directory so the rename preserves plugin identity, settings and account progress.

Latest interaction update: the whole card, including its title, toggles details. A compact **Open** button beside the step number/type is now the explicit Quest Helper action. It reuses the installed Quest Helper tab's icon through the public JTabbedPane API; the plugin does not bundle that asset or retain another plugin's panel. Open, source/training links and manual checkboxes remain independent controls. This replaces the earlier title-as-launch interaction.

Single-result follow-up: the user confirmed the search fallback with a Sheep Shearer screenshot and explicitly requested pressing the result's arrow when only one result is displayed. Count visible QuestSelectPanel rows after the synchronous search document listener finishes; click the sole enabled LINE_END arrow once. Zero/multiple results and changed arrow layouts remain manual. The native listener owns assist/branch setup. Report RESULT_SELECTED separately from confirmed activation, without creating resume intent.

Screenshot feedback (2026-09-12): enlarge all guide text to 13–18 pt and the progress indicator to 22 pixels; make the card body toggle details while its quest title opens Quest Helper; locate quest-action feedback immediately above its card. The user explicitly authorized opening Quest Helper and filling its search field when the proposed PluginMessage receiver is unshipped. This supersedes the earlier manual-opening-only fallback. Use RuneLite's existing tabbed sidebar and public Swing components, without reflection or Quest Helper class imports. The fallback remains distinct from confirmed helper activation and cannot create resume intent.

Implementation update requested by the user on 2026-09-12: remove arbitrary-player lookup. The panel uses the established current RuneLite account, automatically requests its WikiSync STANDARD data on login, and offers a current-player Sync icon. A separate refresh icon updates wiki content. Preserve legitimate repeated activities, including separate quest start and finish stages. The sidebar uses color plus text, fixed navigation to the top/bottom and next progress row, and no caret-driven automatic scrolling during live updates. These decisions supersede the original username-field and Use current player flows below. Unsupported Quest Helper handoff remains gated; the user has not authorized changing upstream Quest Helper.

This is a new repository with no application, build system, or existing specifications. The product is a RuneLite companion to Quest Helper. The [supplied screenshot](references/wiki-progress-example.png) is preserved as a visual reference for ordered mixed activities, guide details, and green/checkmarked completion; its contents are reference data rather than additional instructions.

The source is the table captioned **Old School RuneScape Quest Guide** on the standard OSRS optimal quest guide page. The screenshot includes ordinary quests, miniquests, repeated skill-training milestones, transport unlocks, and a reward hand-in. A quest-only list would lose part of the requested guide.

Research checked on 2026-09-12:

- [OSRS guide](https://oldschool.runescape.wiki/w/Optimal_quest_guide): source table and surrounding explanation were available through search indexing; direct page retrieval failed in this environment. Implementation must capture current HTML/API fixtures and verify selectors rather than assume the indexed revision is current.
- [Quest Helper PR #2756](https://github.com/Zoinkwiz/quest-helper/pull/2756): GitHub API reports `state=open`, `merged=false`. It proposes an inbound `questhelper/start` PluginMessage. This is evidence of a proposed contract, not a shipped API. The issue's separate `launch` example is not a contract either.
- [Quest Helper source](https://github.com/Zoinkwiz/quest-helper/blob/master/src/main/java/com/questhelper/QuestHelperPlugin.java): inspected source has no PluginMessage receiver. Its own internal startup paths do not establish supported cross-plugin access.
- [WikiSync README](https://github.com/weirdgloop/WikiSync): describes player data uploaded to wiki infrastructure. The user has selected WikiSync as a required plugin and specified `GET https://sync.runescape.wiki/runelite/player/{player_name}/STANDARD` for lookup, with spaces replaced by underscores. Provider selection is settled; capture real response fixtures and verify field mapping during implementation.
- [Theoatrix directory](https://www.theoatrix.net/all-guides) and [skill directory](https://www.theoatrix.net/1-99-guides): relevant entry points; individual skill URLs must be verified instead of generated from guessed slugs.
- [RuneLite Plugin Hub documentation](https://github.com/runelite/plugin-hub/blob/master/README.md): basis for project scaffolding and dependency verification. A successful local build alone does not establish Plugin Hub acceptance.
- [RuneLite game API](https://static.runelite.net/runelite-api/apidocs/) and [client API](https://static.runelite.net/runelite-client/apidocs/): the inspected overview identifies 1.12.38; this is the documentation version, not evidence of a locally tested runtime. Game-state access and external-plugin coordination belong to different packages, as detailed below.
- [Developer Guide](https://github.com/runelite/runelite/wiki/Developer-Guide): directs new features toward Plugin Hub development and the example project, and distinguishes plugin lifecycle, persistent configuration, event subscribers, and Swing panels. Follow that external-plugin path without modifying the RuneLite core client.

## Goals / Non-Goals

**Goals:**

- Display the complete source table in order inside RuneLite with readable details and screenshot-equivalent completion cues.
- Support the username/Look up workflow through the required WikiSync plugin and specified STANDARD endpoint, plus automatic live progress for the current account.
- Launch supported quests in Quest Helper and restore the last unfinished selection after logout/login by default, with a persistent on/off option in this plugin's RuneLite settings page.
- Incorporate ordinary wiki content changes without distributing a new plugin binary.
- Route training clicks to relevant Theoatrix guides in the system browser.

**Non-Goals:**

- Alternative Ironman/free-to-play routes, route optimization, replacement quest walkthroughs, gameplay automation, hosted accounts/backends, and Plugin Hub publication.
- Reproducing the full wiki website, executing its JavaScript, or implementing quest walkthrough internals.
- Guaranteeing compatibility with arbitrary future HTML, new game-state definitions, or unannounced third-party API changes without code maintenance.

## Decisions

### 1. Native RuneLite panel with separate adapters

Use the [official example-plugin build](https://github.com/runelite/example-plugin/blob/master/build.gradle): Java 11 bytecode, Gradle wrapper, `net.runelite:client` as `compileOnly`, RuneLite client and jshell on the test/development classpath, and the template's `run` task with developer mode. Retain `latest.release` as the development dependency selector and record the exact resolved version with every verification run. Do not mistake the Javadoc version for a resolved build dependency. Organize code under `com.optimalquestguide` into `guide`, `progress`, `integration`, `persistence`, and `ui`; keep client lifecycle coordination in the plugin class and I/O out of Swing components.

Follow the [example plugin lifecycle](https://github.com/runelite/example-plugin/blob/master/src/main/java/com/example/ExamplePlugin.java): extend `Plugin`, annotate with `@PluginDescriptor`, inject RuneLite services with `@Inject`, provide this plugin's config using `@Provides`/`ConfigManager.getConfig`, and handle subscribed events in the plugin entry point before delegating to services. Add/remove the sidebar navigation entry in `startUp`/`shutDown`. Do not instantiate another plugin or its service graph.

`GuideRepository` fetches and validates content, `GuideParser` creates immutable rows, `ProgressService` combines account observations, `QuestHelperBridge` owns launch compatibility, and `TrainingGuideResolver` owns external destinations. Reuse RuneLite HTTP/JSON facilities; use a DOM parser such as jsoup if verified against the build's existing dependencies, otherwise record the required dependency verification.

Network and parsing work run on a bounded background executor, game-state reads on RuneLite's client thread, and Swing rendering on the EDT. Generation tokens discard results after account changes, newer requests, or plugin shutdown. Shutdown cancels work and unregisters subscriptions.

Alternative: an embedded browser could copy the wiki appearance but adds a rendering engine and fragile page-script dependencies. Native rows fit RuneLite and give explicit progress/launch behavior.

### 1a. Build and package for Plugin Hub

Use `runelite-plugin.properties` with accurate display name, author, description, tags, and entry-point class. Prefer `build=standard`; Hub packaging replaces project Gradle files in this mode, so a custom local classpath is not a distribution solution. If an extra dependency is unavoidable, document its need and the Hub dependency-verification process before selecting a custom Gradle build. Do not bundle Quest Helper or WikiSync classes.

Load packaged resources with `getResourceAsStream`, including from a built JAR. Keep mutable cache/progress outside the JAR. Add the recommended BSD-2-Clause code license and preserve third-party content notices separately. Validate the distribution JAR as well as the development launcher. Hub publication remains out of scope; local success is not a claim of maintainer approval. These packaging choices follow the [Plugin Hub README](https://github.com/runelite/plugin-hub/blob/master/README.md).

### 2. Fetch the table as data and retain a last-known-good snapshot

Prefer the MediaWiki parse API for the named page with revision metadata, or a verified HTML endpoint if necessary. Discover the table by normalized caption plus expected column headers, not a positional table index. Normalize whitespace, entities, rowspans, colspans, wiki links, and merged training/activity rows. Do not use hard-coded row counts or quest point totals.

Each `GuideSnapshot` contains source URL, revision when available, retrieval time, parser schema version, and ordered `GuideRow` values. A row contains a stable key, source position, kind, title, canonical wiki target, optional quest identity, optional skill/from/to targets, guide levels, QP reward, running guide QP, notes, location, and relevant source links. Keep guide-projected levels/QP distinct from actual player values. Unknown activity types remain visible with their text and an explicit unknown status.

Quest keys use canonical quest/subquest identity. Training keys use skill and target plus action semantics. Unlocks and actions use canonical target/action identity; use context to distinguish repeated occurrences, never the row index alone. Preserve progress only across unambiguous matches. Changed targets get new identities; collisions or ambiguous migrations retain prior records separately and request manual review without guessing completion.

Load a valid disk cache immediately, then revalidate on startup when older than 24 hours. A **Refresh guide** action revalidates on demand; coalesce overlapping requests and enforce a short cooldown. Use conditional HTTP requests when supported, explicit timeouts and bounded retry/backoff. Validate the entire candidate before atomic replacement. Detect missing/ambiguous tables, invalid structure, identity collisions, and suspicious truncation; preserve the old snapshot and display a warning rather than publish partial content silently. New unsupported rows alone do not invalidate an otherwise intact table.

On first-run failure, display an actionable empty/error state with Retry. With a cache, retain it and show its age and refresh failure. Fetching guide revisions never resets account records. Render sanitized text and approved links only; no remote scripts or executable configuration. Include OSRS Wiki attribution and source/revision links, and verify applicable content-license notices before bundling or redistributing fixtures.

Alternative: bundling the order is simple but violates content freshness. A hosted transformation service would add operations and is unnecessary for this change.

### 3. Compact ordered rows preserve access to every table field

Add a sidebar navigation entry. At the top show the viewed account, username field, **Look up**, **Use current player**, content refresh status, and **Refresh guide**. Below, display scrollable rows in wiki order. Each row exposes title, type, and status; expand details for levels, QP, notes, location, and source quick-guide link. The primary quest title action launches Quest Helper; a separate wiki link is secondary. Training remains visually distinct and opens Theoatrix.

Completed rows use green plus a checkmark/text. Incomplete rows use a neutral background and incomplete marker, in-progress quests have their own label, and unknown rows use a question mark. Unsupported helpers are a separate availability indicator, not a completion state. Keyboard activation, readable contrast, wrapped notes, and usable narrow-sidebar layout are acceptance criteria. Totals count completed actionable rows over total actionable rows and report unknown rows separately; informational rows are excluded. Keep completed rows visible by default, matching the screenshot.

Alternative: a seven-column table is closer to the screenshot at browser width but becomes unreadable in a narrow sidebar. Expandable details preserve the information and order without requiring a second browser.

### 4. Account-scoped progress with explicit provenance

`AccountProgress` stores identity/game-mode scope, last-observed time, per-quest states, real skill levels, verified activity facts, and manual activity checks. Use RuneLite account profile identity for live records and normalized username plus mode for lookup records; link them only after identity is established. Account name changes must not attach another player's cached record merely through a display-name match.

Completion rules:

- Quests/subquests: authoritative finished state; in-progress is distinct from incomplete. Resolve canonical names and explicit aliases for punctuation, renamed quests, and Recipe for Disaster parts. Unmapped entries remain unknown.
- Pure training: complete when the observed unboosted level reaches the target. Guide projected levels and temporary boosts are not evidence.
- Diaries, unlocks, and action/reward hand-ins: use verified explicit predicates where available; otherwise permit reversible manual completion. A skill threshold alone never proves a reward was collected or a transport route unlocked. Manual state is labeled and never overrides known authoritative quest/skill state.

For live observations, use [`Quest.getState(Client)`](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Quest.html), `Quest.getId()` and explicit mapping for quest identity, and [`Client.getRealSkillLevel(Skill)`](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Client.html) for training targets. Reevaluate from `GameStateChanged`, `VarbitChanged`, and `StatChanged` subscriptions with initialization/readiness checks on `ClientThread`. Use current API definitions rather than duplicating quest varbit constants from Quest Helper. Quest completion can be read independently of the active Quest Helper walkthrough. Isolate all remote STANDARD observations from non-standard live profiles.

Live observations take precedence over remote observations for the same established account. Lookup does not change the actual game account or resume target. Selecting a different lookup account displays only its data; errors never leave another account's green rows under the new name. Missing fields remain unknown. Requests carry account and sequence tokens so stale responses cannot overwrite a newer lookup.

WikiSync is a required installed/enabled plugin, alongside Quest Helper. Expose actionable prerequisite status; if WikiSync is missing or disabled, explain how to install/enable it and make fresh lookup unavailable while keeping guide browsing and previously cached observations usable with their age shown. Do not silently install or enable dependencies. WikiSync performs its own player synchronization; this plugin consumes its API and adds no separate upload mechanism.

Every explicit **Look up** action validates the entered name, trims surrounding whitespace, replaces each internal space with `_`, and URL-encodes the normalized value as one path segment. Send `GET https://sync.runescape.wiki/runelite/player/{player_name}/STANDARD`. For example, `snooze meist` becomes `https://sync.runescape.wiki/runelite/player/snooze_meist/STANDARD`; existing underscores remain underscores. Use the same normalized identity for request association and lookup cache matching so space/underscore spellings do not create separate records. This change always uses `STANDARD`; do not infer another API mode from the current world.

Implement this contract in `WikiSyncProgressProvider` behind `PlayerProgressProvider`. Each submitted lookup requests the latest available server data, including for the current player; a local/cache-only answer must not masquerade as a fresh lookup. Live observations can still take precedence in the displayed current account's merged progress. Persist successful API observations with request time and the server's observation timestamp when provided; absence of a server timestamp must not imply the data was just uploaded. The endpoint returns the latest data WikiSync has received, which is not a guarantee of immediate synchronization with the game.

Map the verified response fields into quest and skill observations. Handle absent profiles, partial or stale payloads, throttling, offline service, and access failures without inventing completion. Explain that the named account needs available WikiSync data when no profile exists. Hiscores or another provider is not a substitute for the specified lookup. Provider choice is not an open implementation decision.

Alternative: local-only progress avoids a provider but cannot satisfy first-time lookup of a logged-out username. A local-only fallback remains useful but does not complete that requested acceptance criterion.

### 5. Access plugins through RuneLite's client API

The screenshot follow-up authorizes a UI search fallback. On the EDT, find the guide's existing JTabbedPane ancestor, uniquely identify its `com.questhelper.panel.QuestHelperPanel` descendant, and select the containing tab. RuneLite 1.12.38's sidebar change listener owns activation, deactivation and history. Its public `ClientToolbar.openPanel(NavigationButton)` is available when a target button is already held, but there is no public getter for another plugin's registered NavigationButton or the proposed `getSideBar().openPanel` chain. Use the existing tab directly rather than constructing a duplicate button. Within the verified panel's north header, find its single client-owned IconTextField and set its text, which runs Quest Helper's normal document listener. When the assist/settings view hides search, its existing "Change your settings" button returns to the list. Refuse ambiguous/changed layouts. Explicit RFD display aliases handle Start, Dwarf, Monkey Ambassador and Finale. Search success is a separate SEARCH_READY result, never a confirmed launch.

The linked `net.runelite.api` documentation describes the game interface. Plugin coordination lives in `net.runelite.client`. Use this division explicitly:

| Need | RuneLite surface | What it establishes |
| --- | --- | --- |
| Find loaded Quest Helper and WikiSync instances | Inject `PluginManager`; enumerate `getPlugins()` | Actual loaded `Plugin` objects, not a quest-launch service |
| Determine dependency readiness | `isPluginEnabled(plugin)` and `isPluginActive(plugin)` | Configured enablement versus successfully started plugin |
| Refresh dependency status | `PluginChanged` / `ExternalPluginsChanged`, followed by a fresh manager snapshot | Install/remove/start/stop changes; verify event timing against the target runtime |
| Coordinate between Hub plugins | `EventBus` with `PluginMessage` | A transport that requires a receiver-defined contract |
| Read local progress | `Quest`, `Client`, and game events | Quest/skill state, not selected helper state |

The manager methods are documented in [PluginManager](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/plugins/PluginManager.html). Distinguish absent/not loaded, disabled, enabled-but-inactive, and active dependencies. Match the verified plugin entry-point identity, not a fuzzy display label. Cache no plugin object across unload/reload. Dependency detection must not silently enable, start, or reconfigure either prerequisite. `startPlugin(Plugin)` controls plugin lifecycle and cannot select a quest.

The generic [`Plugin`](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/plugins/Plugin.html) API exposes `getInjector()`, but does not define quest-specific operations. The published Quest Helper 4.17.0 source exposes public methods including `QuestMenuHandler.startUpQuest(String)`, `QuestManager.getSelectedQuest()`, and `QuestHelperPlugin.displayPanel()`. These are Quest Helper types, not RuneLite API types. Source visibility alone does not demonstrate callable cross-plugin access.

Inspection of [`ExternalPluginManager`](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/externalplugins/ExternalPluginManager.java) and [`PluginHubClassLoader`](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/externalplugins/PluginHubClassLoader.java) shows a separate loader per Hub JAR, parented by the client loader. Consequently, ordinary compile-only imports cannot see a sibling plugin's classes at runtime; shading them produces distinct class identities. A development run with both plugins on a single classpath is not sufficient proof. [`PluginDependency`](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/plugins/PluginDependency.html) takes an actual plugin class; its annotation does not install a Hub plugin or supply sibling class visibility. Inspecting another injector also does not create a typed quest-launch contract.

Use a `RuneLitePluginRegistry` adapter for discovery/readiness, with a separate `QuestHelperBridge` for launch and active-quest observation. Prefer RuneLite's documented [`PluginMessage`](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/events/PluginMessage.html) transport when a released Quest Helper receiver supports it. PR #2756 proposes `questhelper/start` with `quest` and `source`, but remains unmerged in the verified evidence. Neither this namespace nor an acknowledgement/capability event can be assumed to exist. Require an actual supported receiver and activation observation before enabling launch. A direct public-method adapter is an alternative only if a concrete distribution-compatible contract is demonstrated; do not implement reflective injector scraping, private-field access, synthetic game/chat events, or configuration writes as substitutes.

The bridge accepts canonical quest identity and reports pending, confirmed started, already active, unavailable, unsupported, or failed. Preserve subquest/branch semantics, open the helper panel through the proven contract, and record resume intent only after actual selected-helper confirmation. A transport post alone is not success. Losing dependency readiness cancels pending work. Keep the guide usable with an actionable compatibility explanation; manual opening remains a fallback, not full handoff acceptance.

The analysis narrows the blocker to quest-specific launch/observation: RuneLite discovery, live progress, settings, guide loading, WikiSync, and training links can be implemented independently. Task 1.1 is a handoff verification gate rather than a prerequisite for the entire scaffold. No upstream changes or publications are included by this design update.

### 6. Persist resume intent separately from displayed progress

Implementation evidence (2026-09-12): the account-scoped, versioned confirmation store and login coordinator now exist, with a 50-game-tick readiness budget and local adapter/lifecycle tests. They remain inactive behind the unverified production Quest Helper contract. Local simulated confirmations do not satisfy the distributed handoff/resume acceptance gates.

After a confirmed quest launch from this plugin, save `ResumeIntent(accountProfile, questIdentity, lastConfirmedAt)` locally. Add a boolean **Resume quest on login** option to this plugin's standard RuneLite settings page using its configuration interface, enabled by default and persisted through RuneLite configuration. The setting must be accessible without opening a quest row. Turning it off immediately cancels pending automatic resume attempts and prevents future login resume; it does not stop the current helper or disable manual quest clicks. Turning it on makes the remembered unfinished quest eligible on the next login, without forcing an immediate launch. Preserve the preference across restart and provide a separate **Clear remembered quest** action. Guarantee resume for quests launched through this plugin; observing arbitrary quests started elsewhere requires a separately verified public lifecycle signal and is not assumed.

Implement `OptimalQuestGuideConfig extends Config`, `@ConfigGroup("optimalquestguide")`, and `@ConfigItem(keyName = "resumeQuestOnLogin", name = "Resume quest on login", ...)` with a default boolean value of `true`, following the [example configuration](https://github.com/runelite/example-plugin/blob/master/src/main/java/com/example/ExampleConfig.java). Handle `ConfigChanged` only for this group/key. Use this plugin's [`ConfigManager`](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/config/ConfigManager.html) RS-profile configuration for the remembered quest after profile readiness; the toggle is a regular persistent setting. Never write Quest Helper's configuration to simulate quest selection.

On a fresh login session, wait for account identity, quest state, and bridge readiness. Reopen once when the saved quest is unfinished for that same account. Quest Helper determines its actual current step from game state. Logout retains the intent; an explicit clear, observed completion, or disabling resume prevents reopening. Account switch cannot reuse another account's intent. Hops/reconnect bursts must not issue duplicate starts. An already active helper for the same quest is a no-op; a different active helper takes precedence over automatic resume. Readiness retries are bounded per session, and unavailable integration is reported once without repeatedly stealing focus.

Alternative: remembering only the currently selected UI row would reopen training steps, looked-up accounts, or failed launches. Separate intent prevents those mistakes.

### 7. Explicit skill-to-Theoatrix routing

Resolve each parsed training skill through a tested map of canonical skill names/aliases to URLs verified from Theoatrix's guide directories. Prefer general skill guides; use a combined combat guide only where the directory actually provides that coverage. For compound training rows expose each relevant skill action. Reward hand-ins that mention experience remain activities unless the source explicitly represents a training step.

Use RuneLite's browser-opening utility on a user click. Accept only HTTPS URLs on `theoatrix.net` or `www.theoatrix.net`, including redirect validation when fetching link metadata. Missing mappings offer a labeled **Browse Theoatrix guides** action to the supplied all-guides page; never pretend it is skill-specific. Browser-opening failures keep the row unchanged and present Retry/copy-link recovery. Opening a guide never marks progress complete. The wiki update requirement concerns guide content; arbitrary Theoatrix URL or API redesigns can still require mapping maintenance.

## Risks / Trade-offs

- RuneLite plugin discovery does not supply quest-specific operations -> verify launch and observation under actual Hub class loading; keep only the dependent handoff/resume acceptance open until a supported contract works.
- WikiSync may be absent, disabled, unsynced, stale, or temporarily unavailable -> show prerequisite/freshness status, use the specified endpoint, and retain correctly attributed cached observations without claiming a fresh lookup succeeded.
- Wiki selectors drift or bot protection blocks access -> validate current fixtures, preserve last-known-good cache, expose retry/status, and avoid promising arbitrary format changes need no maintenance.
- Reordered or altered activity text can obscure identity -> deterministic matching, explicit aliases, and conservative migration prevent false completions.
- Some activities lack observable completion state -> labeled manual checks preserve usefulness without inferring completion from later quests or skill levels.
- Remote requests complete after account changes -> account/generation tokens and immutable snapshots prevent cross-account state leakage.
- Narrow sidebar loses browser-table density -> retain all fields in expandable details and verify the screenshot's examples at real RuneLite widths.

## Migration Plan

1. Scaffold using the official external-plugin template and implement RuneLite API discovery/settings/progress alongside WikiSync response mapping. Gate only the quest-specific launch/observation adapter on task 1.1; do not gate unrelated development on the unmerged hook.
2. Version disk cache and account record formats from the first release. Keep progress separate from replaceable guide content; reject unknown schemas without deleting records.
3. Test with fixtures, then a development RuneLite client with Quest Helper installed and an actual logged-in test account. Record exact versions and evidence.
4. Deliver code and setup instructions locally. Plugin Hub submission is a later action outside this change.
5. Roll back by disabling/uninstalling the companion plugin. It must not alter Quest Helper configuration or game state; retain local progress unless explicitly reset.

## Open Questions

- Which supported contract adds quest-specific launch/observation to the verified RuneLite discovery layer under Hub class loading? PR #2756 and public Quest Helper methods alone are insufficient proof. Resolve before marking handoff/resume implemented, not before scaffolding independent features.
- Which quest/skill fields and observation timestamps does the specified WikiSync STANDARD endpoint return? Verify response mapping with current fixtures; the provider, endpoint, and space-to-underscore normalization are fixed requirements.
- Which current wiki endpoint and table structure work from RuneLite, and what content-license notices apply to cached/distributed extracts? Capture current evidence during the retrieval spike.
- Which specific Theoatrix URLs cover each training skill present in the current source? Verify and fixture the initial map during implementation.
