## Context

This is the general refactor proposal that the prior UI-only change deliberately excluded. It adopts `.research/questcape-refactor-decisions.md` D01–D11 and R01–R10, read alongside `questcape-source-design-followup.md` and `runelite-community-review-guide.md`. Those research documents describe pinned comparator sources; recommendations are not universal community rules or evidence that open upstream PRs were accepted.

Starting checkout: `33434a8`, production `7398b6351cbf7ca631b8bf01d2aa266cd3460abd`, local `codex/questcape-sidebar-ux`, based on master `40eb54a`. The earlier candidate recorded 92 passing tests on Java 11/RuneLite 1.13.1; that is historical evidence, not a new run for this proposal. Its user acceptance and final audit remain pending. The current source has already composed the panel, shared skill names and training evaluation, and introduced paired views/run fences. Inspect and reuse these rather than repeating the 3.0.1 research's now-stale file counts or treating every recommendation as unimplemented.

Research-decision input SHA-256: `875B912EC8D9282C063A17790463817EF50D5B99B0CDC78357B688F2A8F79FDE`.
User attachment SHA-256: `62CB283FAE8DB36D108285F7A78FC783BB340421AAA7D0E9D8452EF851B4284D`.
An unmodified private copy is retained at `.research/evidence/refactor-followup/2026-10-15-training-regressions.png`; do not publish it or ship it as artwork.

### Evidence for the nine follow-up items

| User item | Evidence/current cause | Required correction |
| --- | --- | --- |
| U1 four training rows | Screenshot shows Prayer 33→37/38→40/41→43 as Unknown at level 50; Farming 41→46 is partly visible. Pinned HTML lines 744–746, 1344–1346, 1582–1584, 2623–2625 contains all four full instructions and matching skill/upper-level attributes. `TrainingInstruction` rejects residual non-connector prose; `GuideClassifier.readTargets` consequently omits its legacy map. | Preserve pure training and all explanatory text without counting prayer purpose links or crop advice as further requirements. The screenshot does not establish Farming's actual level; tests must use synthetic known values. User reports master does not regress; characterize its outputs instead of assuming every old heuristic is correct. |
| U2 delayed middle click | `GuideRowCard` recursively installs disclosure on text and uses a native double-click timer, while backgrounds/buttons act immediately and use different cursors. | Instant release/click without a deliberate timer; hand cursor on ordinary disclosure text/body. User explicitly selected **instant clicks; explicit text selection** on 2026-10-15. |
| U3 related links | `GuideRowDetails` currently emits heading and every link directly. | Independent subsection, folded by default except when its links are the only meaningful details. |
| U4 advice heading | Currently 13-point metadata bold. | Modest 14-point bold heading token. |
| U5 QP styling | Quest points is 14-point plain inline; guide QP is 13-point muted inline. | Same labeled-section heading/value pair as advice, preserving the different meanings. |
| U6 projected levels | Tooltip labels/grid inherit the card's recursive listener. | Read-only region: hover tooltip retained, click causes no disclosure, action, navigation or completion change. |
| U7 resync hit surface | Entire `TrailingIconButton` is actionable. | Inert styled shell/label and separate icon-only accessible button. |
| U8 title/source/resync gaps | Box-layout rows are adjacent. | Explicit named vertical separation without altering the approved iconography. |
| U9 resync/summary gap | A small existing strut does not sufficiently group sections. | Increase section gap while retaining completion count/bar as one group. |

This attachment is visual evidence and a user report, not instructions embedded in an image and not proof of behavior under synthetic tests. No RuneScape input is automated.

## Goals / Non-Goals

**Goals:** keep the layered design; centralize semantic ownership; correct the reported regressions; preserve exact predicates, source content, identity compatibility, account isolation and explicit navigation; make failure and run ownership testable; implement all D/R outcomes with visible coverage.

**Non-Goals:** rewriting the toolkit, global event/reducer/job/component frameworks, reflective discovery, a class per ordinary quest/diary tier, route optimization, Quest Helper handoff, downloaded rules/assets, new automatic activity coverage, telemetry, per-row network enrichment, an XP/time progress estimate, a hard class-length gate, publication or Ubuntu freeze diagnosis.

