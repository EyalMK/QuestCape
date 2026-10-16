## MODIFIED Requirements

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
- **THEN** it completes from the same evaluated target used by its comparison, without classifying display text again or treating its purpose as another requirement

## ADDED Requirements

### Requirement: Explanatory training prose preserves verified targets
The plugin SHALL correctly interpret the three Prayer instructions 33→37 for Protect from Magic, 38→40 for Protect from Missiles and 41→43 for Protect from Melee, and Farming 41→46 followed by advice about working on two other skills while crops grow. Fresh and supported legacy-cached forms SHALL separate required targets from reviewed purpose/advice spans, preserve full original prose/destinations and retain typed lower/upper bounds. Structured skill/upper attributes SHALL reinforce action evidence, not override a hand-in or a conflicting requirement. The repair SHALL NOT ignore arbitrary trailing text, omit extra required actions/unknown skills or turn every level-bearing activity into training. Row numbers SHALL NOT be semantic exceptions.

#### Scenario: Completed prayer milestones
- **WHEN** each of the three reported Prayer instructions is evaluated at real Prayer 50
- **THEN** each row is Complete with current level 50 and 100%, includes only its required Prayer target and retains the complete purpose text/link

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
