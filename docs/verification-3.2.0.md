# QuestCape 3.2.0 combined candidate

Implementation candidate; user in-game acceptance and final rules audit remain pending.
Local branch: `codex/questcape-sidebar-ux`; do not merge, push, publish or archive.
Starting revision: `f76bdba` (prior production tree `7398b63`).

Fresh starting baseline on 15 October 2026: 92 tests passed under Temurin 11.0.22,
Gradle 8.10, offline cached RuneLite client/API **1.13.2**. The older 1.13.1 and
72-test research results are historical. Baseline log: ignored
`.tmp/refactor-baseline.log`.

There is no advertised external domain extension API or consumer in this repository;
the typed internals retain legacy cache fields and established plugin integration.
Keep public legacy constructors/signatures available as data adapters where possible;
prepare semantic values through the central assembler at producer/cache boundaries.
The compatible selection/subsection features plan a 3.2.0 minor release. Any actual
incompatible supported public/data contract still requires revising that plan first.

Master's parser accepted the four reported prose suffixes by matching required
targets without inspecting the residual instruction. The newer residual guard
prevented false completion of extra actions, but also rejected legitimate Prayer
purposes and Farming crop advice. The replacement separates supported explanations
from required clauses and retains the extra-action guard, with fresh/legacy tests.

The regression suite reproduced the recovery defect before its correction:
recovering a saved true Kudos key resurrected a pending false edit. Explicit scoped
revisioned edits replace set union. Source and UI tests remain synthetic; only the
user confirms behavior in RuneScape. No automated game input is used.

## Scope and compatibility

Change: `strengthen-questcape-domain-boundaries`, including all D01–D11 and R01–R10
from `.research/questcape-refactor-decisions.md` and the nine user findings. The
preceding `refine-questcape-sidebar-ux` candidate was **not accepted**. Its
historical results and pending acceptance/audit remain recorded; both gates now
require the combined corrected candidate. No archive, push, PR, publication, Hub
update or merge is authorized. Master stays
`40eb54a520970db9edb77720d254d5b0f1b2f207`.

Research SHA-256:
`875B912EC8D9282C063A17790463817EF50D5B99B0CDC78357B688F2A8F79FDE`.
Private training-regression capture SHA-256:
`62CB283FAE8DB36D108285F7A78FC783BB340421AAA7D0E9D8452EF851B4284D`.
Research and live-account captures remain ignored and excluded from packaging.

3.2.0 is one minor increment from 3.1.0: compatible selection/subsection features
are the highest increment alongside fixes and internal refactoring. No runtime
dependency was added. Schema-1 legacy fields, account/mode/config tokens, row
keys, both Kudos identities and inert retired keys remain compatible. Own
category wire tokens are explicit in the codec, independent of renamed enum or
display labels. Existing API Quest/Skill identities remain API-backed.
Optional evidence v2 retains v1 links and legacy fallback; unknown versions ignore
enrichment while preserving raw source. Malformed/unreadable originals cannot
authorize replacement. No destructive cache/config migration is required.

## Decision and research coverage

Production paths below are relative to `src/main/java/com/questcape/`; test names
identify files under `src/test/java/com/questcape/`.

| Decision | Implemented result and verification |
| --- | --- |
| D01 layers | Source adapter → prepared domain rows/policies → pure projection → Swing. `QuestCapeSession` composes services/UI owners. Domain evaluation performs no Client, Swing, HTTP or disk access. |
| D02 enum policies | `progress/CompletionPolicy`, typed `ClassifiedAction`, explicit quest-state mapping; shared evaluation feeds cards/filters/totals/Continue. A single local detail-presentation switch remains because it does not duplicate domain policy. |
| D03 evidence classification | `SourceRow`, `RowEvidence`, `GuideClassifier`, `QuestCatalog`, `ActivityCatalog` reconcile action-cell/row-ID identity and explicit action/stage. `DomainBoundaryTest`/`GuideParserTest` mutate wording, roles, attributes, note links, conflicts and hand-ins. Unsupported meaning stays unknown. |
| D04 passive rows | `GuideAssembler` copies prepared action/role/token/projection values into `GuideRow`; constructors and `isManual()` do not classify. `DomainBoundaryTest` checks raw construction and source/cache adaptation. |
| D05 typed identity | `QuestKey`, `SkillTargetKey`, `activity/DiaryKey`, `ActivityKey`, `BalloonDestination`, `persistence/LegacyKeyCodec`; normalization/catalogs separate. Stable-key/cache tests retain start/finish/repeat/Kudos identities and inert unknown tokens. |
| D06 activity families | Explicit `ActivityDetector`/`ActivityRegistry`: diary/balloon families and Stronghold, Museum Camp, Herbi Flax, quiz and Knight Waves detectors. Predicate tests retain all 48 diary definitions, Karamja values, separate rewards and four destinations; registry tests isolate unavailable facts/reject duplicate writers. |
| D07 separate interpreters | `TrainingTargetInterpreter`, `ProjectedLevelInterpreter`, `SkillMentionInterpreter` share `SourceTextTokenizer`/`SkillCatalog`; prepared offsets preserve Unicode/unknown text. Tests cover compound requirements, recommendations, malformed/long input and all four reported suffixes. |
| D08 metadata | `PluginMetadata.Loader`, `BoundedHttp.request`, unique `processResources` properties. `PluginMetadataTest` checks changed properties/header without a request, unsafe/missing values, single load and isolated JAR/source-set parity. |
| D09 semantic literals | `GuideSourcePolicy`, `HttpPolicy`, `CachePolicy`, `GamePredicates`, detector definitions, codec tokens, `UiCopy`/`UiMetrics` and bounded run/asset constants own meaningful values. Lexical grammar and ordinary arithmetic/syntax stay local; no global Constants class. |
| D10 cohesion | Thin `QuestCapePlugin`, per-run owner, progress/refresh/sidebar/assets/link coordinators; existing composed UI/search/navigation retained. `CardInteraction`/`DetailDisclosure` own real local interaction state. |
| D11 retained guarantees | Original run/account authority, paired `StoredView`/typed outcomes, last-good candidates, atomic originals, account isolation and explicit navigation intent. Lifecycle/repository/progress/navigation tests retain these contracts. |

