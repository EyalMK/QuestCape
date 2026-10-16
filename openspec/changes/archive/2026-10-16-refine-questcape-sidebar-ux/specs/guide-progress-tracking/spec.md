## MODIFIED Requirements

### Requirement: Completion reflects the activity
The plugin SHALL distinguish finished, in-progress, incomplete, unknown, and informational states. Pure training SHALL use real levels and the typed interval/compound-target policy specified below. Diaries, miniquests and supported unlocks/hand-ins SHALL use authoritative automatic detection and SHALL NOT offer or accept manual completion overrides. Only the two Varrock Museum Kudos actions SHALL retain reversible labeled manual checks, as clarified by the user on 2026-10-12. The plugin SHALL NOT infer a hand-in or unlock from later quests, guide projections, or achieved levels. Missing or unsupported automatic evidence SHALL remain unknown rather than being treated as completed or incomplete. Card styling, state filters, summary and Continue SHALL consume the same evaluated completion state.

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
- **WHEN** a quest has no explicit canonical mapping
- **THEN** its completion remains unknown

#### Scenario: Reverse manual check
- **WHEN** a manual check for an eligible Kudos action is cleared
- **THEN** only that account's activity record changes

#### Scenario: Unsupported future automatic activity
- **WHEN** a newly introduced diary or miniquest has no verified detector
- **THEN** it remains visibly unknown without a checkbox or manual completion fallback

#### Scenario: Ordinary diary object hand-in
- **WHEN** a guide row asks to hand in the diary of Herbi Flax
- **THEN** it remains a hand-in activity rather than being interpreted as an achievement diary tier

## ADDED Requirements

### Requirement: Typed training intervals and source evidence
The guide boundary SHALL preserve supported training skill identities, upper targets, optional explicit lower targets and original instruction skill-name tokens before presentation. Structured source evidence SHALL take precedence over narrow legacy instruction fallback; conflicting or unsupported evidence SHALL remain unresolved without guessed targets. Parenthetical recommendations, guide projected levels and reward/hand-in text SHALL NOT establish required pure-training intervals. Swing SHALL NOT reparse rendered titles, classify English source headers, or read Client to recover this meaning. Canonical skill aliases and supported bounds SHALL be shared with the existing skill interpretation; combat SHALL remain an explicitly derived target.

#### Scenario: Explicit range survives parsing
- **WHEN** a supported pure-training instruction says to train Woodcutting from level 52 to level 55
- **THEN** its typed target retains Woodcutting, lower 52 and upper 55, and its original skill-name token is available for local icon insertion

#### Scenario: Target-only or advice evidence
- **WHEN** training establishes only an upper target and contains parenthetical level advice
- **THEN** no lower target is invented from that advice or from projected-level metadata

#### Scenario: Changed or contradictory source
- **WHEN** a source instruction has conflicting required levels, unknown skills or unsupported action meaning
- **THEN** its full text remains readable, ambiguous required targets remain unresolved, and completion cannot become complete by silently dropping them

### Requirement: Real-level interval completion and comparison
For a supported pure-training X→Y interval with Y>X and observed real level L, the per-target state SHALL be incomplete for L<X, in progress for X≤L<Y, and complete for L≥Y. Its comparison SHALL display `Current level: {L} - {P}%`, with P equal to `floor(100 * clamp((L-X)/(Y-X), 0, 1))` using non-integer division. This percentage SHALL represent level-interval position, not XP or time. Without a supported lower bound, known L SHALL display `Current level: {L}` and evaluate only the supported upper target. Missing or invalid observations SHALL display `Current level: unavailable`, never zero. Invalid X≥Y SHALL omit percentage and evaluate a separately supported upper target only. Current levels and completion SHALL derive from the same immutable account observation, including the existing real-level combat derivation policy. The existing status text/symbols and manual/unknown/information distinctions SHALL remain unchanged.

#### Scenario: At the base and inside the interval
- **WHEN** a 52→55 Woodcutting interval has real level 52 or 54
- **THEN** it displays `◐ In progress` with `Current level: 52 - 0%` or `Current level: 54 - 66%` respectively

