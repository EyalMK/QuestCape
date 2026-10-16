# guide-progress-tracking Specification

## Purpose
Track current-character quests, miniquests, diaries, training, unlocks, hand-ins, and eligible Kudos checks through authoritative local observations and isolated saved progress.

## Requirements

### Requirement: Live account progress
The plugin SHALL read current quest and API-covered miniquest progress through `Quest.getState(Client)`, real skill levels through `Client.getRealSkillLevel(Skill)`, and supported diary and additional miniquest facts through documented public RuneLite APIs and named gameval constants. Reads SHALL occur on ClientThread after the login's first game tick and established account/profile/mode readiness. Observations SHALL apply only to the established current account. Character progress SHALL remain local without WikiSync or character HTTP requests. Explicit Sync SHALL request a fresh local observation even when values are unchanged.

#### Scenario: Quest completes during play
- **WHEN** the current account's quest state becomes finished
- **THEN** its rows become green and checkmarked without another Sync

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

#### Scenario: Prepared semantics and explanatory prose
- **WHEN** a classified pure-training row has a known achieved target and only a supported explanatory suffix
- **THEN** it completes from the same evaluated target used by its progress circle, without classifying display text again or treating its purpose as another requirement

### Requirement: Durable isolated progress
The plugin SHALL persist profile/mode-scoped observations and eligible manual checks independently from replaceable guide snapshots. Stable actions SHALL survive reordering; changed targets or changed action semantics SHALL be reevaluated. Identical ambiguous repetitions SHALL remain visible without sharing manual checks. Existing caches SHALL remain readable or have an explicit lossless migration. Retired non-Kudos manual keys SHALL NOT affect automatic completion; Kudos keys SHALL survive.

#### Scenario: Reorder and restart
- **WHEN** the route is reordered or the client restarts
- **THEN** eligible manual checks follow stable action identities instead of row numbers, and supported diaries/miniquests are reevaluated automatically when the character is ready

#### Scenario: Changed target or action
- **WHEN** a training target or manual action changes
- **THEN** its completion is reevaluated without inheriting an unrelated record

#### Scenario: Account or mode switch
- **WHEN** another account or mode becomes current
- **THEN** it cannot inherit the previous profile's observations, manual checks, or pending requests

#### Scenario: Upgrade from manually checked diary or miniquest
- **WHEN** an old account cache contains checked diary/miniquest rows and unrelated checked unlocks or hand-ins
- **THEN** authoritative facts determine automatic activity completion and eligible Kudos checks are retained; other retired keys are inert

#### Scenario: Stale manual callback
- **WHEN** an old UI callback attempts to toggle any non-Kudos action after upgrade
- **THEN** the service rejects that toggle without changing its automatic state or writing a new manual override

### Requirement: Honest summary
The plugin SHALL count completed actionable rows and show unknown rows separately. Informational rows SHALL NOT inflate the actionable total.

#### Scenario: Partial knowledge
- **WHEN** some actions lack sufficient evidence
- **THEN** they remain counted as unknown, not completed

### Requirement: Automatic unlocks and hand-in
Each currently represented balloon destination SHALL use its distinct named gameval flag equal to one. Stronghold completion SHALL require all three coin-floor reward flags, without requiring fourth-floor boots. Museum Camp SHALL require Peter's camp reward flag, without inferring it from Bone Voyage or construction. Herbi Flax SHALL require the verified hand-in bit in MORTTONMULTI. Unsupported actions SHALL remain unknown without manual controls. Existing non-Kudos checks SHALL NOT override these facts.

#### Scenario: Independent balloon routes
- **WHEN** Varrock's route is unlocked and Crafting Guild's is not
- **THEN** only Varrock's unlock step completes, and both display their full destinations

#### Scenario: Reward boundaries
- **WHEN** only two Stronghold coin rewards are collected or camp improvements exist without Peter's reward
- **THEN** the respective step remains incomplete

#### Scenario: Hand-in evidence
- **WHEN** Shades of Mort'ton is finished and Herblore is high but the hand-in bit is unset
- **THEN** the Herbi Flax step remains incomplete

### Requirement: Automatic diary detection
The plugin SHALL explicitly map supported achievement diary rows to their region, tier, and intended action. Completion SHALL use verified authoritative game state and named gameval constants. Every diary row present in the captured/current route at implementation SHALL have a verified detector. Tier/task completion and reward collection SHALL remain distinct when the guide requests distinct actions. A later tier, higher skill, or completed prerequisite quest SHALL NOT substitute for the required fact.

#### Scenario: Tier completes during play
- **WHEN** authoritative state for a supported diary region/tier changes from incomplete to complete
- **THEN** its completion row updates automatically after the coalesced live reading, without clicking Sync or manually checking it

