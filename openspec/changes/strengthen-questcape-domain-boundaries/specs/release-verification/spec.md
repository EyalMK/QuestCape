## MODIFIED Requirements

### Requirement: Coordinated semantic versioning
Historical release evidence SHALL retain its original context: automatic completion 1.0.0 from 0.1.0, navigation 1.1.0, manual-control removal 2.0.0, single-selection 3.0.0, accepted fixes 3.0.1 and the unaccepted sidebar candidate 3.1.0. This compatible combined refactor/UI follow-up SHALL plan 3.2.0 because it adds selection/subsection interactions alongside compatible repairs, provided the public API/data audit establishes compatibility. Any incompatible public contract, data format or intentional identity migration SHALL require an explicit revised major 4.0.0/migration decision before implementing the break. Build/plugin metadata, ordinary artifact, request product identity, packaging assertions and current release docs SHALL agree. Proposal-only work SHALL NOT change implementation metadata. The durable policy SHALL select the highest applicable semantic increment once, reset lower components and explain the choice.

#### Scenario: Historical requested releases
- **WHEN** earlier verification or acceptance evidence is read
- **THEN** its recorded versions remain historical and do not imply acceptance of the new candidate

#### Scenario: Future compatible feature
- **WHEN** this follow-up preserves supported interfaces and stored data while adding the specified interactions
- **THEN** implementation metadata and User-Agent report the planned 3.2.0 candidate consistently

#### Scenario: Future compatible fix
- **WHEN** a later release contains only compatible fixes
- **THEN** its patch version increments

#### Scenario: Future breaking change
- **WHEN** implementation requires an incompatible public API or persisted-data change
- **THEN** the migration/version plan is revised to the appropriate major release before the break is implemented, without shipping it silently as 3.2.0

#### Scenario: Proposal-only preparation
- **WHEN** this proposal and its artifacts are generated
- **THEN** production source, 3.1.0 build metadata and current runtime behavior remain unchanged

### Requirement: Automated verification before user acceptance
Implementation SHALL freshly characterize the actual starting branch, retain existing progress/mapping/navigation/resource guarantees and add tests for evidence mutations, four reported training regressions, typed identity/legacy readback, exact detector boundaries, negative manual-intent recovery, partial startup and original-run/session callbacks, development/JAR metadata and the specified UI interaction regions. Checks SHALL use Java 11 and actual resolved dependencies, ordinary-JAR packaging, controlled clocks/queues/faults and isolated RuneLiteLAF layouts. Evidence SHALL identify the exact tested revision, current counts, production scope and platform limitations rather than reuse historical passes or imply synthetic events tested the game. Live public guide checks SHALL not include player data. No game input SHALL be automated.

#### Scenario: Platform unavailable
- **WHEN** checks run on Windows but Linux or real native scaling is unavailable
- **THEN** evidence records those limitations and leaves the separate Ubuntu freeze investigation unresolved

#### Scenario: Research baseline is stale
- **WHEN** the older research records 72 tests and the prior candidate records 92 tests
- **THEN** implementation reruns the actual current baseline and records its result rather than treating either historic number as a current pass

#### Scenario: Four-row and interaction counterexamples
- **WHEN** the new regression suite exercises supported purpose/advice and an added unsupported required action, plus text/body/projected/subsection/resync clicks
- **THEN** supported rows complete from real levels, extra unknown requirements prevent false completion and each interaction fires only its assigned behavior

### Requirement: Human in-game acceptance gate
After implementation the agent SHALL offer ./gradlew run from the plugin root, provide the official Using Jagex Accounts instructions and request acceptance of the exact combined candidate. Only the user SHALL operate RuneScape. The checklist SHALL cover all four training rows, genuine missing/compound requirements, instant card disclosure and selected hold-Ctrl drag/word selection, release-then-copy and Escape/focus cleanup, independent projected tooltips, related-link default exceptions, advice/QP typography, icon-only resync/spacing, unchanged header/search/filter/footer actions, automatic activity parity and account/lifecycle/scroll stability. Unavailable irreversible states and controlled failures SHALL have clearly labeled synthetic evidence. User findings on the prior candidate SHALL NOT be treated as blanket acceptance, and a successful JVM launch SHALL NOT count as a pass. Functional corrections SHALL require affected checks and acceptance again. Ubuntu diagnosis remains separate.

