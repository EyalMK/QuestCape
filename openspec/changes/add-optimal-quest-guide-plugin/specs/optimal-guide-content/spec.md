## ADDED Requirements

### Requirement: Complete ordered wiki guide
The plugin SHALL load the table captioned Old School RuneScape Quest Guide from the standard Optimal quest guide page at runtime and preserve the order of all quest, miniquest, training, diary, unlock, and other activity rows. It SHALL retain unknown rows and distinguish source guide projections from actual player statistics.

#### Scenario: A quest begins and finishes at different stages
- **WHEN** the same quest or activity appears more than once, including an earlier start step and a later completion step
- **THEN** every occurrence remains visible, start/finish stages have distinct identities, and an in-progress quest satisfies only its start stage

#### Scenario: Mixed table content
- **WHEN** the source contains quests interleaved with merged training rows, unlocks, and reward hand-ins
- **THEN** the panel displays every row in source order and retains its title, levels, quest points, running guide QP, additional information, location, and source links where present

#### Scenario: Unrecognized activity
- **WHEN** an otherwise valid source table includes a new activity type
- **THEN** the activity remains visible with its source text and an unknown completion state instead of disappearing

### Requirement: Runtime updates and resilient cache
The plugin SHALL load its last validated snapshot immediately, revalidate on startup when older than 24 hours, and offer Refresh guide. It SHALL incorporate ordinary table additions, reordering, and text changes without a plugin update, validate a whole candidate before replacing the cache, and retain the previous snapshot on failure.

#### Scenario: Wiki revision changes
- **WHEN** a refresh retrieves a valid revised table with inserted or reordered rows
- **THEN** the panel adopts that revision without a new binary and preserves progress for unambiguously matched row identities

#### Scenario: Invalid or truncated response
- **WHEN** retrieval fails or parsing finds a missing table, ambiguous identity, or suspicious truncation
- **THEN** the plugin retains the last validated snapshot and reports the failure and cached content age

#### Scenario: First launch while offline
- **WHEN** no valid cache exists and retrieval fails
- **THEN** the panel shows an unavailable-guide state with Retry rather than an empty guide presented as complete

#### Scenario: Duplicate refreshes
- **WHEN** repeated refresh clicks occur while a request is active or within the cooldown
- **THEN** the plugin coalesces or defers the requests and keeps the UI responsive

### Requirement: Readable native guide presentation
The plugin SHALL expose the guide through a RuneLite sidebar with green/checkmarked completed rows, separate incomplete, in-progress, and unknown indicators, and readable access to every source field. It SHALL keep completed rows visible by default and support keyboard activation and normal narrow sidebar widths.

#### Scenario: Larger text and progress indicator
- **WHEN** the guide is displayed at a normal narrow RuneLite sidebar width
- **THEN** metadata is at least 13 pt, body text is 14 pt, quest titles are 16 pt, and the top progress bar is 22 pixels tall with a readable percentage

#### Scenario: Whole-card detail action
- **WHEN** a player clicks a card's title, background, metadata, status, or detail text, or uses Enter/Space with the card focused
- **THEN** the card toggles its details while preserving the independent actions of the Open button, links, and manual checkboxes

#### Scenario: Dedicated Quest Helper action
- **WHEN** a quest or miniquest card is shown
- **THEN** a compact Open button appears beside its step number/type, using Quest Helper's registered sidebar icon when available, and opens Quest Helper without toggling details

#### Scenario: Quest feedback follows the selected card
- **WHEN** a quest's Open button produces login, dependency, search, or activation feedback
- **THEN** that feedback appears immediately above the clicked card instead of at the top of the route

#### Scenario: Position-aware navigation
- **WHEN** the player is at the top, bottom, or middle of the route
- **THEN** fixed navigation offers the appropriate top/bottom destination and a direction-aware jump to the current progress row without changing completion

#### Scenario: Login and repeated live updates preserve scroll
- **WHEN** the player clicks a quest while logged out, logs in, and receives repeated progress updates
- **THEN** rendering does not move the viewport repeatedly, steal focus, or use text caret updates to scroll the route

#### Scenario: Inspect guide detail
- **WHEN** a player expands a long quest or activity row
- **THEN** its notes and metadata are readable without opening the source page or losing the row's place in the guide

#### Scenario: Completion without color recognition
- **WHEN** the player cannot distinguish the row colors
- **THEN** completion remains identifiable through its icon and text

### Requirement: Source attribution and inert content
The plugin SHALL show OSRS Wiki attribution and source provenance, including revision when available and retrieval time. It SHALL render remote content as sanitized data without running page scripts or loading executable instructions.

#### Scenario: Cached source provenance
- **WHEN** a cached guide is displayed
- **THEN** the player can identify the original source and cached retrieval time

#### Scenario: Unexpected active markup
- **WHEN** the fetched document contains scripts, event handlers, or unsafe URL schemes
- **THEN** those elements do not execute or become actionable links in the panel
