## Context

The baseline is `master` / `40eb54a520970db9edb77720d254d5b0f1b2f207`, version `3.0.1`. `GuidePanel` currently mixes header/status, search, filters, card construction, details, completion totals and viewport intent in roughly 1,595 lines. `GuideRow.targets` retains upper levels only. `Completion.of` consequently reports every known below-target training row as incomplete. The parser flattens source fields and links, losing which cell established a link's role. Header rendering also infers operation state from English status substrings.

The authoritative product input is the user-approved revision 3 in [the UI/UX review](../../../.research/questcape-ui-ux-review.md) and [its companion contract](../../../.research/mockups/screenshot-edits/05-combined-user-review-v3.md). These links resolve in this workspace; `.research` remains ignored. This design and its specs contain the complete implementable contract so a checkout without those private assets remains useful. Full-client screenshots contain private session context and will not be copied into tracked artifacts.

Input fingerprints (SHA-256, 14 October 2026):

- Review: `1760f0b50e8c7af7e6af640d21cb6786d70381f35363f703f3a0ff4fa497ef9f`.
- Approved normal full-client PNG: `d6eb2a6c41b9085cf4179115f39079938f80bb4fa919a0ae96740a4fe7cbafa3`.

Four actual screenshots establish completed training, a completed quest, an incomplete quest confirmed not started by the user, and an in-progress miniquest. They establish appearance, not tested interactions. Recovery/unknown recommendations are source-derived. The mockup's `lunarr` query illustrates zero matches; its enabled Continue styling does not override existing enablement rules.

The main presentation spec contains historical Quest Helper Open/feedback scenarios that do not describe this checkout. Its delta explicitly replaces those scenarios with current source-link actions. The separate `quest-helper-handoff` spec remains historical/deferred and is not pulled into this change's tasks or acceptance claim.

## Goals / Non-Goals

**Goals:** implement the approved layout, preserve all route content and deliberate navigation, add trustworthy training range states, and make touched responsibilities readable within the existing guide/progress/integration/persistence/UI layers. Preserve the community lessons about conservative observations, local assets, explicit ownership, immutable publication and bounded failure recovery.

**Non-Goals:** whole-classifier replacement, activity detector hierarchy, global identity/key migration, HTTP metadata/User-Agent work, general manual-recovery repair, complete plugin lifecycle redesign, new search mechanics, stage/XP/time estimates, route optimization, new font/settings/toolkit, new Quest Helper handoff, telemetry, and publication. Existing handwritten rules outside the training/field boundaries remain a separate refactor concern. No class-length CI gate or test-class splitting is introduced.

## Decisions

### 1. Keep the layers and compose the sidebar by responsibility

| Owner | Responsibility and input | Excluded responsibility |
| --- | --- | --- |
| Guide parser/classifier | Sanitized source evidence, field/link roles, typed training targets and title tokens | Current account facts or Swing widgets |
| Completion/training evaluator | Pure per-target and aggregate states from one immutable account observation | Reading Client, formatting labels, colors or browser actions |
| Presentation projectors | Detail blocks, training comparisons, completion/count/filter/Continue candidates | Disk/network IO or classifying rendered text |
| GuidePanel composition | Assemble header, search/filter, route/card/details and navigation components; EDT-local intent | Source interpretation or a second completion evaluator |
| Header/search/card/navigation components | Render supplied values and dispatch explicit narrow actions | Services, Client, storage keys or parent panel internals |
| Plugin/action adapter | ClientThread capture, worker/service handoffs, original run/session authority, LinkBrowser actions | Large widget layout or domain truth |

Extract coherent units such as `GuideHeader`, `GuideSearchBar`, `GuideRowCard`/`GuideRowDetails`, and `GuideNavigationBar`. Keep viewport anchoring and search intent together in the route owner; extract a controller only if it owns that state rather than forwarding calls. Introduce a pure `GuideProjection` for completion/count/filtered unfinished candidates, a `GuideDetailProjector` and a shared training evaluation value. Names are indicative; cohesion is required, a fixed file inventory is not. Reuse current `StepSearch`, filter predicates and navigation algorithms rather than replacing them.

