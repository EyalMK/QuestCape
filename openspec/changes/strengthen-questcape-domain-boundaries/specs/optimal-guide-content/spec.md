## MODIFIED Requirements

### Requirement: Readable native guide presentation
The plugin SHALL expose the guide through a RuneLite sidebar with green/checkmarked completed rows, separate incomplete, in-progress, and unknown indicators, and readable access to every source field. It SHALL keep completed rows visible by default and support keyboard activation and normal narrow sidebar widths. It SHALL implement the approved revision 3 hierarchy while retaining the current font family, 18-point header title, 16-point card titles, 14-point body/status text and metadata at least 13 points. Source order and completion vocabulary SHALL remain unchanged. Existing guide links SHALL remain independent user actions; this UI change SHALL NOT introduce a Quest Helper handoff.

Ordinary disclosure surfaces SHALL use a hand cursor consistently. Route advice headings SHALL increase modestly to an initial 14-point bold token. Quest points and Guide QP total SHALL each use the same heading/value typography as Route advice and its 14-point description, preserving their distinct semantics; other approved font family/title/body minimums SHALL remain.

#### Scenario: Larger text and progress indicator
- **WHEN** the guide is displayed at a normal narrow RuneLite sidebar width
- **THEN** metadata is at least 13 pt, body text is 14 pt, quest titles are 16 pt, and the top progress bar is 22 pixels tall with a readable percentage

#### Scenario: Whole-card detail action
- **WHEN** a player clicks a card's title, background, metadata, status, or detail text, or uses Enter/Space with the card focused
- **THEN** the card toggles its details immediately once without a deliberate text-click delay, preserving independent controls, read-only projected regions and explicit selection mode

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

#### Scenario: Consistent detail typography
- **WHEN** advice, quest points and running guide QP appear together
- **THEN** the three labels share the advice heading style and their values share the readable advice body style, without implying guide QP is observed account QP

### Requirement: Distinct header actions and compact provenance
The header SHALL show the packaged plugin icon before QuestCape with additional padding, the selected GitHub silhouette inside a very faint outline, and a separate selected circular-arrow wiki-refresh control beside source attribution. GitHub activation SHALL open exactly `https://github.com/EyalMK/QuestCape/issues` through LinkBrowser, only on explicit user action, without automatic requests, player-data submission or broadening parsed-source URL validation. Its exact tooltip SHALL be `Report issues or contribute`, floating below the button in a smaller, content-sized, faintly bordered popup that reserves no normal layout row. The local progress action SHALL be labeled `Resync Progress`, use distinct opposing horizontal arrows rather than download imagery, retain a restrained outline, and show `Resync progress from RuneLite` as its tooltip. It SHALL preserve existing readiness and explicit fresh-observation semantics. Routine character-name/Live/Standard/success copy SHALL move out of the permanent header; established account/mode, observation freshness, independent guide revision/retrieval/validation and license attribution SHALL remain keyboard-accessible on demand. Internal scope/isolation SHALL remain unchanged.

The current Resync Progress fill, quiet shell outline, wording and opposing-arrow alignment SHALL remain, but the shell/label SHALL be inert and only its embedded icon button SHALL activate local resync. The icon SHALL have usable hit area, meaningful accessible name, tooltip, visible keyboard focus and readiness/running enablement. Title, source and resync rows SHALL have named vertical gaps; an increased gap SHALL separate resync from the completion-count/progress-bar group. Count and bar SHALL remain one compact group, with short/narrow layouts readable.

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

#### Scenario: Icon-only local resync
- **WHEN** a user clicks the resync label or shell, then activates its icon by pointer or keyboard
- **THEN** shell/label cause no request and use the default cursor, while only the enabled icon invokes one fresh local observation without route refresh or HTTP

#### Scenario: Header groups remain distinct
- **WHEN** the sidebar is rendered at narrow or short sizes with or without multiline recovery feedback
- **THEN** title→source, source→resync and resync→completion have deliberate vertical separation using named metrics, without clipping controls or separating count from its bar

### Requirement: Advice-first details with complete source access
Details SHALL render semantic field roles supplied by the guide boundary rather than literal English-header checks in Swing. A validated `Wiki quick guide ↗` SHALL remain the primary expanded-detail action; N/A, missing or ambiguous evidence SHALL NOT create a dead/guessed action. Expanded content SHALL show full route advice before guide projected levels and compact quest metadata, followed by location and Related wiki pages. `Quest page ↗` SHALL appear under Related wiki pages, not beside the primary quick guide. Projected levels SHALL be identified as guide projections, retain actual skill images and accessible names, and SHALL NOT be presented as current levels or prerequisites. QP copy SHALL distinguish `Quest points` from `Guide QP total`. Original advice, unknown/annotated fields, location links and all validated source destinations SHALL remain accessible, including completed rows and distinct fragments. Equivalent destinations SHALL be deduplicated only without losing their source context. Unrecognized fields and unsupported N/A values SHALL have an honest raw source-fields fallback. Existing whole-candidate validation and last-good retention SHALL remain.

Related wiki pages SHALL be an independent expandable subsection, initially folded when another meaningful detail block exists and initially unfolded when it is the sole meaningful detail content. Empty/N/A placeholders SHALL NOT count as another meaningful block; nonempty primary/advice/projection/QP/location or substantive raw content SHALL count. No subsection SHALL appear for zero related links. User subsection state SHALL persist across parent folding and ordinary live updates for unchanged content. Subsection interaction SHALL NOT toggle the parent; full original links/anchors SHALL remain available after opening it. Quest points and Guide QP total SHALL use the advice heading/value styling.

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

