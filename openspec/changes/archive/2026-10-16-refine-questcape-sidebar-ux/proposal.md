## Why

The live QuestCape sidebar is a useful baseline, but competing header actions, combined search/filter feedback, and metadata-first details obscure the route advice. The user approved revision 3 of the screenshot-based UI/UX review; implementing that design also requires trustworthy training intervals so current levels, percentages, and completion states agree.

## What Changes

- Adopt the approved revision 3 layout from `.research/questcape-ui-ux-review.md` and its `05-combined-user-review-v3` companion. Preserve native Swing, dark styling, current font family/title-body sizes, source order, filter artwork, and existing completion text/symbols.
- Add the packaged plugin icon beside QuestCape, a quietly outlined GitHub action with a small floating tooltip, separate wiki refresh, and a distinct `Resync Progress` action. Move routine account/mode details to on-demand provenance; keep exceptional unavailable/unsaved states visible and account scopes isolated.
- Keep the borderless match counter/downward chevron and add a separate lightly outlined clear X. Put match feedback below search, independent of shown-step counts and global completion totals; retain current debounce, ordered traversal, wrap, and anchor restoration.
- Present advice first in expanded details, keep `Wiki quick guide ↗` primary when supported, put Quest page under Related wiki pages, distinguish guide projections from observed levels, and retain all original fields and safe destinations.
- Insert actual local RuneLite skill icons before training skill names. Preserve typed source lower/upper targets and show `Current level: L - P%` for supported intervals; evaluate below-base, in-range, and reached-target states from real levels. Define conservative missing/ambiguous and multi-target behavior.
- Retain whole-card disclosure with independent nested actions and selection-safe gestures. Replace the existing position-aware Top/Bottom text control with one circular arrow icon above a reasonably sized, full-width Continue button; preserve navigation eligibility and focus/scroll intent.
- Extract cohesive UI components and pure presentation projections within the existing layers. Introduce only the typed source and publication boundaries needed by these changes; no broad service/domain rewrite or strict line-count gate.

## Capabilities

### New Capabilities

None. The change refines existing guide, progress, navigation, link, and release capabilities.

### Modified Capabilities

- `optimal-guide-content`: approved header actions/provenance, advice-first complete details, accessible whole-card interaction, and truthful conditional recovery presentation.
- `guide-navigation`: separate search feedback and query clearing, training-title skill icons, and the circular boundary/full-width Continue layout without changing traversal or targeting.
- `guide-progress-tracking`: typed training intervals, real-level interval states/percentages, conservative compound targets, and legacy-cache compatibility.
- `training-guide-links`: provider-specific visible labels while preserving destination validation, fallback, and user-only activation.
- `release-verification`: plan a compatible `3.1.0` implementation release and require the focused UI/training acceptance checklist, without inheriting historical publication authorization.

## Impact

Affected production areas are `ui/GuidePanel` and its extracted components, shared skill/text presentation, `guide/GuideParser`, `GuideClassifier`, `GuideRow`, cache reclassification, `progress/Completion`, and the plugin's action/publication adapter. The existing guide and progress services supply typed, consistently captured status to the new header; touched asynchronous callbacks retain their original run/account authority. Browser destinations continue through `LinkBrowser`; game observations remain local.

No new runtime dependency, remote asset loading, telemetry, game input, config-key rename, account-cache reset, route optimization, Quest Helper handoff, or alternative UI toolkit is proposed. Approximate 300-line production guidance supports cohesive responsibility boundaries; tests and a reasonable 303-line parser are exempt from size-driven splitting. General classifier/detector redesign and unrelated manual-recovery repairs remain in the separate refactor scope.

The presentation delta replaces two stale Quest Helper Open/feedback scenarios with the checkout's actual independent source-link behavior. The separate historical `quest-helper-handoff` capability is deferred; it is not an implementation obligation for this UI proposal.

This proposal is prepared on local, unpushed `codex/questcape-sidebar-ux`, created directly from `master` at `40eb54a520970db9edb77720d254d5b0f1b2f207`. Proposal creation changes no plugin behavior or version. Future implementation uses a minor increment from `3.0.1` to `3.1.0` because it adds compatible UI/progress features with lossless cache handling; any discovered incompatibility must revise that decision explicitly. Implementation, publication, PR creation, merge, and Hub delivery are separate steps.
