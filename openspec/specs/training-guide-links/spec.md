# training-guide-links Specification

## Purpose
Provide explicitly labeled, validated skill-guide destinations and safe user-initiated browser or copy actions without changing character progress.
## Requirements

### Requirement: Skill training opens the relevant Theoatrix guide
The plugin SHALL map recognized training skills to verified relevant Theoatrix guide URLs and open the destination in the system browser through LinkBrowser when the player activates the training action. Its visible label SHALL identify `Theoatrix guide ↗`; compound training actions SHALL identify their individual skills and offer a distinct destination for each applicable skill. A source-provided Wiki skill destination SHALL be visibly labeled `Wiki: <skill> ↗` and retain the separate safe-Wiki validation policy. Presentation SHALL NOT create new background requests, broaden host/redirect acceptance or substitute an arbitrary first skill.

#### Scenario: Known skill training
- **WHEN** the player activates a training action labeled Theoatrix guide with a verified skill-guide mapping
- **THEN** the relevant Theoatrix guide opens without starting a quest helper or toggling card details

#### Scenario: Several skills in one step
- **WHEN** a training row names several skills with different destinations
- **THEN** the player can select the individually named skill/provider action rather than being sent to an arbitrary first match

#### Scenario: Wiki and Theoatrix destinations coexist
- **WHEN** a Mining row has both safe Wiki and verified Theoatrix destinations
- **THEN** visible labels distinguish `Wiki: Mining ↗` and `Theoatrix guide ↗`, while validation remains specific to each provider

### Requirement: Missing destinations have an honest fallback
The plugin SHALL expose a clearly labeled Browse Theoatrix guides fallback to https://www.theoatrix.net/all-guides for missing skill mappings and SHALL report browser-opening failures with a retry or copy-link recovery.

#### Scenario: Unmapped skill
- **WHEN** a training skill has no verified destination
- **THEN** the player is offered the all-guides directory explicitly as a fallback instead of a guessed skill URL

#### Scenario: Browser invocation fails
- **WHEN** the browser cannot be opened
- **THEN** the plugin displays the failure and offers recovery without changing completion

### Requirement: External links are user-initiated and independent of progress
The plugin SHALL permit only HTTPS Theoatrix destinations for Theoatrix training actions, validate any followed redirects during resolution, and open links only on user activation. Source Wiki actions SHALL retain their existing separate scheme/host validation. Browser navigation SHALL NOT mark a training row complete or change its evaluated state, percentages or account data. Cancellation and stale callback rejection SHALL remain effective after disposal/account changes.

#### Scenario: Unsafe destination
- **WHEN** a proposed skill-guide destination has an unexpected host or unsafe URL scheme
- **THEN** it is rejected and only the known Theoatrix directory fallback is offered for that provider

#### Scenario: Guide opened before training
- **WHEN** a player opens a valid skill guide while below its target level
- **THEN** its existing incomplete or in-progress state remains determined only by observed real levels and supported training evidence

#### Scenario: Late link resolution
- **WHEN** a user-triggered resolver completes after the originating plugin generation or action has been invalidated
- **THEN** it does not open a browser or update a different card's feedback