## Decisions

### 1. Evolve the layers with prepared domain values (D01, D02, D04, D05)

Keep the flow: inert Wiki adapter → source evidence → classifier/assembler → immutable domain action → pure completion/projection → Swing. ClientThread detectors separately produce immutable observations; application coordinators own scheduling and publication; IO adapters own transport and codecs. The domain does not call Client, Swing, storage or browsers.

`SourceRow` preserves original display content separately from sanitized evidence. `ClassificationResult` owns outcome (`MATCHED`, `UNSUPPORTED`, `AMBIGUOUS`), typed reason, typed action identity, policy operands, explicit quest stage, manual eligibility and repetition ambiguity. A `GuideAssembler` constructs `GuideRow` from it. Row constructors perform copying and invariant validation only: no title parsing, activity lookup, key substring checks or `isKudos` calls. Derived training/token interpretation happens once at the source/legacy adapter boundary, not in constructors or on every render.

Use existing RuneLite `Skill`/`Quest` enums. Add only needed typed values: `QuestKey` for API/extra miniquests, `ActivityKey`, diary region/tier/action, balloon destination, `SkillTarget` for ordinary/derived combat targets, and classification/evaluation reasons. Use enum-specific behavior or an explicit `EnumMap` for reusable closed policies (start, finish, training, activity fact, manual Kudos, information, unknown). Avoid repeated dispatch and `State.valueOf(otherEnum.name())`; a localized presentation conversion is acceptable and documented. No injected mutable state or substantial detector logic in enum singletons.

Separate source normalization, reviewed quest/skill aliases, fact definitions and `LegacyKeyCodec`. Only boundary codecs interpret `DIARY:`, `:start`, `:repeated:` and existing persisted spellings. Alias collisions and contradictory operand/policy combinations fail controlled validation. Unknown stored keys are retained/inert; enum renaming cannot rename a wire token. Manual key compatibility is explicit even when display wording becomes more resilient.

Alternative rejected: moving ternaries into another factory or adding enums while still parsing storage strings in completion would preserve the ownership bug. A small typed action is sufficient; no general requirement engine is needed.

### 2. Reconcile evidence rather than trusting a title cascade (D03, D07)

Preserve recognized `data-rowid`, `data-skill`, `data-skill-level`, originating cell roles, validated canonical link targets/fragments and structured skill spans. Preserve source positions/offsets needed for annotation. Do not cache arbitrary HTML, image bodies, scripts or unbounded attributes. Treat these values as hints, not a stable Wiki API or replacement manual key.

At the boundary:

1. Resolve canonical column roles; validate spans, uniqueness, required columns and whole-candidate bounds. Shape errors reject the candidate and retain last-good content.
2. Produce explicit candidate interpretations from row IDs/aliases, action-cell links, action grammar and typed attributes. Notes/projections cannot supply an action identity or completion target.
3. Reconcile compatible signals without hidden first-match priority. Identify start/finish, task/reward and hand-in/training separately. A hand-in plus a skill target remains a hand-in. Canonical identity does not alone prove its requested stage/action.
4. Contradictory supported identities/targets produce a typed ambiguous outcome; malformed optional attributes can be discarded if remaining independent evidence suffices. A valid unfamiliar action stays visible/unknown with its raw source.
5. Use narrow legacy text/link fallback where stronger evidence is missing. Never infer a finish merely from absence of “start”, information merely from punctuation/colspan, or completion from a fuzzy title match.

The table reader can be extracted for evidence ownership, not because the old parser was 303 lines. Validate current public markup during implementation before relying on attributes. That check is public-content-only; do not claim this proposal freshly fetched or verified the live source.

### 3. Repair training with distinct required and explanatory spans (U1, D07, R01/R09)

The failing pinned instructions are:

- `Train Prayer from level 33 to level 37 for Protect from Magic`
- `Train Prayer from level 38 to level 40 for Protect from Missiles`
- `Train Prayer from level 41 to level 43 for Protect from Melee`
- `Train Farming from level 41 to level 46 - You can work on the 2 other skills while letting crops grow.`

