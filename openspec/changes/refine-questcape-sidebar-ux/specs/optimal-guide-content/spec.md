## MODIFIED Requirements

### Requirement: Readable native guide presentation
The plugin SHALL expose the guide through a RuneLite sidebar with green/checkmarked completed rows, separate incomplete, in-progress, and unknown indicators, and readable access to every source field. It SHALL keep completed rows visible by default and support keyboard activation and normal narrow sidebar widths. It SHALL implement the approved revision 3 hierarchy while retaining the current font family, 18-point header title, 16-point card titles, 14-point body/status text and metadata at least 13 points. Source order and completion vocabulary SHALL remain unchanged. Existing guide links SHALL remain independent user actions; this UI change SHALL NOT introduce a Quest Helper handoff.

#### Scenario: Larger text and progress indicator
- **WHEN** the guide is displayed at a normal narrow RuneLite sidebar width
- **THEN** metadata is at least 13 pt, body text is 14 pt, quest titles are 16 pt, and the top progress bar is 22 pixels tall with a readable percentage

#### Scenario: Whole-card detail action
- **WHEN** a player clicks a card's title, background, metadata, status, or detail text, or uses Enter/Space with the card focused
- **THEN** the card toggles its details once while preserving independent link/button/manual-checkbox actions

#### Scenario: Independent source action
- **WHEN** a quest or activity's validated guide-link action is activated
- **THEN** it opens its named destination without toggling details, changing completion or introducing a Quest Helper handoff

#### Scenario: Link feedback follows the selected card
- **WHEN** a row's external action produces destination or opening feedback
- **THEN** that feedback remains associated with the selected card rather than being mistaken for another row's completion

#### Scenario: Position-aware navigation
- **WHEN** the player is at the top, bottom, or middle of the route
- **THEN** fixed navigation offers the appropriate top/bottom destination and a direction-aware jump to the current progress row without changing completion

#### Scenario: Login and repeated live updates preserve scroll
- **WHEN** the player browses a card while logged out, logs in, and receives repeated progress updates
- **THEN** rendering does not move the viewport repeatedly, steal focus, or use text caret updates to scroll the route

#### Scenario: Inspect guide detail
- **WHEN** a player expands a long quest or activity row
- **THEN** its notes and metadata are readable without opening the source page or losing the row's place in the guide

#### Scenario: Completion without color recognition
- **WHEN** the player cannot distinguish the row colors
- **THEN** completion remains identifiable through its existing icon and text

## ADDED Requirements

### Requirement: Distinct header actions and compact provenance
The header SHALL show the packaged plugin icon before QuestCape with additional padding, the selected GitHub silhouette inside a very faint outline, and a separate selected circular-arrow wiki-refresh control beside source attribution. GitHub activation SHALL open exactly `https://github.com/EyalMK/QuestCape/issues` through LinkBrowser, only on explicit user action, without automatic requests, player-data submission or broadening parsed-source URL validation. Its exact tooltip SHALL be `Report issues or contribute`, floating below the button in a smaller, content-sized, faintly bordered popup that reserves no normal layout row. The local progress action SHALL be labeled `Resync Progress`, use distinct opposing horizontal arrows rather than download imagery, retain a restrained outline, and show `Resync progress from RuneLite` as its tooltip. It SHALL preserve existing readiness and explicit fresh-observation semantics. Routine character-name/Live/Standard/success copy SHALL move out of the permanent header; established account/mode, observation freshness, independent guide revision/retrieval/validation and license attribution SHALL remain keyboard-accessible on demand. Internal scope/isolation SHALL remain unchanged.

#### Scenario: Report or contribute
- **WHEN** GitHub is activated by pointer or keyboard
- **THEN** the fixed issues page opens through LinkBrowser without changing completion, starting refresh or submitting character data

#### Scenario: Small floating tooltip
- **WHEN** GitHub is hovered or exposes its tooltip through keyboard access
- **THEN** the exact tooltip appears below the button with smaller text and a very faint border, without a full-width header band or reserved space between title and source row

#### Scenario: Independent refresh operations
- **WHEN** wiki refresh or Resync Progress is activated
- **THEN** only its existing route-refresh or local observation operation runs, using distinguishable icons; account-unready state disables local resync while cached route browsing remains available

#### Scenario: Provenance on demand
- **WHEN** the user requests source/progress details or switches account/mode
- **THEN** current established identity and independent timestamps remain accessible, and no prior account's facts or pending callbacks populate the new scope

