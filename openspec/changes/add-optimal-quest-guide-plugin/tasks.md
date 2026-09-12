## 1. Establish integration contracts

- [ ] 1.1 Record resolved RuneLite and distributed Quest Helper versions; prove a quest-specific launch, panel activation, and selected-helper observation contract through the RuneLite integration layer under separate Hub class loaders, including ordinary and branch/subquest cases. Discovery via PluginManager alone is insufficient. This gates 6.1/6.6, not the independent scaffold or progress features.
- [x] 1.2 Capture and verify response fixtures from the specified WikiSync `GET https://sync.runescape.wiki/runelite/player/{player_name}/STANDARD` endpoint, including quest/skill fields, timestamps, missing-profile behavior, and installed/enabled WikiSync detection; provider selection is fixed.
- [x] 1.3 Verify current wiki HTML or MediaWiki parse access, table identity, revision metadata, and applicable attribution/license notices; capture representative fixtures including merged and unknown rows.
- [x] 1.4 Verify Theoatrix destinations for skills represented in the source table and record aliases, combined-guide coverage, and missing-guide fallbacks.

## 2. Scaffold the RuneLite plugin

- [x] 2.1 Scaffold from runelite/example-plugin with its Gradle wrapper, Java 11 target, compileOnly RuneLite client, test/development client and jshell dependencies, latest.release selector, development run task, accurate plugin metadata, and code license; record resolved versions instead of treating the Javadoc version as a tested runtime.
- [x] 2.2 Add guide, progress, integration, persistence, and UI modules using @Inject; extend Plugin with @PluginDescriptor and provide the Config interface through @Provides/ConfigManager.getConfig. Prefer build=standard and verify the actual packaged dependency set before introducing a parser dependency.
- [x] 2.3 Implement startUp/shutDown navigation registration and cleanup, entry-point @Subscribe handlers delegating to services, executor cancellation, ClientThread reads, EDT updates, and stale-work generation tokens.
- [x] 2.4 Implement RuneLitePluginRegistry with PluginManager.getPlugins, exact verified plugin identities, isPluginEnabled/isPluginActive, and lifecycle-event refresh; test absent, disabled, enabled-but-inactive, active, unload, and reload states without importing or bundling prerequisite plugin classes or automatically changing their configuration.

## 3. Load and maintain guide content

- [x] 3.1 Define versioned GuideSnapshot and GuideRow models retaining all table fields, row types, source provenance, and stable action identities.
- [x] 3.2 Implement caption/header-based DOM parsing with colspan/rowspan handling, text/link normalization, interleaved activities, unknown-row preservation, and canonical quest/skill mapping.
- [x] 3.3 Implement bounded HTTP retrieval, timeouts, conditional requests, cooldown/coalescing, 24-hour startup revalidation, and on-demand Refresh guide.
- [x] 3.4 Implement complete-candidate validation, atomic last-known-good caching, first-run failure handling, cached-age status, and source attribution.
- [x] 3.5 Test ordinary revisions without a binary change, malformed/missing/ambiguous tables, suspicious truncation, unknown rows, offline startup, stale cache, unsafe markup, and concurrent refreshes.

## 4. Track and persist account progress

