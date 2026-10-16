# quest-helper-handoff Specification

## Purpose
Record retirement of the former Quest Helper handoff and resume capability. The accepted 2026-09-23 removal and reviewer rationale are recorded in [Developer rules review](../../../docs/developer-rules-review.md); historical change archives remain historical evidence rather than current requirements.

## Requirements

### Requirement: Retired cross-plugin handoff remains absent
QuestCape SHALL NOT discover another plugin's internal components, inspect or manipulate its UI, populate its search, activate its controls, launch a quest helper or automatically resume one. Guide disclosure and validated external guide links SHALL remain independent local/sidebar or browser actions without claiming cross-plugin activation.

#### Scenario: Quest selected or character logs in
- **WHEN** the user activates a quest card or a character becomes ready
- **THEN** QuestCape does not discover, focus, alter, launch or resume Quest Helper and does not record a cross-plugin launch success

### Requirement: Obsolete saved settings remain untouched
Retired resume preferences and remembered intent values SHALL remain in existing storage without being read, written, renamed or deleted by the current plugin. Their presence SHALL NOT authorize helper actions or change current progress.

#### Scenario: Upgrade retains old resume data
- **WHEN** a profile contains legacy resume settings or a remembered helper target
- **THEN** those values remain untouched and inert while current guide/progress behavior remains available

### Requirement: Future integration requires a supported contract
Any future Quest Helper integration SHALL require a supported upstream API and a separate explicitly authorized proposal with current rules and compatibility review. Public Swing access, component discovery or a development stub SHALL NOT qualify as that supported contract.

#### Scenario: Future handoff is proposed
- **WHEN** a future change requests cross-plugin integration
- **THEN** a supported upstream API and separate authorized proposal are required before implementation, without reviving the retired UI fallback or resume path