| Lesson | Concrete implementation evidence |
| --- | --- |
| R01 provenance/unknown | Unsupported action, conflicting identity, missing observation and explicit eligible manual intent remain distinct; source metadata or levels cannot prove a hand-in. |
| R02 reversible intent | `PendingManualEdits` overlays scoped revisioned true **and false** edits. Only exact saved revisions are acknowledged. `ManualIntentRecoveryTest` covers unreadable recovery, unrelated/retired checks, newer failed edits and a blocked old read after restart. |
| R03 original authority | `PluginRun` cancels without waiting; helpers retain their originating run/account/service token. `PluginLifecycleTest`, `RunOwnershipTest`, `TrainingLinkOwnershipTest` cover partial startup, old startup/retained actions, scoped invalidation and stale link success/failure. |
| R04 coherent publication | Immutable content/save tuples and typed operation outcomes; display strings do not drive control flow. Old saves cannot publish status or reset a newer retry deadline. |
| R05 source evolution | Canonical table/role/span validation precedes publication; additive evidence retains legacy fallback. `GuideRepositoryTest` also proves malformed enrichment leaves the original unwritten while a fetched route remains usable as unsaved data. |
| R06 contextual compatibility | Runtime typed operands and boundary codecs; exact account scopes/Kudos tokens, inert retired keys and no cross-account inference. |
| R07 pure UI/user intent | Prepared source fields and pure `GuideProjection` remain separate from interaction. Existing search/filter/scroll tests retain ordering/membership/viewport intent; ordinary updates do not navigate. |
| R08 faults/measured work | Controlled futures, queues, mocked stores and existing clocks test interleavings without sleeps/game input. Informational preparation/projection/EDT fixture timings below; no speculative cache/job framework. Dirty-event tests verify idle ticks skip capture and IO uses workers. |
| R09 verified definitions | API quests and sourced exceptional predicates stay separate. Starts/finishes/tasks/rewards/hand-ins/manual actions retain independent evidence and guide order; no speculative coverage or later-step inference. |
| R10 delivery | Java 11 ordinary-JAR/resource/metadata tests, actual resolved dependencies, public-only guide contract, aligned docs and explicit acceptance/audit gates. |

## Nine UI findings

| Finding | Result and evidence |
| --- | --- |
| U1 training | Required clauses are separate from reviewed Prayer/Farming explanations. All three Prayer rows complete at real Prayer 50. Farming uses synthetic 40/41/44/46/50; the clipped screenshot does not establish the player's Farming level. Fresh/cache and unsupported extra-action regressions are in `TrainingEvaluationTest`. |
| U2 card clicks | No text-click delay timers. Default title/body/advice disclose immediately with hand cursor. Select text/Done selecting/Escape preserves native selection. Drag/right/repeated/consumed clicks and nested controls stay independent (`NativeSidebarUXTest`). |
| U3 related links | Independent accessible count/chevron subsection folds beside meaningful details, opens for links-only content, ignores blank/N/A and remembers user choice across parent folds/live updates. |
| U4 advice heading | Named 14-point bold heading, preserving the approved font family. |
| U5 QP styling | Quest points/Guide QP total share advice heading/body styles with separate full-width values; meanings/source preserved. |
| U6 projections | Entire panel/numbers/icons/gaps read-only/default cursor; tooltips remain, clicks do not fold/navigate, including selection mode. |
| U7 resync | Styled shell/label inert; only the embedded opposing-arrows icon is actionable/focusable, with readiness/running guards. |
| U8–U9 gaps | Named 6/8/10 logical-unit title→source, source→resync and resync→progress gaps; count/bar together. Approved icons, quiet borders, search/clear/filter artwork, fonts and full-width Continue retained. |