- [x] 4.1 Implement account/profile and mode-scoped observations, normalized lookup identities, provenance/timestamps, and versioned local persistence separate from guide cache.
- [x] 4.2 Implement quest/subquest state through Quest.getState(Client) and explicit identity mapping, unboosted targets through Client.getRealSkillLevel(Skill), verified activity predicates, and reversible manual activity checks without inferred hand-in completion.
- [x] 4.3 Implement GameStateChanged, VarbitChanged, and StatChanged handling with ClientThread/profile readiness, plus automatic current-player sync; give local observations precedence only for the same established account and keep remote STANDARD data separate from non-standard live profiles.
- [x] 4.4 Implement WikiSyncProgressProvider and current-player Sync using the exact STANDARD endpoint, trimming the input and replacing internal spaces with underscores before path-segment encoding; every current-player sync requests the latest available API data, including when local/cache data exists.
- [x] 4.5 Implement conservative progress reconciliation across row reorder, action edits, changed targets, identity ambiguity, restart, and account/mode changes.
- [x] 4.6 Test green quest completion, in-progress/unknown states, repeated skill thresholds, temporary boosts, manual check reversal, reward hand-ins, subquest aliases, stale-response races, and cross-account isolation.
- [x] 4.7 Use RuneLitePluginRegistry from 2.4 for WikiSync prerequisite status and install/enable guidance; implement invalid-name, missing/partial/stale-data, throttling, access-failure, and request-ordering handling while preserving labeled cached observations and the specified provider.
- [x] 4.8 Test `snooze meist` -> `snooze_meist`, equivalent underscore identity, surrounding whitespace, path encoding, exact STANDARD URL construction, fresh requests despite cached data, server/retrieval timestamp distinction, missing/disabled WikiSync, and recovery after enablement.

## 5. Build the guide sidebar

- [x] 5.1 Add the RuneLite navigation entry and panel with current-player identity, automatic login sync, Sync icons, position-aware navigation, and separate account/content freshness indicators.
- [x] 5.2 Render ordered quest and activity rows with green/checkmark completion, blue in-progress/next-step accents, incomplete/unknown labels, manual-state labels, helper availability, and honest progress totals.
- [x] 5.3 Add expandable details for all source fields, wrapped notes, separate source links, keyboard actions, and preserved scroll/selection when returning from Quest Helper; prevent caret-driven scrolling and coalesce live renders.
- [x] 5.4 Verify the panel at normal narrow RuneLite widths using the supplied screenshot's repeated training, unlock, completed quest, incomplete quest, and hand-in examples; record visual evidence.

## 6. Launch Quest Helper and resume after login

- [ ] 6.1 Implement QuestHelperBridge using RuneLitePluginRegistry plus the proven quest-specific contract from 1.1. Use EventBus/PluginMessage only with a supported receiving implementation; verify launch acknowledgement or selected-helper observation, canonical/subquest routing, branch behavior, panel exposure, and already-active detection. Never treat PluginManager.startPlugin or successful event posting as quest selection.
- [x] 6.2 Wire the quest's Open button to the bridge and render absent, disabled, unsupported, incompatible, logged-out, and failed-launch states without false success.
- [ ] 6.3 Add OptimalQuestGuideConfig with @ConfigGroup and a default-true @ConfigItem resumeQuestOnLogin, handled through this group's ConfigChanged events; persist the actual account's confirmed resume intent in this plugin's RS-profile configuration and add Clear remembered quest. Turning resume off cancels pending automatic launches; turning it on applies at the next login. Settings, cancellation and versioned confirmation storage are implemented and locally tested; confirmed intent still awaits a working distributed bridge.
- [ ] 6.4 Implement login readiness coordination, bounded retries, completion checks, account isolation, and duplicate/hop suppression while respecting a conflicting active helper. Implemented with a 50-game-tick budget and local adapter tests; real resume acceptance remains gated by 1.1/6.6.
- [ ] 6.5 Test launch confirmation failure, settings-page discoverability, default resume, off/on preference persistence across restart, canceling pending resume, manual clicks while resume is off, disabled/cleared/completed intent, account switching, a stale response from a previous live account, duplicate login events, and dependency unavailability. ResumeCoordinatorTest and PluginLifecycleTest cover local policy, persisted-store reconstruction and event wiring; actual RuneLite restart/resume with distributed Quest Helper remains unverified.
- [ ] 6.6 Verify one-click quest/subquest activation and logout/login resume with the distributed Quest Helper build and actual Hub class-loader isolation; record versions and selected-helper evidence. A shared-development-classpath run, copied plugin classes, mocks, or manual opening does not satisfy this task.

