## MODIFIED Requirements

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
