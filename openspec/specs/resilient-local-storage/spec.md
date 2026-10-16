# resilient-local-storage Specification

## Purpose
Keep approved local storage bounded, recoverable, isolated, and asynchronous without preventing live guide use when persistence fails.

## Requirements

### Requirement: Supported cross-platform storage boundary
Automatic runtime file access SHALL use the storage API permitted by the resolved RuneLite release and current Plugin Hub rules, with all plugin data and temporary files confined to its subdirectory below `.runelite`. Implementation SHALL verify current `Filepath` guidance and API compatibility, avoid unchecked escape facilities, preserve existing `.runelite/questcape` data through an approved access/migration path, use fixed categories and hashed keys, and perform disk work away from ClientThread and the Swing EDT. Packaged assets SHALL load from classpath streams.

#### Scenario: Ubuntu home path
- **WHEN** QuestCape runs on Ubuntu with a valid home path containing spaces or Unicode characters
- **THEN** its plugin data is read/written below the permitted RuneLite directory without Windows separators or platform-specific assumptions

#### Scenario: Upgrade to supported storage facility
- **WHEN** the approved plugin-directory API changes the runtime root
- **THEN** an explicit approved migration retains existing progress/manual checks and cache files without silently abandoning old data or bypassing path restrictions

#### Scenario: Plugin Hub packaged execution
- **WHEN** QuestCape runs from its ordinary distribution JAR
- **THEN** required icons and properties load through classpath streams without assuming unpacked filesystem resources

### Requirement: Storage failures do not prevent live use
Expected read, write, permission, provider, and malformed-cache failures at plugin-owned storage boundaries SHALL be contained without terminating RuneLite or blocking its client/UI threads. Valid fresh observations and valid guide content SHALL remain usable in memory when persistence fails. The UI SHALL distinguish unsaved progress/content from persisted data and provide a concise limitation. Diagnostics SHALL use debug logging and SHALL NOT expose character identity or credentials.

#### Scenario: Unwritable progress cache
- **WHEN** the established character's new observation is valid but saving it fails
- **THEN** the current character view and completion values update in memory, the prior disk record survives, and the UI indicates unsaved state

#### Scenario: Unwritable guide cache
- **WHEN** a valid guide refresh succeeds but caching it fails
- **THEN** the fresh guide remains usable in memory with an unsaved indication and the previous saved snapshot is retained

#### Scenario: Cache path conflicts with a file
- **WHEN** an expected cache directory is a regular file or is inaccessible
- **THEN** the plugin reports the limitation, avoids uncontrolled exceptions, and leaves RuneLite and any available route/progress usable

#### Scenario: Storage recovers with unchanged observations
- **WHEN** a previously unsaved observation is unchanged and storage later becomes writable
- **THEN** a bounded retry or explicit Sync can persist it, clearing unsaved status without writing on every game tick

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

### Requirement: Lifecycle-safe persistence publication
Storage and recovery SHALL preserve generation/session validation around asynchronous work. Shutdown, logout, account switches, and guide cancellation SHALL NOT wait for disk/network work, and old work SHALL NOT publish current character or UI state after invalidation. Owned executors, HTTP calls, and scheduled work if introduced SHALL be canceled explicitly.

#### Scenario: Logout while a save is blocked
- **WHEN** logout or shutdown occurs while a worker is writing
- **THEN** the foreground action returns promptly and the late result cannot restore the prior character view

#### Scenario: Account changes during recovery
- **WHEN** a save/retry for account A finishes after account B becomes established
- **THEN** it cannot alter account B's progress, persistence status, or UI

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
