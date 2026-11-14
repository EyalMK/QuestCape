## MODIFIED Requirements

### Requirement: Evidence-based action classification
The source adapter SHALL preserve bounded sanitized row attributes, originating column/cell roles, safe semantic links and structured skill evidence with the original source content. Central classification SHALL reconcile supported identity and action candidates, expose typed matched/unsupported/ambiguous outcomes and reasons, and use narrow text fallback only when justified. Source metadata SHALL be a hint rather than an automatic completion authority. Notes, projections, arbitrary full-width layout and absence of a start prefix SHALL NOT establish action identity or stage. Structural candidate failure SHALL retain last-good content; valid unsupported rows SHALL remain visible and unknown.

When an ordinary quest row lacks a catalog entry, the classifier SHALL require the action and article title to agree, a matching quick-guide link in the quick-guide column, and positive individual quest points. Any supplied row identity SHALL agree with the article. A qualifying row SHALL use its explicit start or finish action.

#### Scenario: Wording changes with compatible evidence
- **WHEN** display wording changes while recognized source identity and requested action evidence remain compatible
- **THEN** the same typed action and policy are produced without needing an exact title match

#### Scenario: Unrelated note or conflicting identity
- **WHEN** a note links an unrelated quest, or action-cell identity contradicts a supported row-ID identity
- **THEN** the note does not replace action identity and the conflicting supported candidates produce ambiguity rather than first-match completion

#### Scenario: Hand-in has skill metadata
- **WHEN** Herbi Flax's hand-in carries Herblore/level metadata
- **THEN** it remains a hand-in requiring its authoritative fact, not a level-completion action

#### Scenario: Malformed optional evidence
- **WHEN** a miniquest-start row contains malformed numeric skill metadata but independent supported stage/identity evidence
- **THEN** that attribute is rejected and the verified start meaning is preserved without inventing a target

#### Scenario: Invalid table or unfamiliar action
- **WHEN** required canonical columns duplicate or spans are invalid, or an otherwise valid row has unfamiliar semantics
- **THEN** structural invalidity rejects the candidate while unfamiliar semantics retains a visible unknown row with original content and typed reason

#### Scenario: Newly released quest arrives in the Wiki guide
- **WHEN** an uncatalogued full quest has a consistent action, canonical article link, matching quick-guide link in its own column and positive individual quest points
- **THEN** the row is classified as a quest with its explicit start or finish policy, and completion still requires local game evidence

#### Scenario: Cached quest row has sufficient evidence
- **WHEN** a cached row previously classified as unknown retains the evidence needed to identify a full quest
- **THEN** the classifier recognizes it without another guide request

#### Scenario: Quest-like row lacks supporting evidence
- **WHEN** an uncatalogued row has a conflicting row identity, a quick-guide link in the wrong column, or an unsupported action
- **THEN** the row remains visible without automatic quest completion

### Requirement: Typed runtime identity and compatible codecs
The plugin SHALL use existing API Skill/Quest enums and small typed values for derived combat, extra miniquests, diary actions, balloons and exceptional facts. The plugin SHALL support a normalized game-title identity for full quests absent from the RuneLite enum. Source aliases, normalization and legacy storage codecs SHALL have distinct owners. Runtime evaluation SHALL NOT concatenate or inspect legacy syntax such as DIARY:, :start or :repeated:. Codecs SHALL preserve exact account/mode/config tokens and both eligible Kudos keys, retain unknown/retired saved keys inertly and reject ambiguous alias mappings. Changing enum/display names SHALL NOT rename serialized identities implicitly.

#### Scenario: Legacy row and manual records
- **WHEN** old schema-1 rows and account/manual records are decoded and reserialized
- **THEN** compatible stored tokens and both Kudos keys survive, retired keys cannot authorize automatic completion, and source-order/repetition changes do not merge distinct actions

#### Scenario: Alias collision
- **WHEN** two definitions claim the same reviewed normalized source alias for different identities
- **THEN** definition validation reports a controlled conflict rather than choosing the last definition

#### Scenario: A tracked game title joins the RuneLite enum
- **WHEN** a saved quest observation keyed by normalized game title is read with an enum entry for that title
- **THEN** the observation is readable through the enum identity, with an existing enum-keyed observation taking precedence
