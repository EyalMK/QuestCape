# Release versions and gates

Every implementation release updates `build.gradle` and `runelite-plugin.properties` together. Packaging tests inspect the actual built JAR and require its metadata version to equal the Gradle version.

- Major: breaking behavior, persisted-data compatibility, or public API changes.
- Minor: backward-compatible features.
- Patch: backward-compatible fixes.

Choose the highest applicable increment for the release and reset lower components. The automatic completion candidate bumped 0.1.0 to 1.0.0 as explicitly requested, then compatible navigation features produced 1.1.0. Removal of every remaining non-Kudos manual control produced 2.0.0. Removing within-group multiple selection produced 3.0.0. Corrections to clipped titles, skill-icon readiness, Karamja completion and search traversal produced patch 3.0.1. The compatible sidebar and typed-training features increment the new candidate to 3.1.0; schema-1 legacy fields, account/config keys and goal-completion meaning are retained. Historical evidence retains its original version.

Automated checks produce a test candidate. Only the user tests RuneScape; a successful build or JVM launch is insufficient. After explicit acceptance for the release's agreed scope, re-read the then-current RuneLite/Jagex rules and audit the complete production plugin for the exact candidate revision. Functional audit fixes require affected acceptance again. On 2026-10-12 the user accepted 3.0.1 and explicitly separated the unresolved Ubuntu 22 whole-system freeze into its own investigation, with its diagnosis and affected-machine acceptance still required there.

Open the source PR only after acceptance and the final audit. The user explicitly confirmed the existing `master` destination on 2026-10-11. This development branch remains unpublished until that gate. The source repository is public, so a branch pushed there would also be public; an unpublished local branch is not a private GitHub repository.

The Plugin Hub update PR requires explicit user authorization. Inspect the actual `plugins/questcape` manifest, reference the accepted publicly available full source commit, preserve required warnings, and inspect Hub CI after opening it. On 2026-10-12 the user authorized archiving the verified implementation, merging its source PR to `master`, then opening the Hub update from their fork with a description of at most five sentences and the appropriate classification/size labels. The Hub PR stays open for maintainer review; local audits cannot promise approval.

The current 3.3.0 candidate is local on `codex/questcape-sidebar-ux`. The user explicitly instructed **do not merge this branch into master**. Earlier source/Hub delivery instructions are historical and do not authorize a push, PR, merge or Hub update for this candidate. Its user acceptance and subsequent final audit remain pending.


The combined `strengthen-questcape-domain-boundaries` candidate increments 3.1.0
to **3.2.0** once: compatible selection/subsection features are the highest
applicable increment alongside fixes and internal refactoring. Exact schema-1
fields, config/account/manual tokens and supported plugin entry points remain.
Optional source evidence v2 retains v1/legacy reading. No supported external
domain extension contract was found; legacy row constructors remain passive
data adapters and source/cache producers prepare semantics centrally.

The preceding 3.1.0 candidate was **not accepted**: reported training and
interaction defects are corrected in 3.2.0. Its historical verification is retained,
and both changes' acceptance/final-audit gates stay pending for the combined
candidate. Earlier release authorization does not apply. See
[3.2.0 evidence](verification-3.2.0.md).


The 2026-10-16 interaction follow-up increments the candidate once to **3.3.0**:
training circles and explicit link-copy feedback are compatible features, taking
precedence over the fold-position/browser-message fixes. No cache/config/plugin
integration data changes. Prior candidate evidence remains historical; acceptance
and the later full rules audit now target the corrected candidate. Text-selection
alternatives are suggestions, not approved implementation changes. See
[3.3.0 evidence](verification-3.3.0.md). The branch remains unpushed and unmerged.