Cards receive row presentation, prepared local assets and a small action contract. They do not receive the entire panel or injected services. UI state (filters, query, match index, expansion, anchor, navigation sequence) remains session-local on the EDT. Meaningful copy, URLs, colors, spacing and timing live in cohesive owners such as `UiCopy`, `UiTheme`, `UiMetrics` and existing timing owners. Prefer enums for finite reusable field/action/skill/state policies; use a switch when it is clearer than a new strategy hierarchy. Domain enums do not own Swing colors.

Alternative: cosmetic edits inside the current monolith are smaller initially but leave duplicated training truth and source-header checks. A complete controller/service hierarchy would exceed this feature's needs. Focused composition offers the smallest lasting boundary improvement. Approximately 300 production lines is a review prompt; a cohesive slightly larger class and long tests are acceptable.

### 2. Preserve source meaning once at the guide boundary

Add an immutable optional versioned row-evidence value, containing only recognized semantic field roles, safe link roles, and training/name tokens needed here. `GuideFieldRole` covers quick guide, advice, guide projected levels, quest QP, guide running QP, location and title, with unknown/raw fallback. Recognized header aliases belong at this boundary, not in Swing. Fresh HTML uses each cell's identity and associated validated anchors; conflicting role evidence stays unresolved.

`TrainingTarget` identifies a shared canonical skill (or explicitly derived combat target), required upper level and optional supported lower level. A separate token sequence/span list annotates recognized skill names in the original training instruction, preserving all intervening wording and parenthetical recommendations. Do not infer target requirements from the projections/advice. Reuse one skill-name/alias catalog with existing `SkillText` and `ProjectedLevels`; do not add another UI regex for training. A narrow, bounded source instruction grammar is legitimate fallback evidence, not a guarantee that all future prose can be understood. Unknown/contradictory instructions remain visible without invented levels.

Keep classification of hand-ins, reward claims, quest and miniquest stages ahead of training range inference. A level mentioned in a non-training action proves neither a reward nor a hand-in. This change does not expand automatic activity detector coverage or weaken its unknown behavior.

For a legacy cached row, `GuideParser.reclassify` adapts retained fields/title/safe URLs on the worker. An explicit unambiguous `from level X to level Y` instruction can establish X; `train ... to Y` cannot. Parenthetical advice is ignored for required targets. A legacy quick-guide action requires an unambiguous retained safe quick-guide destination/role; literal `QG` alone and arbitrary URL guessing are insufficient. Ambiguous roles become related/raw source content, not a fabricated primary action. No network request is necessary to browse/reclassify a valid legacy cache.

Alternative: parsing card titles or English field headers inside the view would repeat fragile interpretation and mix source facts with display copy. Capturing the small amount of missing evidence at its origin fixes that boundary without rebuilding the entire guide classifier.

### 3. One training evaluation supplies status and comparisons

Use real unboosted observations from the existing immutable `AccountProgress`. Resolve combat through the existing `Experience.getCombatLevel` policy and required component levels, not a fictional `Skill.COMBAT` icon or Client read in Swing. Validate skill-specific bounds against the existing supported API/source policy; combat's 126 ceiling must not become the allowed bound for every ordinary skill. Reject contradictory target evidence; do not silently choose a last duplicate target.

For each supported pure-training target:

| Evidence | Per-target completion | Percentage |
| --- | --- | --- |
| Valid X→Y, Y>X, known L<X | INCOMPLETE | 0% |
| Valid X→Y, X≤L<Y | IN_PROGRESS | Floored interval percentage |
| Valid X→Y, L≥Y | COMPLETE | 100% |
| Supported Y only, known L<Y | INCOMPLETE | Omitted |
| Supported Y only, L≥Y | COMPLETE | Omitted |
| Missing/invalid observation or unsupported upper target | UNKNOWN | Omitted |
| Invalid/absent X, independently valid Y | Evaluate Y-only | Omitted |

For valid intervals calculate `floor(100 * clamp((L-X)/(Y-X), 0, 1))` using non-integer division. Display `Current level: {L} - {P}%`. At X the row is in progress and displays 0%; 54 in 52→55 displays 66%; 65 in 60→65 displays 100%. This is level-interval position, not XP/time progress. Unknown level displays `Current level: unavailable`; known level without a valid interval displays `Current level: {L}`. X=Y or X>Y never divides by zero and never creates a supported interval. The upper target can still be used if independent source evidence establishes it.

