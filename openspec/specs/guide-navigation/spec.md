# guide-navigation Specification

## Purpose
Provide local guide navigation, combined filters, ordered debounced search, complete titles, and accessible skill-icon presentation.

## Requirements

### Requirement: Complete balloon titles and inline skill mentions
Fresh and cached guide rows SHALL retain complete balloon destinations. A truncated cached balloon title SHALL be repaired only when safe retained Wiki links identify exactly one supported destination; ambiguous or missing evidence SHALL remain unknown. Additional info SHALL insert a locally supplied skill icon before each whole skill-name mention, preserving wording, wrapping and accessibility. Actual skill images SHALL be prepared on the worker before sidebar publication. A generic training icon SHALL NOT substitute for a skill icon. Late actual icon loading SHALL update existing text on the EDT without remote image loading. Projected levels SHALL keep number/icon order.

#### Scenario: Cached destination recovery
- **WHEN** a title ends with “to” and retained safe links identify exactly Crafting Guild
- **THEN** the title and title field include Crafting Guild and use that route's automatic identity

#### Scenario: Repeated skill mentions
- **WHEN** Additional info mentions Herblore twice and Runecrafting once
- **THEN** each mention receives the correct icon before its unchanged name, and unrelated words are preserved

### Requirement: Combined guide filters
The sidebar SHALL allow one step type and one completion state (complete, incomplete, in progress, unknown) at a time. Type and state SHALL apply together. Each menu SHALL offer All to clear that group. Choosing a different option SHALL replace the previous selection, including through keyboard navigation. Type options SHALL display icons before names with a larger gap; State options SHALL display the card's status glyph immediately before its name in the card's status color. Accessible icon-only filter triggers and reset SHALL be spread evenly across the available width. Filters SHALL preserve original numbers and whole-route totals, hide notes while active, and limit Continue to visible unfinished steps. Unknown SHALL remain distinct from incomplete. These requirements supersede the earlier within-group multi-selection behavior.

#### Scenario: Multiple groups selected
- **WHEN** Quest and Incomplete are selected, then Diary replaces Quest
- **THEN** only incomplete diary steps remain visible, Quest is deselected, and original numbers remain

#### Scenario: Live state changes or logout
- **WHEN** a visible step changes state or the character logs out
- **THEN** filter membership updates from current completion facts; logged-out facts become unknown without inheriting another account

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

### Requirement: Reset and projected skill icons
An accessible reset icon SHALL clear all type/state filters and search, cancel pending search, and restore the full route. Recognized guide projected levels SHALL display the level number followed by its local RuneLite skill icon with the skill name in a tooltip/accessibility label. Unrecognized/annotated projected text SHALL be preserved. Projected levels SHALL remain separate from live progress. Icon loading SHALL occur on a worker; Swing updates SHALL occur on the EDT and reject disposed plugin generations.

The entire rendered projected-level region SHALL be read-only, including labels, skill images and layout gaps; it SHALL retain tooltips/accessibility without inheriting parent disclosure listeners or suggesting a click action. Hover/click SHALL NOT collapse details, open a destination or affect progress/navigation.

#### Scenario: Reset while search is pending
- **WHEN** the reset icon is activated during a pending query
- **THEN** every step returns, selections/query clear, and no deferred search scroll occurs

#### Scenario: Projection rendering
- **WHEN** details contain 4 Cooking and 10 Hitpoints
- **THEN** the view shows 4 followed by the Cooking icon and 10 followed by the Hitpoints icon, without changing character levels

#### Scenario: Projection tooltip is not an action
- **WHEN** a projected skill tooltip is visible and the user clicks its number/icon or grid background
- **THEN** the parent card remains expanded, no action fires and the tooltip continues to describe a guide projection rather than a current level

### Requirement: Complete titles under RuneLite rendering
Wrapped title height SHALL be measured through the installed text UI, including RuneLite's look and feel, so all parsed destination text fits at normal narrow sidebar widths. Tests SHALL exercise RuneLiteLAF rather than relying solely on the default Swing look and feel.

#### Scenario: Balloon destination wraps to the last line
- **WHEN** Crafting Guild, Varrock, Castle Wars or Grand Tree occupies the last line of a balloon title
- **THEN** that line fits inside its title component without clipping

#### Scenario: Search start and finish order
- **WHEN** in search matches steps 124, 257 and 295
- **THEN** traversal and counter order are 124, 257, 295, even though step 257's Start prefix gives it a lower relevance score

### Requirement: Local skill icons in training titles
Recognized skill-name tokens in training instructions SHALL display the actual locally supplied RuneLite skill icon immediately before the unchanged name. The existing training category icon SHALL remain distinct. Title rendering SHALL consume guide-boundary tokens/canonical skill identities rather than reparse rendered text. Icon preparation SHALL occur on the worker, publication on the EDT, and late assets SHALL preserve dimensions and reject disposed generations. Text names, complete wrapping and accessible descriptions SHALL remain useful when an icon is unavailable. Combat SHALL use an honest derived-target treatment rather than a fabricated skill image. Filter/category/status artwork SHALL NOT be recreated or replaced during this change.

#### Scenario: Compound training title
- **WHEN** a parsed training instruction names Attack and Strength
- **THEN** each original name is preceded by its actual corresponding skill icon, while the category icon and full instruction wording remain

#### Scenario: Missing or late icon
- **WHEN** an actual skill asset is not yet available or a disposed generation receives it late
- **THEN** the skill name stays readable with stable layout, and only the active generation can apply the prepared asset
