## ADDED Requirements

### Requirement: Skill training opens the relevant Theoatrix guide
The plugin SHALL map recognized training skills to verified relevant Theoatrix guide URLs and open the destination in the system browser when the player activates the training action. Compound training rows SHALL offer a distinct destination for each applicable skill.

#### Scenario: Known skill training
- **WHEN** the player clicks a training step with a verified skill-guide mapping
- **THEN** the relevant Theoatrix guide opens in the browser without starting a quest helper

#### Scenario: Several skills in one step
- **WHEN** a training row names several skills with different destinations
- **THEN** the player can select the intended skill guide rather than being sent to an arbitrary first match

### Requirement: Missing destinations have an honest fallback
The plugin SHALL expose a clearly labeled Browse Theoatrix guides fallback to https://www.theoatrix.net/all-guides for missing skill mappings and SHALL report browser-opening failures with a retry or copy-link recovery.

#### Scenario: Unmapped skill
- **WHEN** a training skill has no verified destination
- **THEN** the player is offered the all-guides directory explicitly as a fallback instead of a guessed skill URL

#### Scenario: Browser invocation fails
- **WHEN** the browser cannot be opened
- **THEN** the plugin displays the failure and offers recovery without changing completion

### Requirement: External links are user-initiated and independent of progress
The plugin SHALL permit only HTTPS Theoatrix destinations for training actions, validate any followed redirects during resolution, and open links only on user activation. Browser navigation SHALL NOT mark a training row complete.

#### Scenario: Unsafe destination
- **WHEN** a proposed skill-guide destination has an unexpected host or unsafe URL scheme
- **THEN** it is rejected and only the known Theoatrix directory fallback is offered

#### Scenario: Guide opened before training
- **WHEN** a player opens a valid skill guide while below its target level
- **THEN** the training row remains incomplete until actual skill observations reach the target
