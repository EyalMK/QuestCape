## MODIFIED Requirements

### Requirement: Live account progress
The plugin SHALL read enum-covered quest and miniquest progress through `Quest.getState(Client)`, real skill levels through `Client.getRealSkillLevel(Skill)`, and supported diary and additional miniquest facts through documented public RuneLite APIs and named gameval constants. For a classified full quest absent from the enum, the plugin SHALL resolve a unique normalized title in the local quest database, require valid start and end thresholds, and read the state through RuneLite's quest status script. Reads SHALL occur on ClientThread after the login's first game tick and established account/profile/mode readiness. Observations SHALL apply only to the established current account. Character progress SHALL remain local without WikiSync or character HTTP requests. Explicit Sync SHALL request a fresh local observation even when values are unchanged.

#### Scenario: Quest completes during play
- **WHEN** the current account's quest state becomes finished
- **THEN** its rows become green and checkmarked without another Sync

#### Scenario: A new full quest is absent from the RuneLite enum
- **WHEN** a classified quest has a unique normalized title match in the local game quest table with valid progress thresholds
- **THEN** the plugin reads its state through RuneLite's quest status script on ClientThread

#### Scenario: Non-standard profile
- **WHEN** a non-standard game-mode profile becomes active
- **THEN** it uses only its scoped live/local observations and does not inherit another mode's progress

#### Scenario: Explicit local refresh
- **WHEN** a ready logged-in character activates Sync
- **THEN** the plugin reads fresh local progress and updates its observation time without a character network request

#### Scenario: Logged out or profile unready
- **WHEN** login, local player, account hash, or profile/mode readiness is missing
- **THEN** the plugin leaves current character progress unavailable, disables character Sync, and keeps cached route browsing usable

### Requirement: Completion reflects the activity
The plugin SHALL distinguish finished, in-progress, incomplete, unknown, and informational states. Pure training SHALL use real levels and the typed interval/compound-target policy specified below. Diaries, miniquests and supported unlocks/hand-ins SHALL use authoritative automatic detection and SHALL NOT offer or accept manual completion overrides. Only the two Varrock Museum Kudos actions SHALL retain reversible labeled manual checks, as clarified by the user on 2026-10-12. The plugin SHALL NOT infer a hand-in or unlock from later quests, guide projections, or achieved levels. Missing or unsupported automatic evidence SHALL remain unknown rather than being treated as completed or incomplete. Card styling, state filters, summary and Continue SHALL consume the same evaluated completion state.

Completion SHALL consume prepared typed action/policy operands, including explicit start/finish and manual eligibility, instead of parsing persisted-key strings, classifying row titles or converting enums by matching names. Supported explanatory purpose/advice SHALL NOT invalidate an otherwise verified pure-training target. Unknown SHALL expose a typed missing/unsupported/ambiguous reason independently of its user-facing wording.

#### Scenario: Boosted training level
- **WHEN** a temporary boost reaches a training target but the real level does not
- **THEN** the row is not complete, and its incomplete or in-progress state and percentage use only the real level

#### Scenario: Repeated skill milestones
- **WHEN** the real level reaches one milestone but not a later one
- **THEN** only the reached milestone is complete, and each remaining supported interval evaluates independently

#### Scenario: Reward hand-in
- **WHEN** a hand-in mentions an already reached level without a verified hand-in predicate
- **THEN** its state remains unknown with no checkbox unless it is an explicitly eligible Kudos action

#### Scenario: Unmapped quest
- **WHEN** a quest has no valid enum match or supported local game observation
- **THEN** its completion remains unknown

#### Scenario: Classified new quest lacks a usable local observation
- **WHEN** its local title match is missing or ambiguous, or the quest status observation is invalid
- **THEN** the row remains in the Quests filter and shows Progress unavailable

#### Scenario: Reverse manual check
- **WHEN** a manual check for an eligible Kudos action is cleared
- **THEN** only that account's activity record changes

#### Scenario: Unsupported future automatic activity
- **WHEN** a newly introduced diary or miniquest has no verified detector
- **THEN** it remains visibly unknown without a checkbox or manual completion fallback

#### Scenario: Ordinary diary object hand-in
- **WHEN** a guide row asks to hand in the diary of Herbi Flax
- **THEN** it remains a hand-in activity rather than being interpreted as an achievement diary tier

#### Scenario: Prepared semantics and explanatory prose
- **WHEN** a classified pure-training row has a known achieved target and only a supported explanatory suffix
- **THEN** it completes from the same evaluated target used by its progress circle, without classifying display text again or treating its purpose as another requirement
