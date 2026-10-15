## ADDED Requirements

### Requirement: Evidence-based action classification
The source adapter SHALL preserve bounded sanitized row attributes, originating column/cell roles, safe semantic links and structured skill evidence with the original source content. Central classification SHALL reconcile supported identity and action candidates, expose typed matched/unsupported/ambiguous outcomes and reasons, and use narrow text fallback only when justified. Source metadata SHALL be a hint rather than an automatic completion authority. Notes, projections, arbitrary full-width layout and absence of a start prefix SHALL NOT establish action identity or stage. Structural candidate failure SHALL retain last-good content; valid unsupported rows SHALL remain visible and unknown.

#### Scenario: Wording changes with compatible evidence
- **WHEN** display wording changes while recognized source identity and requested action evidence remain compatible
- **THEN** the same typed action and policy are produced without needing an exact title match

#### Scenario: Unrelated note or conflicting identity
- **WHEN** a note links an unrelated quest, or action-cell identity contradicts a supported row-ID identity
- **THEN** the note does not replace action identity and the conflicting supported candidates produce ambiguity rather than first-match completion

#### Scenario: Hand-in has skill metadata
- **WHEN** Herbi Flax's hand-in carries Herblore/level metadata
- **THEN** it remains a hand-in requiring its authoritative fact, not a level-completion action

#### Scenario: Malformed optional evidence
- **WHEN** a miniquest-start row contains malformed numeric skill metadata but independent supported stage/identity evidence
- **THEN** that attribute is rejected and the verified start meaning is preserved without inventing a target

#### Scenario: Invalid table or unfamiliar action
- **WHEN** required canonical columns duplicate or spans are invalid, or an otherwise valid row has unfamiliar semantics
- **THEN** structural invalidity rejects the candidate while unfamiliar semantics retains a visible unknown row with original content and typed reason

### Requirement: Prepared immutable row semantics
The classifier/assembler SHALL supply typed identity, requested stage/action, completion policy/operands, manual eligibility, ambiguity and annotation/training evidence before row construction. GuideRow SHALL only validate consistency and copy prepared values; neither construction nor isManual SHALL classify titles or call activity recognizers. Completion SHALL dispatch through explicit closed policies rather than key substring inspection or cross-enum name conversion. Domain policy SHALL remain independent of Client, Swing, browser, scheduler and storage access. A single evaluation SHALL feed styling, filters, totals and Continue.

#### Scenario: Construct or render a row
- **WHEN** an already classified row is constructed and rendered repeatedly
- **THEN** semantic identity/manual eligibility does not require reparsing display text or consulting activity recognizers

#### Scenario: Invalid policy operands
- **WHEN** an assembler attempts a manual-Kudos policy with a diary operand or otherwise incompatible identity/stage
- **THEN** controlled invariant validation rejects the combination instead of constructing contradictory semantics

#### Scenario: Start and finish
- **WHEN** the same miniquest is in progress and has separate start/finish rows
- **THEN** explicit stage policy completes only the start and does not search the persisted key for a start suffix

### Requirement: Typed runtime identity and compatible codecs
The plugin SHALL use existing API Skill/Quest enums and small typed values for derived combat, extra miniquests, diary actions, balloons and exceptional facts. Source aliases, normalization and legacy storage codecs SHALL have distinct owners. Runtime evaluation SHALL NOT concatenate or inspect legacy syntax such as DIARY:, :start or :repeated:. Codecs SHALL preserve exact account/mode/config tokens and both eligible Kudos keys, retain unknown/retired saved keys inertly and reject ambiguous alias mappings. Changing enum/display names SHALL NOT rename serialized identities implicitly.

#### Scenario: Legacy row and manual records
- **WHEN** old schema-1 rows and account/manual records are decoded and reserialized
- **THEN** compatible stored tokens and both Kudos keys survive, retired keys cannot authorize automatic completion, and source-order/repetition changes do not merge distinct actions

#### Scenario: Alias collision
- **WHEN** two definitions claim the same reviewed normalized source alias for different identities
- **THEN** definition validation reports a controlled conflict rather than choosing the last definition

### Requirement: Role-specific text interpretation
Training requirements, guide projections and skill mention annotation SHALL use shared catalog/tokenization and separate pure role-specific interpreters. Fresh structured spans SHALL be preferred when valid; supported legacy grammar SHALL remain available offline. Annotators SHALL preserve original wording/Unicode offsets, aliases and unknown content. Projection/advice skill mentions SHALL NOT become completion requirements. Complex action semantics SHALL NOT be implemented as an ever-growing whole-prose regex or a universal interpreter framework without a real shared contract.

#### Scenario: Shared alias with different roles
- **WHEN** Runecrafting or Defense appears in a training action, projected-level field and advice
- **THEN** canonical skill recognition is consistent while only the action's verified required target can affect completion

#### Scenario: Unknown annotated projection
- **WHEN** a projection includes unfamiliar annotation that cannot be safely fully represented
- **THEN** readable original source survives without presenting a confident incomplete projection or altering observed progress

### Requirement: Cohesive semantic constants and exhaustive policies
Meaningful protocol, domain, storage, timing, UI copy and layout literals SHALL be named in cohesive owning enums/constants even for one use, preferring existing gameval/API constants. Stable reusable finite behavior SHALL have one explicit enum/strategy owner and exhaustive mappings. Ordinary arithmetic/syntactic literals SHALL NOT trigger artificial constants or a universal Constants class. Approximately 300 production lines SHALL guide responsibility review without a hard gate, code compression or test-class splitting.

#### Scenario: Review a meaningful literal or repeated dispatcher
- **WHEN** a changed production path contains a predicate value, wire token, deadline, label or reusable category behavior
- **THEN** its meaning and owner are explicit, exact persisted/game semantics are retained and repeated consumer dispatch is removed without speculative abstractions