Recognize a pure-training action first. Parse its required clauses into ordered targets and its reviewed purpose/advice suffix into separate retained explanatory spans. Fresh structured skill/upper attributes reinforce those clauses; they never independently turn another action into training. The linked “Magic” prayer purpose must not become a Magic skill requirement. Skill annotations may still decorate the original text without becoming required targets.

Use a bounded tokenizer and small grammar with explicit clause boundaries, supported purpose/advice forms and typed unresolved-required reasons. Grammar support covers the four source examples, compound training, punctuation/spacing variants and legacy retained prose, with no exact-row-number exception. Only recognized explanatory spans are excluded from requirements. `...and hand in the diary...`, another unsupported skill/goal, conflicting metadata, or an unrecognized required clause still blocks false Complete. Do not fix this by deleting the residual check, ignoring all text after the first target, or trusting only `data-skill-level`.

Pure `TrainingTargetInterpreter`, `ProjectedLevelInterpreter` and `SkillMentionInterpreter` use a shared catalog/tokenizer and narrow role-specific contracts. Common infrastructure must have a real common caller; avoid a generic interpreter dispatcher solely for three unrelated result types. Projected values never feed completion; unknown annotated projections preserve original text. Preserve original Unicode offsets, full prose and approved local icons. Named lexical regex is acceptable; complex action policy must not become another giant regular expression.

Retain the existing real-level evaluator as one authority, adapting it to typed operands. Below X is Incomplete; X≤L<Y is In progress; L≥Y is Complete. Percent is floor/clamp of the level interval; 54 in 52→55 = 66%. Invalid/absent base omits percentage but an independently valid goal can still evaluate. Missing levels are not zero. Every required compound target must be complete; partial/unknown reasons remain distinct. Unknown must explain genuinely missing/ambiguous required meaning, not harmless purpose/advice prose.

### 4. Observe different activities through explicit detectors (D06)

Introduce a narrow `ActivityDetector` contract: declare supported typed keys; capture on ClientThread under established readiness; return immutable known/unknown facts. Compose an explicit immutable registry with duplicate-key validation. A missing/failed individual observation is unknown for those keys; do not fill all absent facts with false or erase unrelated known facts.

Initial distinct families: `DiaryTierDetector`, `BalloonUnlockDetector`, `StrongholdCoinsDetector`, `MuseumCampRewardDetector`, `HerbiFlaxHandInDetector`, `NaturalHistoryQuizDetector`, `KnightWavesDetector`. Shared records/enums parameterize 48 diary tiers and four balloon destinations. API-covered quests stay with a generic Quest reader. The same typed definitions connect source recognition to observation; detectors never parse titles. Preserve Karamja 2-versus-1 values, independent reward flags, three Stronghold coin rewards without boots, Peter's reward, the Herbi bit, quiz reward and Knight Waves boundaries. Use named gamevals and documented exact comparison constants.

Prefer an interface; add a base class only if it enforces a genuinely shared invariant. A registry is ordinary composition, not an extensible plugin system, package scan or ServiceLoader.

### 5. Repair manual intent and coherent publication before broad moves (D11, R02–R05)

Current recovery unions saved and pending checked sets, so a pending removal can be lost. Track account-scoped pending edits as explicit true/false values with sequence/revision authority. Once readable saved data is recovered, overlay the latest edit for each key onto the saved set; untouched saved checks survive, pending false removes a check. Do not discard edits until a successful compatible save of their captured revision. A late save can acknowledge only the edits it included, not later edits. Unknown/corrupt schema files remain preserved; no forced overwrite is a recovery shortcut. Retired non-Kudos edits remain rejected.

Retain the existing paired `StoredView` mechanism and test exact content/save pairing, pending/unsaved status, guide/account independent freshness and account invalidation. Extend typed operation reasons where needed; messages format outcomes and never drive logic. Use controlled clocks/queues/store faults rather than sleeps. Preserve bounded retry/write coalescing; repeated unchanged facts do not write every tick.