#### Scenario: Related links alongside advice
- **WHEN** details include advice or other meaningful content and related links
- **THEN** related links start folded, their named toggle/count remains visible and opening it reveals all links without collapsing the card

#### Scenario: Only related links
- **WHEN** related links are the sole meaningful detail content, including when other source fields are only empty/N/A
- **THEN** their subsection starts unfolded and remains independently collapsible

#### Scenario: Subsection choice survives live rendering
- **WHEN** the user changes related-link expansion, folds/reopens the parent and receives unchanged-content live updates
- **THEN** the user's subsection choice survives without unsolicited movement or focus change

### Requirement: Selection-safe and accessible disclosure
Ordinary left clicks on default non-control card body/title/metadata/status/advice SHALL toggle details once immediately, without a multi-click delay timer, using a consistent hand cursor. Dragging, right-clicking, repeated click counts and independently handled events SHALL NOT produce extra toggles. Enter/Space on the focused card SHALL disclose; nested controls SHALL keep their own keyboard actions. Holding Ctrl over card text SHALL enable native drag/word selection with a text cursor and no disclosure, with a discoverable small hover hint after one second and an accessibility description; unrelated tooltip timings SHALL remain unchanged. Release SHALL restore ordinary hand-cursor behavior while retaining the highlight/focus for Ctrl+C. Ordinary disclosure, focus leaving the card, replacement or disposal SHALL clear selection. Retained live cards SHALL preserve selections. Handling SHALL remain local to sidebar pointer/focused-key events with no global game interception; native caret/copy bindings SHALL be retained. The permanent Select text/Done selecting action SHALL be removed. Projected-level panels, labels, images and gaps SHALL remain read-only/default cursor with useful tooltips/accessibility and no parent disclosure. Expanded/collapsed state SHALL be accessible; explicit reflow SHALL preserve anchors and live updates SHALL NOT steal focus/scroll or collapse content. This supersedes the prior timer-based direct-selection contract.

#### Scenario: Immediate middle and edge clicks
- **WHEN** the user completes an ordinary click on title/advice text or a non-control card edge in default mode
- **THEN** the same disclosure action executes on that event without scheduling a delay, and both surfaces use a hand cursor

#### Scenario: Explicit text selection
- **WHEN** the user holds Ctrl while dragging or double-clicking card text, releases Ctrl and copies
- **THEN** native selection/copy works without card collapse, release retains highlight with a hand cursor and ordinary disclosure clears it

#### Scenario: Independent projected region
- **WHEN** the user hovers or clicks a projected-level label, icon or its panel gap
- **THEN** the tooltip remains available and clicking causes no disclosure, browser action, completion change or navigation

#### Scenario: Nested actions and keyboard focus
- **WHEN** an eligible manual checkbox, link, related subsection or other nested action is activated by pointer or its focused keyboard action
- **THEN** only that action runs, card expansion remains unchanged and accessible state/focus remains accurate

#### Scenario: Live change and disposal
- **WHEN** retained cards receive live completion changes or old cards are replaced/disposed
- **THEN** retained expansion/selection/subsection intent remains stable and disposed interaction work cannot alter the new view

#### Scenario: Fold large route advice without moving the card
- **WHEN** a visible card with substantial advice is expanded and folded
- **THEN** its pixel anchor is restored after changed row heights and viewport range settle, subject to end clamping, and any newer explicit user navigation wins

## ADDED Requirements

### Requirement: Compact training progress circles
Training cards SHALL replace status/comparison text with progress circles driven
by the shared domain evaluation. Unfinished circles SHALL display the percentage
as a number without its percent sign at the center, with no current-level text. Arcs SHALL start
at the top and advance counterclockwise; hover SHALL show only the percentage. Completed targets SHALL use a
full green ring/checkmark with a 100% tooltip. Compound targets SHALL remain
individually identified; unavailable levels/intervals SHALL NOT invent progress,
and unsupported required meaning SHALL NOT gain a completion checkmark.

#### Scenario: Percentage center and counterclockwise progress
- **WHEN** a supported 52-to-55 target has observed real level 54
- **THEN** its circle contains 66 and the hover tooltip is only 66%, while the standard status/comparison text is absent

#### Scenario: Completed or unverified target
- **WHEN** a training target is complete, lacks required observations or has unsupported action meaning
- **THEN** verified completion shows a full checkmarked ring; missing/unsupported evidence stays honest and the same domain outcome still drives filters, totals and Continue

### Requirement: Explicit link copying with local transient feedback
Successful browser opening SHALL be silent. Shift plus right-click on a supported
link SHALL copy its configured destination without browser/network work and show
small local feedback for one to two seconds without layout changes. Failure SHALL
NOT claim success. Copy actions SHALL retain original run ownership; feedback
timers/popups SHALL stop on hiding/replacement/disposal. Normal left-click SHALL
retain existing URL validation and browser-error recovery.

#### Scenario: Copy without opening or scrolling
- **WHEN** the user Shift-right-clicks a wiki, quest, training, license or GitHub link
- **THEN** its configured URL is copied, local confirmation appears for 1.6 seconds, and the browser, card expansion and viewport remain unchanged

#### Scenario: Clipboard failure or discarded gesture
- **WHEN** clipboard access fails or the gesture belongs to a disabled/disposed control, drag or expired run
- **THEN** no false copied confirmation or browser action occurs and no stale feedback survives disposal

#### Scenario: Delayed selection hint without Escape cancellation
- **WHEN** the pointer hovers over a non-control card surface for one second
- **THEN** the small Ctrl-selection/copy hint appears, leaving sooner suppresses it, independent tooltips retain their timing, and no Escape cancellation action or hint is installed