### Requirement: Advice-first details with complete source access
Details SHALL render semantic field roles supplied by the guide boundary rather than literal English-header checks in Swing. A validated `Wiki quick guide ↗` SHALL remain the primary summary action; N/A, missing or ambiguous evidence SHALL NOT create a dead/guessed action. Expanded content SHALL show full route advice before guide projected levels and compact quest metadata, followed by location and Related wiki pages. `Quest page ↗` SHALL appear under Related wiki pages, not beside the primary quick guide. Projected levels SHALL be identified as guide projections, retain actual skill images and accessible names, and SHALL NOT be presented as current levels or prerequisites. QP copy SHALL distinguish `Quest points` from `Guide QP total`. Original advice, unknown/annotated fields, location links and all validated source destinations SHALL remain accessible, including completed rows and distinct fragments. Equivalent destinations SHALL be deduplicated only without losing their source context. Unrecognized fields and unsupported N/A values SHALL have an honest raw source-fields fallback. Existing whole-candidate validation and last-good retention SHALL remain.

#### Scenario: Completed Tourist Trap
- **WHEN** the completed quest is expanded
- **THEN** its full Agility/reward advice precedes projections/QP, all original fields/destinations remain accessible, and completion does not hide useful context

#### Scenario: Lunar Diplomacy link hierarchy
- **WHEN** the incomplete quest has a validated source quick guide and quest page
- **THEN** Wiki quick guide is primary, Quest page is under Related wiki pages, advice precedes Magic/Runecraft guide projections, and guide QP total is distinct from actual account progress

#### Scenario: Miniquest with N/A quick guide
- **WHEN** In Search of Knowledge has source quick-guide value N/A
- **THEN** no primary quick-guide action is invented, its advice/links remain readable, and N/A remains inspectable in source fields

#### Scenario: Unknown field and distinct link anchors
- **WHEN** retained source content includes an unfamiliar field or two safe links to different fragments of one page
- **THEN** the field and both distinct destinations remain accessible without requiring the UI to infer their meaning

### Requirement: Selection-safe and accessible disclosure
An ordinary left single click anywhere in the non-control card body SHALL expand/collapse once. Text selection drags and multi-click selection SHALL NOT toggle disclosure. Nested links, action buttons and eligible manual checkboxes SHALL perform only their own action. Enter/Space on the focused card SHALL toggle disclosure; those keys on a nested control SHALL retain that control's action. Disclosure SHALL expose current expanded/collapsed state accessibly, preserve text selection and stable expansion/viewport context on reflow, and SHALL NOT auto-collapse or steal focus/scroll on live completion. Controls SHALL retain meaningful names, visible focus and usable hit areas at narrow widths/platform scaling. RuneLiteLAF wrapping SHALL preserve complete source text without horizontal instruction scrolling.

#### Scenario: Read and select advice
- **WHEN** the user drags across advice or multi-clicks to select its text
- **THEN** selection remains usable and the card does not collapse or expand as a side effect

#### Scenario: Nested manual or link action
- **WHEN** the user activates an eligible Kudos checkbox or validated link inside the card
- **THEN** only the intended manual/link action runs, without an extra disclosure toggle

#### Scenario: Keyboard disclosure and live change
- **WHEN** the focused card toggles with Enter/Space and a live update later changes its completion
- **THEN** accessible disclosure state matches the expansion and the update preserves expansion, focus and viewport intent

### Requirement: Truthful conditional operation feedback
The UI SHALL receive typed operation/persistence outcomes with the content/facts they describe, not infer application state through status-message substring tests. Touched publications SHALL capture immutable service views and carry original run/account authority through completion and EDT delivery, rejecting invalidated callbacks. Missing observations, unsupported/ambiguous action meaning, known false facts, eligible unchecked manual intent and unsaved content/progress SHALL remain distinct. Account/route unavailable, refresh failure and unsaved limitations SHALL be visible in a concise conditional nonmodal status area outside the route scroller; routine successful sync messages SHALL NOT occupy a permanent block. Valid in-memory observations/content and last-good route browsing SHALL remain usable on save/refresh failure. Retry/Resync/Refresh actions SHALL correspond to the actual limitation; unsupported meaning SHALL NOT be advertised as repairable by resync. General unknown reasons SHALL be shown only when supported by evidence.

#### Scenario: Failed persistence while reading lower route
- **WHEN** current progress or route content is usable in memory but saving fails while the viewport is away from the route top
- **THEN** a visible conditional unsaved limitation identifies the correct current content/account, leaves that content usable and offers the relevant recovery action

#### Scenario: Unsupported action versus absent observations
- **WHEN** an activity lacks a supported meaning or the current account lacks a required observation
- **THEN** the respective reason remains distinct, and resync is offered only for observation/readiness recovery rather than as a fix for unsupported semantics

#### Scenario: Late operation after restart or account switch
- **WHEN** an old worker/completion/EDT callback arrives after its original run or account authority was invalidated
- **THEN** it cannot change the new header, current facts, status, focus or viewport

#### Scenario: Success after recovery
- **WHEN** a valid current operation resolves its failure or unsaved limitation
- **THEN** the corresponding conditional message clears without a permanent success banner or automatic scroll