## Verification and limits

Final check results, exact production revision and ordinary-JAR fingerprint are
recorded below. Package tests validate own Java 11 classes (major 55), metadata,
PNG dimensions/transparency, resources and notices, excluding tests/fixtures.
Test-only URLClassLoader checks are not shipped plugin code.

Source inspection covers forbidden production reflection/process/native/game-input
paths, blocking lifecycle waits, per-tick INFO logging, dependency additions and
ClientThread/EDT IO. Metadata, local training-link definitions and images load on
workers before sidebar use. Domain evaluation, link resolution and UI projection
do no IO. Injected bounded OkHttp runs on workers; browser actions use LinkBrowser.
This inspection is **not** the post-acceptance official-rules audit or a claim of
maintainer approval.

The approximately 300-line guideline remains a cohesion prompt. The thin plugin
delegates lifecycle/capture/publication/actions. The panel composes header, cards,
details, search, filters, assets, route and navigation; remaining orchestration
includes small UI/test accessors. Parser enrichment, search state and progress
readiness are cohesive modest exceptions. No forwarding class or compressed code
was introduced for physical counts; test length is excluded.

Reviewed `NativeSidebarPreviewTest` output:
`build/ui-evidence/native-sidebar-3.2.0/` at 225×640, 240×1364, 320×900,
320×1364 with related links open/selection mode, long compound/missing-level
content with failure/unsaved notices, and 1.25×/1.5× device transforms. RuneLiteLAF,
actual local skill images and packaged artwork are used with synthetic fixtures.
These are component renders, **not live-client captures or in-game acceptance**.
Short viewport content remains scrollable with a fixed footer. Device transforms
do not verify Windows display scaling. Default-open/closed subsection and retained
choice are also checked in isolated Swing tests.

Informational measurements cover 351-row preparation, 20 pure projections and EDT
composition/layout/paint/PNG work. They include test/JIT/fixture overhead and are
not in-game capture/latency guarantees; no optimization threshold is asserted.
Linux/affected Ubuntu 22/Jagex Launcher testing is unavailable; the whole-system
freeze remains a separate unresolved investigation. Build/JVM/public-request
success cannot establish its cause or resolution.

## Required in-game acceptance

Offer `./gradlew run` (`.\gradlew.bat run` on Windows) with Java 11 and
[RuneLite's official Jagex-account development-client instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Only the user logs in and tests the exact candidate:

1. Prayer 33→37 / 38→40 / 41→43 purpose steps at Prayer 50: Complete/100%.
   Farming 41→46 with crop advice: below 41 Incomplete, 41–45 In progress, 46+
   Complete. Percentage is floored/clamped level range, not XP. No need to train
   or start content solely for review; synthetic tests cover unavailable levels.
   Confirm cached/offline source agrees.
2. Middle/edge/title/advice/status clicks: immediate disclosure/hand cursor;
   Details and card Enter/Space work. Select text, drag/word-select/copy, then
   Done selecting/Escape; folding resets the mode. Links/checks act independently.
3. Projection tooltip survives; clicking numbers/icons/gaps does not fold or move
   the viewport. Related links fold beside other details and open for links-only
   content; toggling then folding/reopening/resync retains choice. Quick guide
   stays primary; Quest page is inside related links.
4. Review advice/QP styles, header spacing and quiet icons at narrow/short/scaled
   layouts. Resync text/background inert; only its arrows sync locally. Wiki and
   GitHub actions stay independent.
5. Search/filter/clear, borderless match navigation and Continue/top/bottom retain
   behavior. Ordinary updates preserve viewport/input intent; long/compound and
   unknown content remain readable with honest status.
6. Compare known quest/miniquest start/finish, diary task/reward (Karamja included),
   balloon and supported exceptional facts with the account. Only the two Kudos
   steps are reversible/manual; check/uncheck/resync/restart retains latest intent.
7. Logout/hop/reconnect, available profile/mode switches and disable/re-enable:
   first-tick readiness, no stale facts/feedback/browser/navigation. Cached route
   remains usable during public-content failure with visible unsaved/failure state.

Record explicit acceptance of the exact corrected revision. Then re-read current
official RuneLite/Jagex rules and audit the complete production/build/resources/
config/storage/privacy/threading/lifecycle candidate. Corrections require affected
acceptance again. Tasks **9.4 and 9.5 remain pending**; do not archive either change.