For multiple required targets: COMPLETE only when all required targets are recognized, observed and complete; IN_PROGRESS when any supported interval is in progress, or at least one required target is complete and another known target is unfinished; otherwise INCOMPLETE when a known target is below its required level; UNKNOWN when none of those outcomes is established. Missing targets never allow COMPLETE. Known positive/negative facts remain useful alongside a separate partial-observation indication; for example one complete and one missing target is UNKNOWN, one in-range and one missing target is IN_PROGRESS with an unavailable comparison, and one below-base and one missing target is INCOMPLETE with that indication. An unresolved extra requirement prevents all-known-complete from becoming COMPLETE.

Show a labeled comparison for each skill, in source order. A single target needs no redundant skill label because its icon/title supplies it; compound comparisons include the skill name. Never average percentages or choose an arbitrary first target as the whole row. Use the same aggregate result for card style, state filter, global summary and Continue. Retain exact existing `○ Incomplete`, `✓ Complete`, `◐ In progress`, `? Unknown`, manual suffix and Information vocabulary. This includes keeping incomplete quest wording as approved.

Alternative: leaving compound training always incomplete would discard observable partial achievement, while assuming missing requirements are done creates false completion. The selected policy preserves determinate facts and explicitly exposes partial evidence.

### 4. Details are projected, complete and advice-first

The projector produces primary validated quick-guide action, advice, guide projections, compact QP, location and related links. Source title/status remain in the summary; the primary quick-guide action leads expanded details immediately below Details, followed by advice, as in the approved revision 3 screenshot. Use title-case headings and the existing 14-body/13-metadata scale. Projections say `Guide projected levels`, retain level-then-icon order, and remain separate from current levels and prerequisites. QP uses `Quest points: 2` / `Guide QP total: 279`; this is route metadata, not observed account QP.

`Quest page ↗` belongs in `Related wiki pages`; it is not an alternative primary button. Primary is `Wiki quick guide ↗` only for its validated source destination. N/A produces no dead primary action; its original field remains inspectable. Location retains its own source action. All remaining safe destinations, including distinct fragments, remain accessible under related links. Deduplicate only equivalent validated destinations without losing their labels/source context. Preserve advice verbatim, source order and original raw fields through a compact `Source fields` fallback when needed. A changed/unrecognized field does not disappear. Table structures outside existing validated parsing support still reject the whole candidate and retain last-good content; this proposal does not promise arbitrary table-schema acceptance.

Training actions visibly distinguish `Theoatrix guide ↗` from `Wiki: Mining ↗`; compound Theoatrix actions include their skill, e.g. `Theoatrix guide: Attack ↗`. The existing resolver, host/redirect constraints, directory fallback, cancellation and LinkBrowser integration remain authoritative. Link grouping must not introduce background verification or broaden safe Wiki links.

Alternative: new nested accordions or summarized advice would save height at the cost of hiding meaningful route decisions. Reordering and compact metadata improve scanning while keeping long useful notes readable.

### 5. Header actions and operation state are explicit

Normal layout: packaged plugin icon, about 10 logical units of gap, QuestCape title, quiet GitHub button; below, OSRS Wiki/source row with separate wiki-refresh control; then `Resync Progress`, global completion, search and unchanged filter icons. Keep title 18 bold, card titles 16 bold, advice/status 14 and metadata at least 13. On-demand provenance remains independently keyboard-accessible through an existing footer/provenance disclosure or source-info action; add a text disclosure only if none exists. It includes established account/mode, observation time, guide revision/retrieval/validation and license attribution, without a permanent account-name/Live/Standard/sync-success block.

GitHub opens exactly `https://github.com/EyalMK/QuestCape/issues` through a fixed application action and LinkBrowser. It does not pass through the parsed-Wiki allowlist or extend that allowlist to GitHub. Exact tooltip: `Report issues or contribute`. Use a below-button floating tooltip, content-sized from font metrics, smaller than body copy (11 logical units initial token), no reserved row or header-wide box. Target initial borders: 1-unit `#2E3946` for GitHub/resync/clear, `#29333F` for tooltip. Normal outlines are subtle; keyboard focus must still be obvious. Padding, scaling and tooltip bounds require native verification; the mockup's 154×21 tooltip is not a production fixed size.