#### Scenario: Acceptance is pending
- **WHEN** automated checks pass without explicit combined-candidate confirmation
- **THEN** the launch and focused checklist are offered, acceptance remains pending and no delivery/archive is claimed

#### Scenario: User confirms tested revision
- **WHEN** the user confirms the agreed behaviors on the exact corrected candidate
- **THEN** that acceptance is recorded and the then-current official RuneLite/Jagex rules are re-read for the complete production audit before any authorized delivery

#### Scenario: Functional change after acceptance
- **WHEN** a correction changes accepted runtime behavior
- **THEN** affected automated and user checks are repeated before the candidate satisfies the release gate

### Requirement: Separate source and Plugin Hub PR gates
This work SHALL remain local on the private-development branch and SHALL NOT merge into master. Proposal/implementation/acceptance/audit SHALL NOT themselves authorize a push, PR, repository visibility change, Hub update or archive. Historical 2026-10-12 release instructions SHALL apply only to that prior release. Any later publication SHALL require a fresh explicit user instruction, verified destination/visibility, accepted exact candidate and final complete developer-rules audit. A later authorized Hub update SHALL reference the publicly accessible accepted source commit, use the actual plugins/questcape manifest, preserve required warnings and follow current repository conventions. Any created PR SHALL be attached to the chat. Nothing in these gates promises maintainer approval.

#### Scenario: Source PR after verification
- **WHEN** acceptance and final audit pass but no new publication instruction exists
- **THEN** no source PR or push occurs and master remains unchanged

#### Scenario: Hub instruction has not arrived
- **WHEN** an explicitly authorized source PR later exists without Hub authorization
- **THEN** no Plugin Hub PR is opened

#### Scenario: User later requests Hub update
- **WHEN** the user separately authorizes the Hub update and the accepted source is publicly available
- **THEN** only the actual necessary manifest fields are updated, warnings preserved and the created PR attached without inferring permission to merge

## ADDED Requirements

### Requirement: Explicit refactor decision coverage and predecessor integration
The change SHALL account for every D01–D11 decision, R01–R10 research recommendation and U1–U9 user finding in design/specs/tasks and verification. Existing satisfactory sidebar components/publication mechanisms SHALL be reused; unresolved scope SHALL NOT silently become background reading or a checked task. The prior refine-questcape-sidebar-ux deltas SHALL remain effective where unchanged; conflicting disclosure/header/detail behavior SHALL be explicitly superseded. Neither unaccepted predecessor nor successor SHALL be archived during proposal generation. Combined acceptance bookkeeping SHALL identify the corrected tested revision without backdating acceptance of the failed predecessor candidate; any eventual authorized archive SHALL apply predecessor then successor without restoring stale behavior.

#### Scenario: Decision coverage review
- **WHEN** a reviewer checks an accepted decision or one of the nine findings
- **THEN** its implementation owner, requirement/scenario and task are discoverable, with any missing work explicitly pending

#### Scenario: Overlapping active changes
- **WHEN** both sidebar and refactor changes remain active or later receive combined-candidate acceptance
- **THEN** unchanged sidebar contracts are retained, the new interaction decisions win explicitly and historical acceptance is not fabricated

### Requirement: Selected Ctrl interaction version
The approved hold-Ctrl follow-up SHALL use compatible minor version 3.4.0 consistently across build, properties, packaged metadata, generated product header and current release documentation. Historical 3.2.0/3.3.0 evidence SHALL retain its original version context. Acceptance and final audit SHALL target the corrected combined candidate.

#### Scenario: Selected alternative replaces earlier mode
- **WHEN** the user selects option A and implementation removes the permanent selection action
- **THEN** the 3.4.0 metadata and acceptance checklist describe scoped Ctrl selection, while unaccepted earlier candidates remain historical
