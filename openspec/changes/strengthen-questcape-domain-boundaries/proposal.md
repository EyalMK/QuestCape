## Why

The sidebar candidate incorporated only part of `.research/questcape-refactor-decisions.md`; title-derived classification, constructor-owned identities, central activity predicates, hardcoded HTTP identity and mixed plugin orchestration remain. User testing now also exposes four training regressions and eight interaction/presentation shortcomings, so the next candidate must strengthen the existing layers and correct those behaviors with explicit acceptance criteria.

## What Changes

- Adopt **all D01–D11 decisions and R01–R10 research recommendations** as scoped outcomes, with a decision/spec/task coverage matrix. Reuse already sound sidebar components, typed publication and skill catalog rather than reimplementing them.
- Preserve sanitized source attributes, cell/link roles and skill spans; reconcile supported identities and explicit actions centrally. Assemble immutable rows without constructor classification; use typed runtime operands, enum-owned reusable policies and compatibility codecs for existing storage keys.
- Repair the four reported pure-training rows: Prayer 33→37 for Protect from Magic, Prayer 38→40 for Protect from Missiles, Prayer 41→43 for Protect from Melee, and Farming 41→46 with crop-growing advice. Separate purpose/advice from required targets without ignoring an additional required action. Retain interval math, compound precedence and honest unknowns.
- Introduce explicit activity-family detectors and separate training/projection/skill-mention interpreters with shared catalog/tokenization. Preserve exact current game predicates, automatic eligibility, real-level observation and local account isolation.
- Preserve both true and false manual edits during cache recovery. Give each enabled run explicit resource ownership, original-session continuations and paired immutable data/outcomes; remove remaining status-text protocols before broad class moves.
- Load header-safe product name/version once from classpath plugin properties for development and packaged execution; remove hardcoded User-Agent versions. Name meaningful one-use literals in cohesive owners.
- Make ordinary whole-card disclosure immediate and pointer-consistent; remove the text multi-click timer. Provide explicit hold-Ctrl text selection (user-selected option A) with a hover/accessibility hint, preserving native copying without delaying ordinary clicks. Nested controls and read-only projected-level regions never toggle the card.
- Make Related wiki pages a separate expandable subsection, folded initially unless it is the only meaningful detail content. Increase Route advice headings modestly; render Quest points and Guide QP total using the same heading/value styling.
- Keep the Resync Progress shell, but make only its opposing-arrow icon an actionable button. Add named vertical gaps between title, Wiki/source, resync and completion sections. Preserve approved artwork, search/filter semantics, complete advice, footer behavior and explicit navigation.

## Capabilities

### New Capabilities

- `guide-domain-model`: evidence reconciliation, prepared row semantics, typed identities/compatibility codecs, enum policies, detector/interpreter ownership and conservative reasons.
- `plugin-run-ownership`: explicit per-run resources, nonblocking cleanup, narrow orchestration and deterministic stale-work/failure guarantees.
- `plugin-product-metadata`: classpath metadata, validated derived User-Agent, worker-side load and development/JAR parity.

### Modified Capabilities

- `guide-progress-tracking`: explained training targets, all four regressions, typed completion and activity-observation parity.
- `optimal-guide-content`: immediate disclosure, explicit selection, nested links disclosure, inert projections, consistent detail typography and spaced icon-only resync.
- `guide-navigation`: read-only projection regions while preserving full-width Continue, circular boundary control, search and filter intent.
- `resilient-local-storage`: reversible manual-intent recovery, additive evidence and identity compatibility.
- `release-verification`: staged refactor evidence, coverage accounting, combined candidate acceptance and local-only release gates.

## Impact

Affected areas: guide parsing/classification/assembly/cache adaptation; activity/live observation and completion; progress recovery; bounded HTTP metadata/build resources; plugin run/capture/refresh/link/sidebar coordination; targeted header/card/details interactions. No new runtime dependency, reflection, generic rules/component/job framework, dynamic discovery, telemetry, game input or UI toolkit is proposed. Approximately 300 production lines remains a cohesion guideline; tests and a reasonable parser are exempt from size-driven work.

This proposal builds on local `codex/questcape-sidebar-ux` at `33434a8` (production candidate `7398b63`, master baseline `40eb54a`). The prior UI change remains unaccepted; its still-valid deltas are prerequisites, and this change explicitly supersedes the conflicting disclosure/header/detail behavior. Historical Quest Helper handoff and publication instructions confer no new authorization. **No merge into master**, push, PR, Hub update, archive, production edit or version bump occurs during proposal preparation.

Plan a compatible `3.2.0` combined implementation release if the API/data compatibility audit passes; an incompatible public contract requires an explicit `4.0.0`/migration revision before implementation. Preserve readable schema-1 legacy fields, exact Kudos/account/config tokens and unsupported originals; do not trade correctness for a cosmetic cleanup.