Wiki refresh uses the earlier approved circular-arrow silhouette; GitHub uses the earlier approved GitHub silhouette. Obtain clean, permissively licensed packaged UI art matching those references, record provenance/notices, and optimize actual PNG dimensions. Do not ship screenshot-cropped reference artwork without verified rights. Real skill/category/filter assets come only from existing RuneLite local managers/resources, never regenerated substitutes, remote packs or captured screenshot strips. Resync uses opposing horizontal arrows, distinctly different from wiki refresh and never a download-tray icon. Tooltip `Resync progress from RuneLite`; existing readiness and forced local observation semantics apply.

Replace view-side status string matching with typed operation and persistence values emitted where outcomes occur. Capture each service's content/facts and save outcome in a single immutable view before EDT publication; do not combine a new record with an old independent status read. If publication precedes save, mark its pending state explicitly and later publish the result for the same revision/session. Guide and account freshness remain independently meaningful. Capture original run/session authority at request creation and carry it through worker, completion and EDT delivery; re-render/message helpers cannot adopt a restarted plugin's new token. This is a bounded repair of touched publication paths, not the full `PluginRun` refactor.

Show concise conditional unavailable/refresh-failure/unsaved messages outside the route scroller, with appropriate existing Retry/Resync/Refresh action. Hide routine successes rather than announcing stored state on every event. Unknown automatic action meaning is explained on the card and cannot be repaired by Resync; missing observations can be retried. Use only reasons supported by evidence, not invented stage details. Keep valid in-memory content and last-good route usable; debug diagnostics do not disclose account identity.

Alternative: removing the name block while retaining status-string interpretation would make failure provenance less trustworthy. Typed immutable values provide truthful compact UI with a small, justified service boundary change. A global reducer/event framework is unnecessary.

### 6. Search, disclosure and footer change presentation while retaining intent

Keep the match counter/downward chevron borderless at the search field's trailing edge; place a separately outlined X beside it. Its accessible name/tooltip is `Clear search`; a blank query hides/disables it without a disruptive width jump. For nonempty zero-match queries show disabled `0/0 ▾`, enabled X and `No matching step` below search. With a selected match show `Search match: step N` there. Keep `x of y steps shown` separate and global completion independent. Clear only the query, restore pre-search anchor, cancel deferred search/wrap, preserve filters. Reset-all stays distinct. Retain approximately 300 ms debounce, ascending traversal, relevance initial jump, 500 ms two-Enter wrap and latest-user-intent fencing.

The full non-control card body remains disclosure surface, including expanded advice. Left single clicks toggle once; text clicks wait for the native multi-click interval so word selection does not collapse the card. Background and keyboard disclosure remain immediate. Drag/multi-click selection and nested links/buttons/checks do not toggle, and all owned timers stop on row disposal. Preserve text-selection cursor inside selectable advice rather than applying a hand cursor indiscriminately. Enter/Space toggles the focused card, not a focused nested action. Update accessible expanded/collapsed description/state; preserve stable expansion keys and viewport anchor on reflow. Live updates never auto-collapse or move focus/scroll.

Footer has one circular up/down arrow-icon button centered above Continue. Use the existing position-aware destination policy, with active `Top`/`Bottom` tooltip/accessibility name, not arrow Unicode as the visible icon. Continue retains its name and existing resume/play icon, occupies usable footer width with about 10-unit side margins and 34-unit starting height, and targets the earliest visible unfinished row. Preserve clamped-alignment enablement, account/route readiness, unknown eligibility and Ctrl+Home/End/Ctrl+J. The boundary button changes direction with position; do not add a second simultaneous boundary action. No timer-driven or live-fact-driven navigation.

Alternative: changing traversal or adding new footer destinations would conflate a layout improvement with navigation redesign. Existing tested semantics fit the requested layout.

### 7. Keep additive cache compatibility and local release scope

Retain schema 1, existing stored title/fields/links/upper-target maps, account cache shape, stable key recipes and manual eligibility. New evidence is optional and carries its own version; legacy absence uses the offline adapter. Regenerate derived targets/tokens instead of trusting serialized completion or presentation. For an unknown evidence version, ignore that enrichment and use legacy retained evidence, preserving the file; contradictory enrichment cannot override validated raw source. Old binaries must continue to read new snapshots' legacy fields, verified with a frozen legacy fixture/reader seam. If that cannot be established, redesign the enrichment before implementation; do not silently invalidate data or bump schema behind this plan.

Fresh and legacy-cached rows with equivalent retained evidence produce equivalent classifications/keys. Preserve safe balloon-title repair, duplicate/start/finish handling and all Kudos checks. A refreshed guide with changed training semantics is reevaluated; observed levels are never rewritten. Cache browsing works offline. No forced cache deletion, schema-acceptance relaxation, overwrite of corrupt/unknown originals or user-setting reset.