## 7. Open Theoatrix training guides

- [x] 7.1 Implement the verified skill URL map and aliases from 1.4, compound-row skill selection, HTTPS/host checks, and explicit all-guides fallback.
- [x] 7.2 Wire training actions to the system browser with opening-failure recovery and no changes to quest activation or completion state.
- [x] 7.3 Test known/missing mappings, unsafe destinations/redirects, compound training, reward-hand-in distinction, browser failures, and unchanged progress after navigation; manually verify representative destination pages.

## 8. Validate and document the complete feature

- [x] 8.1 Run the Gradle build and parser, progress, persistence, registry, UI, link, and bridge lifecycle tests; record resolved tool/client/dependency versions and use existing core RuneLite plugins to review API/threading patterns as recommended by the Developer Guide.
- [x] 8.2 Verify real WikiSync STANDARD lookup including a name containing a space, installed/enabled prerequisite handling, and live quest/skill updates, plus cached/offline operation and a changed wiki fixture retaining manual progress, without opening a wiki browser tab. Public HTTP and local regression evidence are supplemented by the user's 2026-09-12 in-game confirmation: "Yes. Sync works."
- [x] 8.3 Document installation/development setup, required WikiSync and Quest Helper plugins, supported Quest Helper versions, the WikiSync STANDARD endpoint/name normalization, manual activity semantics, refresh/cache behavior, the resume on/off option in plugin settings, attribution, and remaining limitations.
- [x] 8.4 Review every capability scenario against evidence; leave WikiSync lookup or handoff/resume tasks unchecked until their real integrations work, and do not describe degraded operation as full implementation.
- [x] 8.5 Run strict OpenSpec validation and git diff --check, and align all task checkboxes with demonstrated results before declaring implementation complete.
- [x] 8.6 Validate the Plugin Hub packaging path: standard-build compatibility or documented custom-build dependency verification, JAR resource loading via getResourceAsStream, no bundled prerequisite classes, and separate code/content notices. Document any local-versus-Hub differences without publishing or claiming maintainer approval.

## 9. Apply in-game screenshot feedback

- [x] 9.1 Enlarge guide text throughout, including details and navigation, and make the top progress bar taller with a readable percentage.
- [x] 9.2 Make each card's title and body toggle details with mouse/keyboard while keeping Open, source/training-link, and manual-check actions independent.
- [x] 9.3 Place quest-action feedback immediately above the selected card and preserve stable rendering.
- [x] 9.4 Implement the explicitly requested Quest Helper tab/search fallback when no launch receiver exists, including verified subquest search aliases, hidden-search recovery, dependency/layout checks, and no false activation or resume confirmation.
- [x] 9.5 Verify the revised narrow sidebar visually, test card interactions/search/lifecycle/scroll regression, and update usage and evidence documentation. Full build passes with 41 tests; previews were inspected at 242 × 820.
- [x] 9.6 On a user-requested search with exactly one visible quest result, press its enabled arrow once through the native listener; preserve setup prompts, zero/multiple matches, hidden/disabled/layout checks and confirmation-only resume storage. Covered by QuestHelperSearchTest and IntegrationTest.
- [x] 9.7 Move Quest Helper activation to a compact Open button with its registered sidebar icon beside the step number/type; make the title toggle details with the card body. Verify action separation, icon refresh/removal and narrow layout.
- [x] 9.8 Create an original plugin sidebar icon, package its transparent PNG, and verify small-size readability and JAR loading. Gold map/blue route artwork was inspected at 16 and 32 pixels; the full build passes all 45 tests including PNG decoding and alpha checks from the JAR.
- [x] 9.9 Rename the visible plugin, panel header, metadata, distribution and usage documentation to QuestCape while preserving existing configuration and progress identities. Verified the sidebar preview and packaged metadata in questcape-0.1.0.jar; all 45 tests pass.