#### Scenario: Region and tier isolation
- **WHEN** one region's easy tier is complete while its medium tier and another region's easy tier are incomplete
- **THEN** only rows whose explicit predicates are satisfied are completed

#### Scenario: Reward has not been collected
- **WHEN** tasks are complete but a separately requested diary reward has not been collected
- **THEN** the task/tier completion row is complete and the reward-claim row is not falsely completed

#### Scenario: Existing completed diary at login
- **WHEN** the established character logs in with a supported diary already completed
- **THEN** its row is automatically completed after readiness with no need to reopen the game's diary interface

#### Scenario: Legacy Karamja completion encoding
- **WHEN** Karamja Easy, Medium or Hard's named ATJUN_DONE flag is 2
- **THEN** that tier completes automatically; values 0, 1 and 3 do not complete it, reward facts remain separate, and Elite still requires its own flag equal to 1

### Requirement: Automatic miniquest detection
The plugin SHALL detect every miniquest row in the captured/current route through `Quest` state or a documented authoritative predicate using named gameval constants. Coverage SHALL include Natural history quiz and Knight Waves Training Grounds. Start rows SHALL complete once their miniquest is in progress or finished; finish rows SHALL require finished. All miniquest rows SHALL remain free of manual completion controls.

#### Scenario: API-covered miniquest completes
- **WHEN** RuneLite reports an API-covered miniquest as finished
- **THEN** its finish row completes automatically using that state

#### Scenario: Extra miniquest completion
- **WHEN** the verified completion predicate for Natural history quiz or Knight Waves Training Grounds becomes satisfied
- **THEN** the corresponding row completes automatically without a quest-enum identity or manual check

#### Scenario: Start and finish milestones
- **WHEN** In Search of Knowledge is in progress but unfinished
- **THEN** its start row is complete and its finish row remains in progress

#### Scenario: Partial progress is insufficient
- **WHEN** only part of an additional miniquest's verified completion predicate is satisfied
- **THEN** its finish row remains unfinished

### Requirement: Consistent progress presentation
Automatic diary/miniquest state SHALL feed the same completion evaluator used by card styling, completion totals, and next-step navigation. The UI SHALL remove their manual checkbox and manual label while preserving existing category styling, details, and user-controlled scrolling.

#### Scenario: Automatic completion changes the next step
- **WHEN** the earliest unfinished diary or miniquest completes
- **THEN** its card and summary reflect completion and the next-step action targets the next unfinished action without scrolling the viewport automatically

#### Scenario: Unknown automatic row
- **WHEN** a diary/miniquest lacks sufficient current evidence
- **THEN** it contributes to the unknown count, remains eligible as an unfinished next step, and presents no manual completion control

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
For a supported pure-training X→Y interval with Y>X and observed real level L, the per-target state SHALL be incomplete for L<X, in progress for X≤L<Y, and complete for L≥Y. Its percentage P SHALL equal `floor(100 * clamp((L-X)/(Y-X), 0, 1))` using non-integer division. This percentage SHALL represent level-interval position, not XP or time. Without a supported lower bound, a known real level SHALL evaluate only the supported upper target without inventing an interval percentage. Missing or invalid observations SHALL remain unavailable, never zero. Invalid X≥Y SHALL omit percentage and evaluate a separately supported upper target only. Current levels and completion SHALL derive from the same immutable account observation, including the existing real-level combat derivation policy. Training presentation SHALL follow Compact training progress circles in optimal-guide-content: percentage number only at the center, percentage-only tooltip, no current-level/status comparison text, and a full checkmarked ring for verified completion. Non-training status vocabulary and manual/unknown/information distinctions SHALL remain unchanged.

#### Scenario: At the base and inside the interval
- **WHEN** a 52→55 Woodcutting interval has real level 52 or 54
- **THEN** the domain evaluates In progress at 0% or 66% respectively, and the circle shows 0 or 66 with percentage-only hover and no current-level text

#### Scenario: Below and beyond the interval
- **WHEN** a 60→65 Defence interval has real level 59, 65 or 70
- **THEN** the domain evaluates Incomplete at 0%, Complete at 100%, or Complete at 100% respectively; unfinished presentation shows 0 and completed presentation uses the full checkmarked ring

#### Scenario: Missing level or lower target
- **WHEN** the level is unavailable, or a supported upper target has no lower target
- **THEN** no interval percentage is invented; missing observation remains unknown, while a known real level evaluates the upper goal without an invented base and verified completion may show the full checkmarked ring

#### Scenario: Degenerate interval
- **WHEN** lower and upper bounds are equal or reversed
- **THEN** no division or percentage is performed, and only independently supported upper-goal evidence is eligible for evaluation

#### Scenario: Source projections disagree with observation
- **WHEN** guide projected levels or a temporary boost exceed the current real level
- **THEN** neither changes the training state or real-level interval percentage

