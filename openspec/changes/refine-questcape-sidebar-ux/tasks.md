## 1. Pin the approved contract and characterize touched behavior

- [x] 1.1 Read this change's proposal/design/deltas, the current AGENTS instructions and revision 3 review/companion; verify input fingerprints and record the implementation starting revision. Treat the private mockups as layout references and actual captures as visual evidence only.
- [x] 1.2 Run the existing relevant parser/progress/navigation/integration/persistence/lifecycle/packaging suite under Java 11 and record the baseline; do not treat historical test counts as a new pass.
- [x] 1.3 Add focused behavioral characterization for filter scope, ordered/debounced search and wrap, query-origin restoration, clamped Continue alignment, stable expansion and whole-card/nested-control actions before UI moves. Reuse existing tests and avoid implementation-mirroring snapshots.
- [x] 1.4 Record the two stale presentation handoff scenarios corrected by this delta and the separately deferred handoff/general refactor/manual-recovery scope. Do not implement those features or broadly move services as part of this change.

## 2. Preserve typed guide evidence and legacy compatibility

- [x] 2.1 Add cohesive canonical skill/field/link-role types and optional versioned row evidence. Preserve raw title/fields/links, upper-target representation, existing stable keys and schema-1 account/config semantics.
- [x] 2.2 Extract supported training upper/lower targets and original skill-name tokens at the guide boundary, with source-cell/structured evidence preference and bounded instruction fallback. Keep parenthetical recommendations, projections and non-training hand-ins out of target requirements.
- [x] 2.3 Capture validated quick-guide, title/quest-page and location link associations before flattening fresh source cells. Keep ambiguous/unknown fields and links as raw/related fallback instead of guessing destinations.
- [x] 2.4 Extend worker-side cache reclassification with an offline legacy adapter and unsupported-enrichment fallback; retain safe balloon repair, duplicates/start/finish identities and all eligible Kudos checks.
- [x] 2.5 Test explicit ranges, target-only rows, compound/unknown skills, contradictory/degenerate/out-of-range evidence, header aliases, parenthetical advice and reward hand-ins using fixture mutations. Verify conservative unknown outcomes and full content retention.
- [x] 2.6 Test legacy-to-new equivalence, serialization/reclassification round trips, schema-1 legacy-reader compatibility, key/manual-record retention and offline browsing. Preserve corrupt/unsupported originals and bounded whole-candidate validation; stop and revise the design if additive compatibility cannot be established.

## 3. Evaluate training once and project meaningful details

- [x] 3.1 Implement one pure per-target/aggregate training evaluation from the immutable account observation, sharing real-level and existing derived-combat resolution with completion. Preserve exact state labels and automatic/manual eligibility outside training.
- [x] 3.2 Implement the design's multi-target precedence and partial-observation indicators; require every target for Complete and emit per-skill comparisons without averaging. Feed the same aggregate into styling, state filters, totals and Continue projection.
- [x] 3.3 Implement floored/clamped interval percentages and current-level copy; omit percentages for missing bases/levels, invalid intervals and target-only rows. Keep known upper-goal completion separately evaluable and boosts/projections inert.
- [x] 3.4 Test below-X/at-X/interior/at-Y/above-Y, 54 in 52→55 = 66%, 65 in 60→65 = 100%, X≥Y, missing observations, combat inputs, unresolved requirements and all compound precedence cases in the delta spec.
- [x] 3.5 Add a pure detail projector for primary quick guide, full advice, guide projections, compact QP, location, related Quest page and unknown/source-field fallback. Preserve safe URL fragments, original advice and destination/context completeness.
- [x] 3.6 Test Tourist Trap/Lunar Diplomacy/miniquest-style detail fixtures, N/A/ambiguous quick guides, unfamiliar fields, distinct anchors and completed-row content retention. Use source-derived metadata rather than account facts from mockup pixels.

## 4. Establish typed, current publication before component moves

- [x] 4.1 Replace touched English-message-derived state decisions with typed operation and persistence outcomes at the service/action boundary, keeping user-facing copy solely in presentation.
- [x] 4.2 Expose paired immutable guide and account views so content/facts and save outcome publish together. Represent pending/unsaved outcomes explicitly and retain independent guide/account freshness and readiness.
- [x] 4.3 Carry the original plugin run and account/session authority through all touched worker, completion, message/render and EDT paths. Reject late results instead of recapturing a restarted run's token; retain prompt shutdown/cancellation and no client/EDT IO.
- [x] 4.4 Use deterministic queued/fault seams to test old-run startup callbacks, account switches, delayed save results, route failure with last-good content and unsaved live observations. Verify correct content/status pairing and no focus/viewport resurrection before UI extraction.

## 5. Compose the sidebar and prepare genuine local assets

- [x] 5.1 Extract cohesive header, search/filter, card/details and navigation units with narrow action contracts; retain EDT-local query/filter/expansion/anchor intent and reuse existing search/navigation algorithms. Keep GuidePanel as composition rather than passing it into cards.
- [x] 5.2 Introduce cohesive copy/theme/metrics owners for meaningful literals, using enums for reusable finite policies. Review responsibility/readability rather than imposing a 300-line hard limit; do not split tests or the current parser for size alone.
- [x] 5.3 Reuse one skill interpretation/catalog for instruction tokens, Additional info and projected levels while preserving each format's own grammar and unknown fallback. Render actual training skill icons before names, keeping category icons and unchanged filter artwork.
- [x] 5.4 Prepare local RuneLite skill/category assets on the worker and preserve stable placeholders, accessibility names and EDT generation checks. Do not ship screenshot status strips or redraw game icons.
- [x] 5.5 Obtain clean permissively licensed GitHub/wiki-refresh UI artwork matching the approved earlier silhouettes; add distinct opposing-arrow resync and circular-boundary arrow icons. Record provenance/notices and verify optimized real PNGs/classpath loading without remote resources; seek review if a material artwork substitution is necessary.