#### Scenario: Below and beyond the interval
- **WHEN** a 60→65 Defence interval has real level 59, 65 or 70
- **THEN** it displays `○ Incomplete` with 0%, `✓ Complete` with 100%, or `✓ Complete` with 100% respectively, retaining the actual observed level in its comparison

#### Scenario: Missing level or lower target
- **WHEN** the level is unavailable, or a supported upper target has no lower target
- **THEN** the comparison omits percentage; missing observation remains unknown, while a known real level evaluates the upper goal without an invented base

#### Scenario: Degenerate interval
- **WHEN** lower and upper bounds are equal or reversed
- **THEN** no division or percentage is performed, and only independently supported upper-goal evidence is eligible for evaluation

#### Scenario: Source projections disagree with observation
- **WHEN** guide projected levels or a temporary boost exceed the current real level
- **THEN** neither changes the training state or current-level percentage

### Requirement: Conservative compound training progress
Compound training SHALL show a named comparison for every required skill in source order without averaging percentages. The aggregate SHALL be complete only if every required target is recognized, observed and complete. Otherwise it SHALL be in progress if any supported interval is in progress or a complete required target coexists with a known unfinished required target; otherwise incomplete if a known required target is below its goal; otherwise unknown. Missing/unresolved required targets SHALL prevent complete, remain explicitly unavailable and SHALL NOT suppress a separately established in-progress/incomplete fact. A complete target plus only missing targets SHALL aggregate to unknown. The aggregate SHALL feed the same styling, filter, counts and unfinished navigation as all other completion results.

#### Scenario: One achieved and one known unfinished target
- **WHEN** Attack's required goal is complete and Strength's observed level is below its goal
- **THEN** the compound row is in progress with separate labeled comparisons

#### Scenario: Partial observations
- **WHEN** one interval is in progress and another required level is missing
- **THEN** the aggregate is in progress, the missing comparison says unavailable, and the row is not complete

#### Scenario: Complete target and missing requirement
- **WHEN** all observed targets are complete but another required target is unavailable or unresolved
- **THEN** the aggregate remains unknown and exposes the missing evidence

#### Scenario: Below-base and missing requirement
- **WHEN** one observed required target is below its base and every other target is missing
- **THEN** the row is incomplete with explicit unavailable comparisons and no fabricated overall percentage

#### Scenario: All required goals met
- **WHEN** every recognized required goal has an observed real level at or above its target and no unresolved required target remains
- **THEN** the row is complete, independent of whether every target had a lower bound

### Requirement: Additive legacy guide evidence compatibility
Training and field-role enrichment SHALL be optional, independently versioned and compatible with schema-1 legacy guide snapshots. The plugin SHALL retain legacy title/fields/links/upper-target data and existing account-cache/config/key semantics. Worker-side offline reclassification SHALL reconstruct only meaning supported by retained evidence; missing enrichment SHALL NOT require network retrieval or make a valid legacy route unbrowseable. Unknown enrichment versions SHALL fall back to retained legacy evidence without destroying originals. Equivalent evidence SHALL retain stable keys, source order, duplicate/stage handling, safe title repairs and eligible Kudos checks. Derived completion and percentages SHALL NOT be persisted as account facts. Unsupported or corrupt original cache records SHALL retain the existing preservation policy.

#### Scenario: Offline legacy cache
- **WHEN** a valid pre-change cache contains an explicit training interval in its retained instruction but has no enriched row evidence
- **THEN** it is browsable offline and obtains the same supported target/state as an equivalent fresh row without resetting account progress

#### Scenario: Legacy cache lacks interval or link role
- **WHEN** retained data establishes only an upper target or ambiguous link roles
- **THEN** upper-target completion remains available, percentage or primary link is omitted where unsupported, and raw content/destinations remain accessible

#### Scenario: Cross-version snapshot readability
- **WHEN** an enriched snapshot is read through the legacy schema-1 reader
- **THEN** its original row data remains usable and account/manual records are unchanged

#### Scenario: Unknown enrichment version
- **WHEN** an otherwise valid snapshot contains an unsupported evidence version
- **THEN** the plugin uses supported retained legacy evidence, preserves the file and does not invent new semantics