### Requirement: Conservative compound training progress
Compound training SHALL show a named progress circle for every required skill in source order without averaging percentages. The aggregate SHALL be complete only if every required target is recognized, observed and complete. Otherwise it SHALL be in progress if any supported interval is in progress or a complete required target coexists with a known unfinished required target; otherwise incomplete if a known required target is below its goal; otherwise unknown. Missing/unresolved required targets SHALL prevent complete, remain explicitly unavailable and SHALL NOT suppress a separately established in-progress/incomplete fact. A complete target plus only missing targets SHALL aggregate to unknown. The aggregate SHALL feed the same styling, filter, counts and unfinished navigation as all other completion results.

#### Scenario: One achieved and one known unfinished target
- **WHEN** Attack's required goal is complete and Strength's observed level is below its goal
- **THEN** the compound row is in progress with separate labeled progress circles

#### Scenario: Partial observations
- **WHEN** one interval is in progress and another required level is missing
- **THEN** the aggregate is in progress, the missing target remains visibly unavailable, and the row is not complete

#### Scenario: Complete target and missing requirement
- **WHEN** all observed targets are complete but another required target is unavailable or unresolved
- **THEN** the aggregate remains unknown and exposes the missing evidence

#### Scenario: Below-base and missing requirement
- **WHEN** one observed required target is below its base and every other target is missing
- **THEN** the row is incomplete with explicitly unavailable targets and no fabricated overall percentage

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

### Requirement: Explanatory training prose preserves verified targets
The plugin SHALL correctly interpret the three Prayer instructions 33→37 for Protect from Magic, 38→40 for Protect from Missiles and 41→43 for Protect from Melee, and Farming 41→46 followed by advice about working on two other skills while crops grow. Fresh and supported legacy-cached forms SHALL separate required targets from reviewed purpose/advice spans, preserve full original prose/destinations and retain typed lower/upper bounds. Structured skill/upper attributes SHALL reinforce action evidence, not override a hand-in or a conflicting requirement. The repair SHALL NOT ignore arbitrary trailing text, omit extra required actions/unknown skills or turn every level-bearing activity into training. Row numbers SHALL NOT be semantic exceptions.

#### Scenario: Completed prayer milestones
- **WHEN** each of the three reported Prayer instructions is evaluated at real Prayer 50
- **THEN** each row evaluates Complete from real level 50 and 100%, displays a full checkmarked progress circle without current-level text, includes only its required Prayer target and retains the complete purpose text/link

#### Scenario: Farming advice and observed level
- **WHEN** the reported Farming 41→46 instruction is evaluated at synthetic real Farming 40, 41, 44, 46 or 50
- **THEN** it is respectively Incomplete, In progress, In progress, Complete or Complete with 0%, 0%, 60%, 100% or 100%, preserving the crop advice without inventing the screenshot user's Farming level

#### Scenario: Purpose versus requirement
- **WHEN** Prayer training mentions Protect from Magic, or training additionally requires an unsupported skill or a hand-in
- **THEN** Magic in the prayer purpose is not a required Magic target, while a genuine unresolved required clause continues to prevent false Complete

#### Scenario: Legacy and revised wording
- **WHEN** attributes are absent in a valid retained cache, or harmless whitespace/punctuation/explanation wording changes within supported grammar
- **THEN** the same verified goals evaluate offline without requiring refreshed metadata or reducing the parser to exact screenshot strings

### Requirement: Explicit detector-family observation
Supported exceptional activities SHALL be observed through an explicit immutable detector registry with typed supported keys and duplicate-writer validation. Distinct diary, balloon, Stronghold coins, Museum Camp reward, Herbi Flax, Natural History Quiz and Knight Waves predicate families SHALL have cohesive implementations; repeated tier/destination data SHALL be parameterized rather than one class per entry. API-covered quests SHALL retain the generic Quest reader. Reads SHALL occur on ClientThread under existing account/mode/readiness guards. Per-key unavailable evidence SHALL remain unknown without erasing unrelated known facts. Source recognition SHALL share typed definitions but SHALL NOT occur in detectors.

#### Scenario: Exact predicate parity
- **WHEN** family extraction is evaluated at Karamja 0/1/2/3, independent reward flags, partial Stronghold rewards, camp access without reward, Herbi hand-in bit, quiz stages or partial/final Knight Waves
- **THEN** the existing documented exact automatic outcomes remain, including Karamja Easy/Medium/Hard requiring 2 and Elite requiring its own value 1

#### Scenario: Duplicate key or unavailable family
- **WHEN** two detectors register the same fact key or one family cannot obtain its observation
- **THEN** duplicate definitions fail validation, while unavailable observation is unknown only for affected keys and never becomes false or manual eligibility
