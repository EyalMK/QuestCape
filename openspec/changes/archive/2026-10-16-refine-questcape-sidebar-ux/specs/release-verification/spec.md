## MODIFIED Requirements

### Requirement: Coordinated semantic versioning
Historical release evidence SHALL retain its original version context: automatic completion `1.0.0` from `0.1.0`, compatible navigation `1.1.0`, removal of remaining non-Kudos manual controls `2.0.0`, replacement of within-group multi-selection `3.0.0`, and acceptance corrections `3.0.1`. The compatible sidebar/typed-training implementation candidate SHALL increment `3.0.1` to `3.1.0`, provided additive cache compatibility and existing settings/public interfaces are preserved. Proposal-only work SHALL NOT change implementation version metadata. Build and plugin metadata, ordinary distribution artifact, packaging verification, and current release documentation SHALL agree when implementation occurs. The durable repository policy SHALL require major for breaking behavior/data/public API, minor for compatible features, and patch for compatible fixes, selecting the highest applicable increment once and explaining it. A discovered incompatible migration SHALL require an explicit revised version/migration plan rather than silently shipping under `3.1.0`.

#### Scenario: Requested major release
- **WHEN** historical evidence for the earlier automatic-completion and acceptance-correction releases is inspected
- **THEN** its recorded version remains unchanged rather than being rewritten to the new sidebar candidate version

#### Scenario: Future compatible feature
- **WHEN** this change adds approved sidebar controls and training-range progress while preserving data/settings compatibility
- **THEN** build, packaged metadata, verification and current release documentation report `3.1.0`

#### Scenario: Future compatible fix
- **WHEN** a later release contains only backward-compatible fixes
- **THEN** its patch version increments

#### Scenario: Future breaking change
- **WHEN** a later release breaks existing behavior, data compatibility, or public API
- **THEN** its major version increments and minor and patch reset to zero

#### Scenario: Proposal-only preparation
- **WHEN** the OpenSpec artifacts are created and validated before implementation
- **THEN** source behavior and existing `3.0.1` build/plugin metadata remain unchanged

### Requirement: Human in-game acceptance gate
After implementation, the agent SHALL offer to launch RuneLite with `./gradlew run` from the plugin root, provide the official Using Jagex Accounts link, and give the focused sidebar/training checklist. Only the user SHALL interact with RuneScape. The checklist SHALL cover header actions and provenance, quiet floating tooltip/focus, search clear versus reset and zero matches, unchanged filters, completed/incomplete/in-progress detail examples, source content/link retention, selection-safe whole-card disclosure, real-level interval/compound/missing evidence, native icons/wrapping, footer boundaries/Continue, and live updates without unsolicited movement. Relevant isolation/restart/legacy/automatic completion checks SHALL remain regressions where affected. Controlled unsaved/unavailable/stale-callback behavior SHALL have clearly labeled automated/native-preview evidence, without requiring the user to create irreversible game states. Ubuntu freeze diagnosis and stability acceptance SHALL remain in the separately authorized diagnosis change. The implementation SHALL remain awaiting acceptance until the user explicitly confirms required behavior for the tested source revision. A clean JVM start SHALL NOT count as a pass.

#### Scenario: Acceptance is pending
- **WHEN** automated checks finish but the user has not completed the agreed in-game checklist
- **THEN** the agent offers the development launch and checklist, does not claim runtime completion, and does not open a source PR

#### Scenario: User confirms on tested revision
- **WHEN** the user confirms the current candidate's agreed in-game acceptance tests
- **THEN** evidence is recorded for that source revision and final rules verification begins, without claiming the separate Ubuntu issue resolved

#### Scenario: Functional change after acceptance
- **WHEN** a fix after acceptance changes tested runtime behavior
- **THEN** affected automated and user acceptance checks are repeated before the release gate is satisfied

## ADDED Requirements

### Requirement: Local sidebar proposal and fresh publication authorization
This sidebar proposal and subsequent candidate preparation SHALL use a local private-development branch from the requested master baseline without pushing, creating PRs, merging, changing repository visibility or updating Plugin Hub as part of proposal generation. Historical source/Hub delivery instructions for prior releases SHALL NOT authorize delivery of this candidate. Any later publication SHALL require a fresh explicit user request, visibility/destination verification, recorded user acceptance, final developer-rules verification and the applicable existing source/Hub gates. Proposal validation SHALL be distinguished from implementation tests and user gameplay acceptance.

#### Scenario: Proposal artifacts are ready
- **WHEN** the proposal, design, five capability delta specs and implementation tasks validate
- **THEN** they are available on the local branch, implementation remains unstarted and no remote publication occurs

#### Scenario: Prior release delivery instruction exists
- **WHEN** old release documentation contains an instruction to archive, merge or update Hub
- **THEN** that instruction is treated as historical and does not trigger those actions for this new candidate