Future implementation targets `3.1.0`: compatible features, optional cache enrichment, no broken public interface or setting semantics. Update current metadata, packaging verification and current release docs once implementation occurs, leaving historic `3.0.1` evidence intact. Creation/validation of this proposal leaves version files unchanged. The new branch is local and unpushed; old source/Hub delivery authorizations are historical, not authorization for this change.

## Risks / Trade-offs

The research-to-implementation mapping is explicit:

| Review input | Chosen design boundary | Verification tasks |
| --- | --- | --- |
| UX01 advice-first details; D03/D07/D10; R05/R07 | Semantic source roles and pure detail projection, complete raw fallback | 2.3, 3.5–3.6, 7.1 |
| UX02 training; R01/R07/R09 | Typed intervals and one real-level evaluation for status/comparison | 2.2, 3.1–3.4, 7.2 |
| UX03 whole-card interaction | Explicit disclosure/selection/nested-action ownership | 1.3, 7.3, 7.5 |
| UX04 compact header; R01/R04/R06 | Independent fixed actions, typed paired status and original publication authority | 4.1–4.4, 6.1–6.4 |
| UX05/UX06 search/navigation; R07/R08 | Retain current algorithms and EDT intent; change control composition | 6.5–6.6, 7.4–7.5 |
| UX07/UX08 typography/icons; R08/R10 | Existing native type scale and genuine local assets with prepared metrics | 5.3–5.5, 8.1 |
| UX09 recovery; R01–R06 | Conditional truthful limitations; broader recovery repairs separately scoped | 4.4, 6.4, 9.3 |

These references carry the established research lessons into concrete seams and checks. They do not make this UI candidate the general refactor proposal.

- Source prose/cell roles can change → bounded interpretation, conflict tests and raw fallback; no inferred URL or false completion.
- Optional cache enrichment can drift from legacy fields → recompute derived values, cross-version/readability tests and key-equivalence assertions; retain originals on invalid input.
- Quiet borders can weaken focus visibility → separate explicit focus treatment, reachable controls, tooltips and native keyboard review.
- Inline icons and reordered details can alter wrapped height → actual local assets with stable placeholder dimensions, RuneLiteLAF text metrics, narrow/DPI previews and anchor regression tests.
- A floating tooltip can be clipped or overlap the source line → native popup placement below the button with bounded screen positioning, no permanent layout slot; temporary overlap is acceptable.
- Service publication work can expand scope → repair only paired content/status and original authority needed by this header; no broad service move. Research R02–R05 repairs are prerequisites before any later broad class moves and remain separately tracked; do not claim general manual-recovery correctness from this UI work.
- Partial compound training may read as active gameplay → explain interval position, show per-skill unavailable facts and never estimate XP/time or current activity.
- Synthetic native previews are insufficient gameplay evidence → user-only acceptance on the exact implemented revision; no agent game input.

## Migration Plan

1. Characterize existing source/cache/URL, search/filter, disclosure and navigation behavior before moving code. Pin the revision 3 contract; distinguish real captures from compositions.
2. Add typed evidence/targets and the offline legacy adapter with additive compatibility/key checks. Implement the shared training evaluation and pure projectors.
3. Establish typed paired service views and original run/account publication on touched paths, with deterministic failure/interleaving tests before UI component extraction.
4. Extract cohesive components incrementally; adopt approved header, search, details, icons, interaction and footer. Compare native previews against revision 3 using the real local assets.
5. Align `3.1.0` metadata/docs and run relevant automated/package/API checks. Offer the development client and exact manual checklist; await user acceptance before claiming runtime completion or archiving.
6. If acceptance uncovers a failure, repair it and repeat affected checks on that revision. Rollback to `3.0.1` can read retained legacy fields; no account/config migration requires reversal. Publication/PR/merge/Hub work requires a later explicit request and visibility check.

## Open Questions

No product-policy question blocks implementation. Native tooltip sizing, actual sidebar/DPI metrics and clean licensing of the selected GitHub/wiki artwork are implementation verification items with explicit tasks. If a preferred silhouette cannot be packaged permissively, present the closest verified asset for user review before substituting it. This is not approval to invent RuneScape icons or use an older discarded layout.
