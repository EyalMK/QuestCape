## Why

Following the OSRS optimal quest guide currently requires switching between a browser table and RuneLite's Quest Helper. A RuneLite companion plugin should put that ordered guide and the player's progress together, launch the selected helper, and restore an unfinished quest after login.

## What Changes

- Create **QuestCape**, a RuneLite sidebar plugin for the **Old School RuneScape Quest Guide** table on [Optimal quest guide](https://oldschool.runescape.wiki/w/Optimal_quest_guide), including interleaved training, miniquest, diary, unlock, and other activity rows in source order.
- Preserve the screenshot's completion semantics: green completed rows with a checkmark, clearly distinguishable incomplete and unknown states, and accessible guide details within the sidebar.
- Require WikiSync alongside Quest Helper. Automatically sync the established current player's progress on login and provide a **Sync** icon using `https://sync.runescape.wiki/runelite/player/{player_name}/STANDARD`, replacing spaces with underscores. Arbitrary-player lookup is removed at the user's request. Live account observations take precedence; unavailable or stale server data remains explicit.
- Track quests from authoritative quest state, training from actual unboosted levels, and otherwise unverifiable activities through account-scoped manual completion.
- Open supported quest rows directly in Quest Helper and automatically reopen the last selected unfinished quest after the same account logs back in. Add a persistent **Resume quest on login** on/off option in this plugin's RuneLite settings page, enabled by default.
- Fetch and cache guide content at runtime so ordinary wiki edits do not require a plugin release. Preserve progress across guide refreshes.
- Open the relevant Theoatrix skill guide in the system browser when a training row is clicked, with a clearly identified all-guides fallback when no specific guide can be resolved.
- Preserve separate start and finish steps for the same quest. Add fixed, position-aware top/bottom and next-step navigation, compact refresh icons, and stable scrolling during live updates.
- Apply the in-game screenshot feedback with larger typography and progress bar, whole-card detail toggling, and quest messages immediately above their card. When the launch receiver is unavailable, open the existing Quest Helper tab and fill its search field. Press the result's own arrow when exactly one result is displayed; leave multiple matches for manual choice.
- Keep scope to the standard optimal quest guide. Alternative Ironman/free-to-play routes, route optimization, replacement quest walkthroughs, gameplay automation, hosted accounts/backends, and Plugin Hub publication are outside this change.

## Capabilities

### New Capabilities

- `optimal-guide-content`: Runtime retrieval, validation, caching, and sidebar presentation of the ordered wiki table.
- `guide-progress-tracking`: Current-player WikiSync and live synchronization, completion rules, and account-scoped persistence.
- `quest-helper-handoff`: Supported quest activation and default-on resume after login, with explicit dependency handling.
- `training-guide-links`: Skill-specific Theoatrix browser navigation and safe missing-link fallback.

### Modified Capabilities

None. This repository has no existing implementation or canonical specifications.

## Impact

- Introduces a Java/Gradle RuneLite plugin project, Swing panel, network adapters, parser, persistence, and tests in the currently empty repository.
- Depends on RuneLite APIs and a verified Quest Helper interoperability contract. The proposed [Quest Helper launch hook, PR #2756](https://github.com/Zoinkwiz/quest-helper/pull/2756), is open and unmerged as checked on 2026-09-12; direct launch and resume are release gates, not assumed working features.
- Reads OSRS Wiki content and opens Theoatrix links. WikiSync is a required plugin and the selected lookup service; use its specified STANDARD player endpoint, with response mapping verified during implementation. WikiSync owns its synchronization/upload behavior; this companion adds no separate account uploads.
- Adds wiki attribution, cached-content provenance, and explicit unavailable-provider states. No external service changes or maintainer communications are performed by this proposal.
