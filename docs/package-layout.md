# Source package map

QuestCape groups Java code by responsibility. Enums, interfaces and classes live
together with the code that owns their meaning. There is no global `enums`,
`interfaces`, `helpers` or `utils` dumping ground. Source folders match Java
package declarations, and tests mirror the package they exercise.

`QuestCapePlugin` remains the RuneLite entry point at `com.questcape`. Its name,
plugin registration, resources, configuration keys and persisted JSON do not move.
`persistence` continues to own storage mechanics and legacy wire-key codecs.

## Guide content

Under `src/main/java/com/questcape/guide/`:

| Package | Responsibility and starting points |
| --- | --- |
| `model` | Prepared immutable route/action values: `GuideRow`, `GuideSnapshot`, `ClassifiedAction`. Constructors validate/copy values; they do not classify titles or read the client. |
| `source` | Sanitized source evidence, source-column roles and source constraints: `SourceRow`, `RowEvidence`, `GuideFieldRole`, `GuideSourcePolicy`. |
| `parsing` | Bounded HTML/table extraction and offline reclassification: `GuideParser` and its package-private `HtmlTree`. |
| `classification` | Reconcile identity/action evidence and assemble rows: `GuideAssembler`, package-private `GuideClassifier`, `QuestCatalog`, `ActivityCatalog`. Start at the assembler's public producer boundary. |
| `interpretation` | Required training targets, projected levels and decorative skill mentions, plus their shared vocabulary/tokenizer. `TrainingInstruction`, `TrainingTargetInterpreter`, `ProjectedLevelInterpreter` and `SkillMentionInterpreter` retain distinct meanings. |
| `repository` | `GuideRepository`: last-good guide publication, bounded refresh/cache lifecycle and persistence state. It is not a parser or completion evaluator. |

When adding a new source alias, edit the relevant classification catalog. When
changing required training grammar, use `interpretation` and its regression tests;
do not treat advice or projected rewards as required targets. Add supported
evidence fields in `source`, preserving existing cache/wire spellings.

## Integration and application coordination

Under `src/main/java/com/questcape/integration/`:

| Package | Responsibility and starting points |
| --- | --- |
| `http` | `BoundedHttp`, `HttpPolicy`, `PluginMetadata`: bounded transport and validated packaged request identity. |
| `links` | `BrowserLinks`, `ApplicationLinks`, `TrainingGuideResolver`, `TrainingLinkController`: destination policy, provider lookup, explicit browser/copy actions and original-run authority. |
| `coordination` | `ProgressCoordinator` and `GuideRefreshCoordinator`: event coalescing, readiness, asynchronous handoff and typed operation outcomes. |
| `lifecycle` | `PluginRun`: per-enable resources, executor ownership, cancellation and idempotent nonblocking close. |
| section root | `QuestCapeSession` is the composition root wiring the other packages. `OperationOutcome` is their shared immutable result contract. |

Change network bounds in `http`, link destinations in `links`, and scheduling or
stale-work rules in `coordination`/`lifecycle`. Keep these decisions out of views.
Client reads stay on ClientThread, blocking IO/assets on workers and widgets on
EDT. Shutdown must cancel owned work without waiting.

## Character progress

Under `src/main/java/com/questcape/progress/`:

| Package | Responsibility and starting points |
| --- | --- |
| `model` | Immutable observed account facts and checked quest keys: `AccountProgress`, `QuestKey`. Missing evidence remains unavailable. |
| `evaluation` | Pure `Completion`, `CompletionPolicy`, `TrainingEvaluation`: one completion/percentage authority shared by presentation, filters, totals and navigation. |
| `tracking` | `ProgressService` and package-private `PendingManualEdits`: account-scoped publication, persistence recovery and latest true/false manual intent. |
| `capture` | `LiveProgressReader` and `ActivityRegistry`: ClientThread observation and explicit detector composition/duplicate-output checks. |
| `activity` | `ActivityDetector` and `ActivityObservation`: narrow observation contracts. |
| `activity.definitions` | Typed activity/diary/balloon keys, reviewed mappings and exact predicate constants. Shared source recognition and capture use the same definitions. |
| `activity.detectors` | The seven concrete diary/balloon/reward/miniquest detector families. Each reads only its declared facts. |
| `compatibility` | `Identities` and `ActivityProgress`: retained legacy adapters. New behavior belongs in the focused owner, not another facade condition. |

For new automatic activity coverage, define the supported key/mapping, implement
the appropriate detector family and register it explicitly in `capture.ActivityRegistry`.
Test exact positive/negative/unavailable boundaries. Never infer a collected
reward from a skill level. Storage token changes require an explicit migration.

## Sidebar UI

Under `src/main/java/com/questcape/ui/`:

| Package | Responsibility and starting points |
| --- | --- |
| `panel` | `GuidePanel` composition and its tightly coupled header, card/detail, route, navigation, search/filter and prepared-image collaborators. Their internal widget state stays package-private. |
| `interaction` | `CardInteraction`, `InteractionRole`, package-private `SelectionHint`, `LinkCopyGesture`: pointer/selection roles, delayed hints and transient copy feedback, with explicit cleanup. |
| `style` | `UiComponents`, `UiTheme`, `UiMetrics`, `UiCopy`, `GuideControlIcons`, `StepIcons`: shared presentation primitives, copy, sizing and artwork. Icons depend on the theme directly. |
| `sidebar` | `SidebarController`, `SidebarAssets`, `SidebarImages`: run-scoped RuneLite attachment, coherent EDT publication and worker-prepared local assets. |
| section root | `GuideActions` is the application-action interface; `SidebarStatus` is the immutable display-status contract. Leaf interaction code does not depend on the panel implementation. |

The `panel` package intentionally keeps its seventeen cooperating implementations
together. Search/navigation share viewport intent; cards and image bindings share
EDT state. Splitting every widget into another package would expose mutable
fields or add forwarding interfaces without a useful boundary. A future split
needs a real narrow contract first. Keep changes local to the existing owner:

- Card content/order: `GuideRowCard`, `GuideRowDetails`, `GuideDetailProjector`.
- Training-circle presentation: `TrainingProgress`; calculation remains in progress evaluation.
- Search matching: `StepSearch`; traversal/debounce in `GuideSearchController`.
- Scroll anchors/Continue: `GuideNavigator`; fixed footer controls in `GuideNavigationBar`.
- Pointer selection/copy hints: `interaction`; shared visual tokens in `style`.
- Enable/disable, asset loading and client sidebar registration: `sidebar`, not card widgets.

Public visibility at a package boundary permits internal composition; it is not
a promise of a supported external extension API. Keep implementations and mutable
fields private/package-private unless a real cross-package caller needs them.
Do not add public test hooks: `TestGuideLinks` is a test-source-only fixture for
interaction tests using real package-private panel links.

## Maintenance checks

Run the Java 11 build after a package move. Update imports, qualified references,
test packages and any current task/document references together. Keep plugin
entry-point metadata and classpath resource names stable. This branch remains
**3.1.0** throughout its follow-ups. Package organization does not change route
meaning, cache schemas, lifecycle ownership or user interaction behavior.
