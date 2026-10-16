## ADDED Requirements

### Requirement: Training verification works with standard packaged metadata
An activated Theoatrix action SHALL be able to build its validated HTTP request using the plugin-owned metadata resource in development and standard Plugin Hub packaging. Successful response handling SHALL retain the existing relevant-skill destination, browser ownership, silent success and progress independence. Missing or invalid metadata SHALL retain the existing controlled failure behavior.

#### Scenario: Magic training after a fresh plugin start
- **WHEN** the player activates the Magic Theoatrix action after startup and the allowlisted destination returns a successful response
- **THEN** the Magic guide opens through LinkBrowser without a metadata-unavailable message or a progress change

#### Scenario: Another skill or compound training action
- **WHEN** the player activates another skill's action or an individually named action in a compound step
- **THEN** the same available validated product metadata supports verification of that skill's configured destination

#### Scenario: Retained recovery and copy action
- **WHEN** verification fails for a real transport/browser problem or the player uses Shift-right-click to copy
- **THEN** the existing actionable failure or local copy behavior remains available without marking training complete