### 6. Extract real run/coordinator ownership (D10, R03/R08)

`QuestCapePlugin` becomes descriptor/injection/event delegation. A per-enable `PluginRun` owns its original authority, executors, calls, timers/registrations and idempotent close. Initialize resources incrementally with cleanup on partial startup. Shutdown cancels and returns promptly, never joining/awaiting IO. Every continuation and queued EDT action closes over the original run/account session; no helper adopts `generation.get()` after invalidation.

Compose cohesive capture/readiness, guide refresh, sidebar publication/assets and training-link action owners. Proposed names (`ProgressCoordinator`, `GuideRefreshCoordinator`, `SidebarController`, `SidebarAssets`, `TrainingLinkController`) describe responsibilities, not a mandatory class count. Keep services/domain state outside the run resource object so it cannot become a replacement god class. Reuse the current search/navigator/card components and pure projections; do not extract one-line forwarding services or replace tested algorithms. State capture stays ClientThread, IO/assets on workers, UI mutation EDT; browser actions remain explicit through LinkBrowser with existing independent destination policies.

Review roughly 300 production lines as a cohesion prompt. Cohesive modest exceptions are allowed; tests excluded. Never compress code or split a reasonable parser merely to satisfy a count. Measure capture/preparation/EDT work before caching or speculative optimization. Retain event coalescing and structural reuse.

### 7. Make metadata and semantic literals authoritative (D08, D09, R10)

`processResources` includes the root `runelite-plugin.properties` as the single classpath resource for both development and ordinary JAR; remove duplicate explicit JAR inclusion. Load an immutable `PluginMetadata` once on a worker/HTTP boundary. Validate required displayName/version and derive an HTTP product token from a header-safe name; reject CR/LF/control characters. Preserve raw metadata for UI/docs but never send identity, credentials or paths. An invalid resource disables the affected transport with a typed actionable error, while available local data remains usable. No hardcoded fallback version, working-directory read or blocking lifecycle future.

Own meaningful literals by domain/protocol: HTTP headers/status/limits, endpoints/hosts, cache categories/schema/tokens, queue/cooldown units, game comparisons, UI copy/theme/metrics. Use existing RuneLite constants first. Name meaningful one-use values; do not create a universal Constants class or constants for ordinary arithmetic zero/one/empty strings. Keep exact legacy wire syntax in codecs. Verify candidate version, properties, generated User-Agent and documentation from both isolated JAR and source-set classpaths.

### 8. Immediate disclosure with explicit selection and interaction regions (U2/U6)

User selected instant clicks with explicit text selection. Remove the timer-based text-click disambiguation. Default ordinary body/title/status/advice uses a hand cursor and toggles once on an ordinary left click/release, with no application-imposed delay; a drag cancels the click. Do not toggle on mouse press, right-click, repeated click counts, tooltip hover or an event consumed by a nested control. Enter/Space works only for the focused card. No game input is involved.

Provide a compact accessible `Select text` action in expanded details. It enters a card-local selection mode: disclose stays open, selectable text uses the text cursor, drag/double-click/Ctrl+C work normally, and selection events do not disclose. Its action becomes `Done selecting`; Escape in that mode exits without an unrelated navigation action. Selecting does not mutate progress. The state is ephemeral/EDT-owned, not serialized; ordinary live updates preserve it on retained cards, structural replacement/disposal clears it safely. Independent nested actions remain independent in both modes.

Use explicit closed interaction roles/owner components for disclosure surfaces, independent controls and read-only content; avoid broad recursive “everything except AbstractButton” registration or string client-property protocols. A whole projected-level panel (labels, icons, gaps) is read-only/default cursor; keep its tooltip and accessibility description. Clicking it neither toggles nor calls a browser, and percentages/projections remain separate from observed progress. Accessibility focus for interactive controls stays visible. This explicitly supersedes the earlier direct-selection-with-delay contract.

### 9. Details subsection and typography (U3–U5)

