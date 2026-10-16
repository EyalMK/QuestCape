## MODIFIED Requirements

### Requirement: Preserve recoverable saved data
The store SHALL validate schema and data shape, enforce bounded reads, retain malformed/unsupported originals, and stage writes in the destination directory. Replacement SHALL be atomic when supported. Unsupported atomic replacement SHALL preserve the previous record and degrade to unsaved in-memory operation unless a separately verified recovery-safe strategy exists. Cleanup failures SHALL NOT obscure the original error or delete unrelated files.

New source evidence SHALL be optional and separately versioned while retaining legacy schema-1 fields and stable keys. Missing, v1 cell-link-only and unsupported enrichment SHALL use the appropriate supported legacy adapter offline without discarding originals. Old-reader/new-writer round trips SHALL remain valid; any incompatible identity/data change SHALL require an explicit lossless migration/version decision before replacement.

#### Scenario: Atomic replacement unsupported
- **WHEN** the filesystem refuses atomic replacement
- **THEN** the prior destination remains intact, the candidate is cleaned up when possible, and the caller receives a controlled unsaved outcome without truncation or delete-then-write

#### Scenario: Corrupt, oversized, or unknown-schema data
- **WHEN** a cache is invalid, too large, or has an unsupported schema
- **THEN** the file is retained, invalid data is not adopted as progress, and the plugin can obtain fresh valid data without freezing

#### Scenario: Temporary file cleanup fails
- **WHEN** a write fails and deleting its temporary file also fails
- **THEN** diagnostics preserve the write failure and the prior good file, and no exception escapes into client/UI lifecycle handling

#### Scenario: Evidence upgrade or downgrade
- **WHEN** a fresh enriched snapshot is read by the legacy reader or a current reader encounters absent/v1/unknown enrichment
- **THEN** retained legacy content/keys remain readable without network access or fabricated richer evidence, and unsupported original files remain preserved

## ADDED Requirements

### Requirement: Reversible manual intent during recovery
Only eligible Kudos actions SHALL accept manual edits. Recovery SHALL preserve latest account-scoped true AND false edits independently of the checked-set representation, overlay them on recovered saved checks and retain untouched/unknown saved keys without granting new eligibility. Pending edits SHALL be revision/sequence-aware and remain recoverable in memory until their captured compatible save succeeds. Late saves SHALL acknowledge only included edits and SHALL NOT clear newer intent, republish invalid accounts or describe a new record with an old saved outcome. Expected retry/backoff SHALL be bounded and testable without sleeps; corrupt/unsupported originals SHALL NOT be overwritten to force recovery.

#### Scenario: Clear a previously saved check while reading fails
- **WHEN** a saved Kudos check is true, cache reading fails, the user clears that check and later readable storage is recovered
- **THEN** pending false removes the recovered true value, unrelated saved checks survive and the user's clear is not resurrected by set union

#### Scenario: Latest edit wins
- **WHEN** the same key is set true, false and true through failed saves before recovery
- **THEN** the recovered value follows the latest scoped edit, and successful persistence acknowledges only the saved edit revisions

#### Scenario: Edit while older save is blocked
- **WHEN** an older save finishes after a later edit to the same key or after logout/account switch
- **THEN** it cannot discard that newer edit, change another account or claim the newer view was saved

#### Scenario: Retired action or unreadable original
- **WHEN** an old callback targets a retired non-Kudos key or recovery encounters corrupt/unsupported data
- **THEN** automatic completion is not overridden, the original is retained and valid in-memory facts/eligible pending intent remain usable with honest unsaved status