## 6. Apply the approved header and search layout

- [x] 6.1 Build the logo/title row with added gap and very quietly outlined GitHub button; move the separate wiki-refresh action to the attribution/source row, preserving refresh coalescing/readiness and source/license access.
- [x] 6.2 Add the fixed GitHub issues action through LinkBrowser without broadening parsed-Wiki URL validation. Implement the exact small below-button floating tooltip with faint border, content-sized native metrics and no reserved header row; preserve visible keyboard focus.
- [x] 6.3 Replace the routine identity/Live/Standard/success block with restrained `Resync Progress` and opposing arrows. Keep account/mode and independent freshness available through a keyboard-accessible provenance disclosure, with existing forced local-observation/readiness behavior.
- [x] 6.4 Add conditional unavailable/refresh-failure/unsaved feedback outside the route scroller, with recovery matched to typed outcomes. Preserve last-good/in-memory browsing and avoid routine success banners or unsupported-meaning resync promises.
- [x] 6.5 Add the lightly outlined clear X beside the borderless match counter/downward chevron. Put search feedback immediately below the field, separate from shown-step counts; clearing only search retains filters and cancels pending jumps/wrap while restoring origin.
- [x] 6.6 Test exact action URLs/copy, zero-match versus no-visible-row states, blank-query layout, clear versus reset, independence of global totals, preserved filter artwork/menus and disabled local resync when account-unready. Verify no new requests or game inputs.

## 7. Apply details, independent disclosure and footer

- [x] 7.1 Render the advice-first detail projection with full text, restrained title-case headings, guide-only projection/QP labels, primary Wiki quick guide, location action and Quest page under Related wiki pages. Keep N/A/unknown raw fallback and all source destinations accessible.
- [x] 7.2 Render actual skill-token training titles and current-level comparisons from the shared evaluation; use provider-specific visible labels for Wiki/Theoatrix and per-skill compound actions. Preserve resolver host/redirect validation, fallback, cancellation and user-only activation.
- [x] 7.3 Retain whole-body click disclosure and Enter/Space on the card, implement selection-drag/multi-click suppression, exclude nested independent controls and expose accessible expansion state. Preserve selection cursor, expansion keys and scroll anchor through reflow/live changes.
- [x] 7.4 Replace visible Top/Bottom text with one centered circular up/down icon above reasonably padded full-width Continue. Preserve position policy, destination tooltips/accessibility, shortcuts, earliest filtered unfinished candidate and clamped-alignment/readiness enablement.
- [x] 7.5 Test body/title/advice clicks, text-selection gestures, nested action isolation, focused-control key activation, footer direction/enablement, unknown/in-progress filter eligibility and live updates without disclosure/focus/viewport jumps. These synthetic tests must not interact with RuneScape.

## 8. Verify native layout, packaging and implementation release

- [x] 8.1 Produce and inspect isolated RuneLiteLAF Swing previews at narrow/default/expanded widths, short/tall heights and representative scaling. Include long titles/advice, compound targets, many links, unavailable icons and multiline notices; verify complete wrapping, focus order, hit areas, quiet borders and tooltip bounds against revision 3.
- [x] 8.2 Run the relevant full automated suite and ordinary-JAR resource/packaging checks after functional changes. Verify Java 11, resolved API/dependency compatibility, no reflection/process/game input, worker/ClientThread/EDT ownership and lifecycle cancellation. Record Linux limitations if that runtime is unavailable.
- [x] 8.3 Update build/plugin/packaged version, version assertions and current release documentation coherently to `3.1.0`, explaining the compatible minor increment and retaining historic evidence. Revise the migration/version decision first if an incompatibility is discovered.
- [x] 8.4 Record exact tested revision, actual automated/native-preview evidence and outstanding manual acceptance. Keep publication unrequested: no push, PR, merge, visibility change or Hub update.

## 9. Obtain user acceptance and final verification

- [x] 9.1 Offer `./gradlew run` from the plugin root and provide [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts). Only the user may interact with RuneScape; a successful launch is not acceptance.
- [x] 9.2 Ask the user to exercise header GitHub/tooltip/wiki refresh/resync/provenance; clear alongside match navigation with unchanged filters; completed Tourist Trap, incomplete Lunar Diplomacy and available in-progress activity; advice/link retention; whole-card disclosure/selection/nested actions; native icons/fonts and narrow layout.
- [x] 9.3 Ask the user to exercise observed training below/base/interior/goal states where available, compound/missing comparisons, footer Top/Bottom/Continue alignment and live/account transitions without unsolicited movement. Use deterministic synthetic evidence for unavailable irreversible states and controlled failures, explicitly distinguishing it from game confirmation.
- [ ] 9.4 Record explicit user confirmation for the exact candidate revision, or leave acceptance pending with concrete remaining cases. Repeat affected checks after any functional correction; keep Ubuntu diagnosis and unrelated refactor limitations separate.
- [ ] 9.5 After acceptance, re-read then-current official RuneLite Hub/Jagex rules and audit the exact shipped production/build/resource/config/privacy/threading/lifecycle candidate as required by the release spec. Resolve violations and repeat affected acceptance before archive/delivery; do not claim maintainer approval or act on historical PR/Hub authorizations.