Related wiki pages is an independent toggle with count, chevron and expanded state. Its link area starts folded when any other meaningful detail content exists; starts unfolded when it is the sole meaningful detail block. Meaningful content includes a valid primary quick guide, nonempty advice/projection/QP/location or substantive raw field; blank/N/A placeholders and the Details toggle itself do not count. An empty related list produces no subsection. All validated destinations, source labels and distinct anchors remain available after expanding; Quest page stays inside it and Wiki quick guide remains primary outside it.

Determine the initial state in the pure detail projection from actual content, not row category or completion. Once the user changes it, preserve that card-local preference across parent fold/unfold and ordinary live updates; reset/recompute only when its relevant source content changes or the row is replaced. Subsection pointer/keyboard activation cannot bubble into parent disclosure. Use the same anchor/intent owner for explicit reflow, never timer-driven scrolling.

Introduce a small shared heading/value renderer rather than separate QP label formats. Start `DETAIL_HEADING` at 14 points bold/muted (up from advice's 13), value/body at existing 14 points plain/TEXT. Apply this pair to Route advice, Quest points and Guide QP total, with each QP value under its label; keep those different meanings and all original content. Header 18, card title 16, metadata minimum 13 and font family stay. Native preview/manual review can tune named spacing within this direction; no new font setting is proposed.

### 10. Inert resync shell and clearer header groups (U7–U9)

Replace the full-width button with a same-sized panel shell using current fill, quiet border, label and arrow alignment. Only an embedded opposing-arrow icon button receives pointer/keyboard activation, tooltip, focus and enabled state. Shell and label use the default cursor, have no action listener and do not focus/trigger sync. Keep a reasonable icon hit target (initial 30–34 logical units within the 34-unit shell), a meaningful accessible Resync Progress name and visible focus. Readiness/running guards and forced local observation remain unchanged; Wiki refresh/GitHub stay separate.

Use named initial vertical gaps: 6 logical units title→source, 8 source→resync, 10 resync→completion group. Count and progress bar remain close together as one unit. Retain title/logo horizontal gap, quiet tooltip/borders, current filters, borderless search match navigator with X, source attribution and full-width footer. Verify short/narrow native layouts so added spacing does not consume an unreasonable fraction of the route; multiline failures remain visible without becoming routine banners.

### 11. Compatibility and staged scope integration

The prior `refine-questcape-sidebar-ux` change is a prerequisite effective delta: its source/link/search/training/footer improvements remain. New MODIFIED requirements use that effective version for overlapping requirements and explicitly replace the delay, projected-level click surface and header/detail contracts. Some names were added by that still-active change and are not yet in `openspec/specs`; archive order must be predecessor then successor only after valid combined acceptance/audits. Do not archive either now or mark the failed 7398b63 candidate accepted. If delivery uses a single combined candidate, update predecessor acceptance bookkeeping to reference the corrected tested revision and superseded requirements transparently, preserving historical evidence.

Retain schema 1 legacy fields with optional versioned source enrichment. Enrichment version 1 already exists for cell links: retain/read it, introduce a new version only for the richer evidence contract, and adapt absent/v1/unknown versions without pretending metadata exists. Old readers still consume legacy fields. Fresh and cached equivalent evidence should classify equivalently; no network requirement for browsing. Manual keys remain exact for both Kudos actions; source wording/id changes do not silently rename or merge checks. Any intentional action identity migration requires a reviewed alias/migration and original retention first.

Preserve existing public contracts where actually supported; inventory consumers before changing signatures. Internal typed redesign alone is not a reason for a major release. Plan 3.2.0 for compatible added selection/subsection behavior plus fixes/refactor; if public API or data compatibility cannot be preserved, revise to a major 4.0.0/migration before coding that break. Proposal preparation changes no source or version. Keep all work local/unmerged; publication remains separately authorized.

## Coverage matrix

No decision below is merely background reading. Tasks are acceptance-bearing; class names remain indicative.

Requirement references in the table below use these capability names: **Domain** = `guide-domain-model`, **Run** = `plugin-run-ownership`, **Metadata** = `plugin-product-metadata`, **Progress** = `guide-progress-tracking`, **UI** = `optimal-guide-content`, **Navigation** = `guide-navigation`, **Storage** = `resilient-local-storage`, **Release** = `release-verification`. Existing unchanged requirements and predecessor deltas still apply.

| Decision/finding | Requirement ownership (exact titles) |
| --- | --- |
| D01 / D04 / R07 | Domain: Prepared immutable row semantics; Run: One enabled run owns its resources |
| D02 / D09 | Domain: Cohesive semantic constants and exhaustive policies; Prepared immutable row semantics |
| D03 / R01 / R05 | Domain: Evidence-based action classification; Storage: Preserve recoverable saved data |
| D05 / R06 | Domain: Typed runtime identity and compatible codecs |
| D06 / R09 | Progress: Explicit detector-family observation; Completion reflects the activity |
| D07 | Domain: Role-specific text interpretation; Progress: Explanatory training prose preserves verified targets |
| D08 | Metadata: One classpath metadata source; Header-safe versioned product identity |
| D10 / R03 / R08 | Run: One enabled run owns its resources; Original authority crosses every continuation; Deterministic faults and measured work |
| D11 / R02 / R04 | Storage: Reversible manual intent during recovery; Run: Original authority crosses every continuation |
| R10 | Metadata: One classpath metadata source; Release: Automated verification before user acceptance; Coordinated semantic versioning |
| U1 | Progress: Explanatory training prose preserves verified targets |
| U2 | UI: Selection-safe and accessible disclosure |
| U3 | UI: Advice-first details with complete source access |
| U4 / U5 | UI: Readable native guide presentation; Advice-first details with complete source access |
| U6 | Navigation: Reset and projected skill icons; UI: Selection-safe and accessible disclosure |
| U7 / U8 / U9 | UI: Distinct header actions and compact provenance |
| All decisions | Release: Explicit refactor decision coverage and predecessor integration |

| Decision | Concrete scope | Tasks |
| --- | --- | --- |
| D01 layers | Prepared domain, one-way adapters/coordinators; retain Design A | 1.1–1.4, 4.1–4.6, 7.1–7.5 |
| D02 enums | Closed completion/category/action policies, exhaustive mappings | 4.2–4.4, 5.1, 8.4 |
| D03 classifier | Source evidence, recognizers/reconciliation, mutation tests | 2.1–2.4, 4.1–4.6 |
| D04 immutable rows | Assembler supplies semantic fields/manual/stage | 4.3–4.5 |
| D05 identities | Focused catalogs, typed keys, legacy codecs | 4.2–4.6, 5.1–5.3 |
| D06 detectors | Explicit family implementations and exact predicates | 5.1–5.4 |
| D07 interpreters | Shared catalog/tokenizer, separate semantics/projection/annotation | 2.2–2.4, 4.1–4.6 |
| D08 metadata | One worker-loaded resource, dev/JAR/header parity | 6.1–6.3 |
| D09 literals | Cohesive protocol/domain/storage/timing/UI owners | 6.3–6.4, 8.4 |
| D10 cohesion | Thin plugin/run/coordinators; reuse composed UI | 7.1–7.5, 8.4 |
| D11 compatibility/failure | Manual negatives, paired views, fences, migration/tests | 3.1–3.4, 4.5–4.6, 7.1–7.5, 9.1–9.5 |
| R01/R06/R09 | Provenance/reasons, conservative predicates, stable identity and guide order | 2.2–2.4, 4.2–4.6, 5.1–5.4 |
| R02 | Explicit pending true/false edit recovery | 3.1–3.3 |
| R03/R04 | Original run/session resources and coherent publication | 3.4, 7.1–7.5 |
| R05 | Bounded evidence validation, legacy versions and last-good content | 4.1, 4.5–4.6 |
| R07 | Pure projection, narrow UI contracts, no unsolicited navigation | 7.3, 8.1–8.4 |
| R08 | Deterministic failure/clock seams, measured work only | 1.3, 3.1–3.4, 7.4–7.5, 9.1 |
| R10 | Classpath packaging, API/rules/version/docs gates | 6.1–6.4, 9.1–9.5 |
| U1 | Four training regressions, including fresh/legacy and negative counterexamples | 2.1–2.4, 9.4 |
| U2/U6 | Immediate clicks, explicit selection, read-only projection regions | 8.1–8.2, 9.3–9.4 |
| U3/U4/U5 | Folded related subsection and shared heading/value style | 8.3–8.4, 9.3–9.4 |
| U7/U8/U9 | Icon-only resync, inert shell and named vertical gaps | 8.5–8.6, 9.3–9.4 |

## Risks / Trade-offs

- Broader refactor hides behavioral changes → small ordered commits, characterize first, fix regressions/recovery before moving responsibilities, review compatibility/semantic differences explicitly.
- Over-permissive training repair creates false completion → recognized explanation spans only, structured hints reconciled with action semantics, extra required-action/unknown-skill/conflict tests.
- Wiki metadata can drift → validated hints plus bounded legacy fallback, live public contract check during implementation, preserve originals and typed unknowns; no stability claim.
- Instant clicks conflict with native word selection → user chose explicit selection mode; remove delayed default path rather than promising both incompatible defaults.
- More types/files become ceremony → every extracted owner must own a resource, invariant, behavior or cohesive UI unit; combine forwarding-only candidates; no hard line gate.
- Identity/readback changes reset checks → wire codec/fixture compatibility before assembly change, exact manual tokens, unknown keys inert/retained and explicit migration for intentional changes.
- Additional header spacing reduces short viewport → named small initial gaps, minimum readable typography and isolated RuneLiteLAF previews at short/narrow sizes/scaling.
- Prior change remains active → preserve its still-valid requirements and acceptance history, explicitly supersede only changed interactions; no accidental archive-order reversion.

## Migration Plan

1. Pin current state/tests and compare against master for the four rows. Resolve public API and version decision; prepare regression/fault seams.
2. Correct training explanations and manual-recovery negatives; retain paired publication and original authority before broad moves.
3. Introduce typed source/identity/assembly contracts and role interpreters with legacy adapters; migrate consumers incrementally without rewriting persisted tokens.
4. Extract detector families, metadata and real run/coordinator owners; remove obsolete cascades/adapters only after parity/boundary checks.
5. Apply the nine UI findings and selection decision through cohesive components; preserve search/filter/footer algorithms.
6. Verify normal JAR/development metadata, Java 11, public source contract, native layouts and exact candidate; offer user launch/checklist. After explicit acceptance audit current official rules. Keep local and unmerged throughout.

Rollback is to the last locally reviewed commit; lossless legacy fields and retained originals permit rollback without cache deletion. Do not roll back a genuinely migrated data format without its specified tested reverse/read compatibility path. Neither rollback nor an archive authorizes merging or publishing.

## Open Questions

No product-choice blocker remains: instant clicks plus explicit selection was selected by the user. Exact public API consumers, current Wiki markup and available native platform environments must be checked during implementation; a discovered incompatible contract requires a revised migration/version decision rather than an implicit exception. Spacing/14-point heading values are concrete initial choices for native/user review, not claims that the screenshot measured those dimensions.

## User follow-up, 16 October 2026

The user requested three additional corrections/features before choosing a new
text-selection interaction. Candidate 3.3.0 retains the layered refactor and
replaces training status/comparison text with fixed-size progress circles:
current real level in unfinished circles, **percentage only on hover**, green
ring/checkmark when complete. Compound/unknown/target-only semantics retain the
same domain evaluation; unsupported meaning cannot gain a completion checkmark.

Disclosure now restores the clicked card's pixel anchor after settling changed
row geometry and viewport range, respecting newer user navigation and necessary
end clamping. Successful browser opens are silent; Shift + right-click copies a
validated configured link without HTTP/browser work and shows local popup
feedback for 1.6 seconds. Original run ownership and popup/timer cleanup apply.

Existing Select text behavior remains until the user chooses an alternative.
The options in `.research/questcape-text-selection-options.md` are suggestions,
not implementation approval. Combined in-game acceptance and the subsequent
official-rules audit remain pending for the corrected candidate; no merge/push.
