## MODIFIED Requirements

### Requirement: Debounced step search
Search SHALL wait approximately 300 milliseconds after the last edit, score visible step titles deterministically, and initially scroll to the best match without hiding further cards. Exact, prefix and substring matches SHALL precede token/limited typo matches, with route order breaking ties for the initial jump. The result list, counter indices and traversal SHALL use ascending step numbers rather than relevance scores. The field SHALL show a borderless current/total and downward chevron at its right edge, including disabled 0/0 for a nonempty no-match query. A separate lightly outlined X SHALL coexist with that navigator and clear only search. Its tooltip/accessibility name SHALL be `Clear search`; blank-query visibility SHALL follow the existing navigator rule, with the X hidden/disabled without a disruptive layout jump. Match feedback SHALL appear directly below the search field, separate from `x of y steps shown` and whole-route completion totals. Nonempty no-match feedback SHALL be `No matching step`; selected-match feedback SHALL identify `Search match: step N` rather than claiming the viewport is at that step.

Enter SHALL resolve pending search and then advance through matches in ascending step-number order. At the last of multiple matches, two Enter presses within 500 ms SHALL wrap to the first; a single or expired press SHALL NOT wrap. Query edits, filter changes, manual navigation, reset and disposal SHALL invalidate wrap state. Clearing effective search SHALL restore the viewport anchor saved before search began, preserving it across query edits including select-all replacements, cancel pending search jumps and retain active type/state filters. Reset SHALL also restore that origin when available. No match SHALL show a message without moving the viewport. Live updates SHALL reconcile counts/membership without automatic jumps; route replacement SHALL NOT retain stale cards. Manual navigation and disposal SHALL prevent stale deferred jumps. Search SHALL remain local and SHALL NOT send HTTP requests or game input. Existing filter artwork, menus, tooltips and accessible names SHALL remain unchanged.

#### Scenario: Rapid typing
- **WHEN** several query edits arrive within the debounce interval
- **THEN** only the latest query causes a search jump after typing pauses

#### Scenario: Search conflicts with filters
- **WHEN** a matching step is excluded by active filters
- **THEN** search does not reveal it, and reports no match below the search field if no visible step matches

#### Scenario: Clear after changing the query
- **WHEN** the user scrolls to an offset within a card, searches and edits/replaces the query, then activates Clear search
- **THEN** the original card/offset is restored, the counter disappears, no pending jump occurs, and filter selections remain

#### Scenario: Traverse and wrap
- **WHEN** Enter advances to 5/5 and the user then presses Enter twice quickly
- **THEN** the view returns to 1/5; a single Enter at 5/5 or presses more than 500 ms apart do not wrap

#### Scenario: Visible rows with zero search matches
- **WHEN** one filtered row is visible and a nonempty query matches none of the filtered rows
- **THEN** the field retains disabled borderless `0/0` with downward chevron beside enabled clear X, `No matching step` appears below the field, shown-step count remains one, and global totals do not change

#### Scenario: Clear differs from reset
- **WHEN** the user clears search and subsequently activates the existing reset control
- **THEN** clear retains filters, while reset separately clears query and both filter groups according to its existing contract

### Requirement: Continue alignment and larger projections
Continue SHALL occupy the usable footer width with reasonable padding/margins and the existing resume/play icon. A single position-aware circular up/down arrow-icon control SHALL appear centered above it, replacing visible Top/Bottom text without adding simultaneous boundary controls. The active tooltip/accessibility name SHALL remain Top or Bottom, and the existing position-based destination policy and Ctrl+Home/End/Ctrl+J shortcuts SHALL remain. Continue SHALL be enabled whenever the earliest visible unfinished card is not aligned with the viewport top, even if already visible. Scrolling SHALL clamp normally at route boundaries; an already reached clamped target SHALL disable Continue. Logged-out/all-complete/empty-filter/unavailable-route views SHALL disable it. Unknown and in-progress rows SHALL remain unfinished candidates under active filters. Live state updates SHALL NOT themselves navigate or steal focus. Projected skill icons SHALL be 18px, prepared from local RuneLite images on a worker; Additional info icons SHALL remain 16px.

#### Scenario: Next card already visible below the top
- **WHEN** the earliest visible unfinished card begins below the viewport top
- **THEN** Continue remains enabled and moves that card to the top where scrolling permits

#### Scenario: Position-aware circular action
- **WHEN** the existing boundary policy selects Bottom near the upper route or Top near the lower route
- **THEN** the same circular control displays the corresponding down/up arrow icon and destination name, above full-width Continue

#### Scenario: Aligned or unavailable target
- **WHEN** the clamped unfinished target is already aligned, all filtered actions are complete, or route/account readiness is unavailable
- **THEN** Continue is disabled regardless of the enabled style shown in the design mockup

#### Scenario: Live completion while reading
- **WHEN** a live update completes the current unfinished row
- **THEN** the next target and enablement update without moving the viewport, collapsing details or stealing focus

## ADDED Requirements

### Requirement: Local skill icons in training titles
Recognized skill-name tokens in training instructions SHALL display the actual locally supplied RuneLite skill icon immediately before the unchanged name. The existing training category icon SHALL remain distinct. Title rendering SHALL consume guide-boundary tokens/canonical skill identities rather than reparse rendered text. Icon preparation SHALL occur on the worker, publication on the EDT, and late assets SHALL preserve dimensions and reject disposed generations. Text names, complete wrapping and accessible descriptions SHALL remain useful when an icon is unavailable. Combat SHALL use an honest derived-target treatment rather than a fabricated skill image. Filter/category/status artwork SHALL NOT be recreated or replaced during this change.

#### Scenario: Compound training title
- **WHEN** a parsed training instruction names Attack and Strength
- **THEN** each original name is preceded by its actual corresponding skill icon, while the category icon and full instruction wording remain

#### Scenario: Missing or late icon
- **WHEN** an actual skill asset is not yet available or a disposed generation receives it late
- **THEN** the skill name stays readable with stable layout, and only the active generation can apply the prepared asset
