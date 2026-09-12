## ADDED Requirements

### Requirement: Current-player WikiSync
The plugin SHALL automatically request the established current player's latest available progress on login and when the player activates its Sync icon. It SHALL use `GET https://sync.runescape.wiki/runelite/player/{player_name}/STANDARD`, trim surrounding whitespace, replace internal spaces with underscores, and encode the name as one path segment. The UI SHALL NOT offer arbitrary-player lookup. Every explicit Sync SHALL request the API, including when local or cached observations exist.

#### Scenario: Login sync
- **WHEN** the current account and profile become ready after login, and WikiSync is active
- **THEN** one request fetches the current player's STANDARD profile without requiring a wiki browser tab

#### Scenario: Player name contains a space
- **WHEN** the current player's name is `snooze meist`
- **THEN** Sync requests `https://sync.runescape.wiki/runelite/player/snooze_meist/STANDARD`

#### Scenario: Equivalent underscore spelling
- **WHEN** a name uses underscores in place of spaces
- **THEN** it uses the same API path and normalized cache identity

#### Scenario: Explicit refresh
- **WHEN** the player clicks Sync with existing observations
- **THEN** a new API request is made, live observations keep precedence, and retrieval time remains distinct from server observation time

#### Scenario: Logged out
- **WHEN** no current account has been established
- **THEN** current-player Sync is unavailable and cached guide browsing remains usable

#### Scenario: Unavailable or partial profile
- **WHEN** the API reports missing, partial, stale, restricted, throttled, or failed data
- **THEN** the limitation is explicit, missing values remain unknown, and any retained snapshot keeps its original account and timestamps

#### Scenario: Newest current-account request wins
- **WHEN** a response arrives after another Sync, logout, account switch, or dependency disablement
- **THEN** it cannot replace the newer current-account observations

### Requirement: WikiSync prerequisite
The plugin SHALL require WikiSync to be installed, enabled, and active for fresh synchronization. It SHALL provide install/enable guidance, retain labeled cached observations, and add no separate upload mechanism or substitute provider.

#### Scenario: WikiSync missing or disabled
- **WHEN** WikiSync is missing, disabled, or enabled but inactive
- **THEN** fresh Sync is unavailable with a setup explanation while the route and correctly scoped observations remain available

#### Scenario: Recovery after enablement
- **WHEN** WikiSync becomes active during a logged-in session that has not synchronized
- **THEN** current-player synchronization becomes available and requests the specified endpoint

### Requirement: Live account progress
The plugin SHALL read current progress through Quest.getState(Client) and Client.getRealSkillLevel(Skill) on ClientThread after profile readiness. Live observations SHALL take precedence only for the established current account, and STANDARD remote observations SHALL remain separate from non-standard profiles.

#### Scenario: Quest completes during play
- **WHEN** the current account's quest state becomes finished
- **THEN** its rows become green and checkmarked without another Sync

#### Scenario: Non-standard profile
- **WHEN** a non-standard game-mode profile becomes active
- **THEN** it uses only its scoped live/local observations and does not inherit remote STANDARD progress

### Requirement: Completion reflects the activity
The plugin SHALL distinguish finished, in-progress, incomplete, unknown, and informational states. Pure training uses real levels. Unverifiable unlocks, diaries, and hand-ins use reversible labeled manual checks. It SHALL NOT infer a hand-in or unlock from later quests, guide projections, or achieved levels.

#### Scenario: Boosted training level
- **WHEN** a temporary boost reaches a target but the real level does not
- **THEN** the training row remains incomplete

#### Scenario: Repeated skill milestones
- **WHEN** the real level reaches one milestone but not a later one
- **THEN** only the reached milestone is complete

#### Scenario: Reward hand-in
- **WHEN** a hand-in mentions an already reached level without a verified hand-in predicate
- **THEN** its state remains unverified until manually checked

#### Scenario: Unmapped quest
- **WHEN** a quest has no explicit canonical mapping
- **THEN** its completion remains unknown

#### Scenario: Reverse manual check
- **WHEN** a manual check is cleared
- **THEN** only that account's activity record changes

### Requirement: Durable isolated progress
The plugin SHALL persist profile/mode-scoped observations and manual checks independently from replaceable guide snapshots. Stable actions survive reordering; changed targets or changed action semantics are reevaluated. Identical ambiguous repetitions remain visible without sharing manual checks.

#### Scenario: Reorder and restart
- **WHEN** the route is reordered or the client restarts
- **THEN** manual checks follow stable action identities instead of row numbers

#### Scenario: Changed target or action
- **WHEN** a training target or manual action changes
- **THEN** its completion is reevaluated without inheriting an unrelated record

#### Scenario: Account or mode switch
- **WHEN** another account or mode becomes current
- **THEN** it cannot inherit the previous profile's observations, manual checks, or pending requests

### Requirement: Honest summary
The plugin SHALL count completed actionable rows and show unknown rows separately. Informational rows SHALL NOT inflate the actionable total.

#### Scenario: Partial knowledge
- **WHEN** some actions lack sufficient evidence
- **THEN** they remain counted as unknown, not completed
