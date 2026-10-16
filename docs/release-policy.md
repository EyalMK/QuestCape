# Release versions and gates

Every implementation release updates `build.gradle` and `runelite-plugin.properties` together. Packaging tests inspect the actual built JAR and require its metadata version to equal the Gradle version.

- Major: breaking behavior, persisted-data compatibility, or public API changes.
- Minor: backward-compatible features.
- Patch: backward-compatible fixes.

Choose the highest applicable increment for the release and reset lower components. The automatic completion candidate bumped 0.1.0 to 1.0.0 as explicitly requested, then compatible navigation features produced 1.1.0. Removal of every remaining non-Kudos manual control produced 2.0.0. Removing within-group multiple selection produced 3.0.0. Corrections to clipped titles, skill-icon readiness, Karamja completion and search traversal produced patch 3.0.1. The compatible sidebar and typed-training features increment the new candidate to 3.1.0; schema-1 legacy fields, account/config keys and goal-completion meaning are retained. Historical evidence retains its original version.

Automated checks produce a test candidate. Only the user tests RuneScape; a successful build or JVM launch is insufficient. After explicit acceptance for the release's agreed scope, re-read the then-current RuneLite/Jagex rules and audit the complete production plugin for the exact candidate revision. Functional audit fixes require affected acceptance again. On 2026-10-12 the user accepted 3.0.1 and explicitly separated the unresolved Ubuntu 22 whole-system freeze into its own investigation, with its diagnosis and affected-machine acceptance still required there.

Open the source PR only after acceptance and the final audit. The user explicitly confirmed the existing `master` destination on 2026-10-11. This development branch remains unpublished until that gate. The source repository is public, so a branch pushed there would also be public; an unpublished local branch is not a private GitHub repository.

The Plugin Hub update PR requires explicit user authorization. Inspect the actual `plugins/questcape` manifest, reference the accepted publicly available full source commit, preserve required warnings, and inspect Hub CI after opening it. On 2026-10-12 the user authorized archiving the verified implementation, merging its source PR to `master`, then opening the Hub update from their fork with a description of at most five sentences and the appropriate classification/size labels. The Hub PR stays open for maintainer review; local audits cannot promise approval.

The current combined candidate is **3.1.0**, selected once for the entire private
branch `codex/questcape-sidebar-ux` against accepted base 3.0.1. Select major,
minor or patch based on the branch's complete feature/compatibility scope, then
retain that version through follow-up work on the same branch. Do not increment
for each implementation iteration. The user explicitly selected 3.1.0 on
2026-10-16; compatible sidebar/selection/training features make minor appropriate.
The layered refactor preserves data/config/public API compatibility. Build,
properties, packaged metadata, HTTP identity and current docs must agree.

Earlier 3.2.0, 3.3.0 and 3.4.0 labels were unreleased private verification
iterations, not separate releases. Their records retain exact old hashes/version
context as historical evidence; they do not override this branch-level policy or
provide current acceptance. See [current candidate evidence](verification-3.1.0-current.md).
The cleanup retains canonical research, live captures and the approved mockup.

On 2026-10-16 the user explicitly confirmed **"Testing passed"** for the combined
3.1.0 candidate and authorized a source PR against master, then a Plugin Hub PR
through their fork. They subsequently requested spec sync and archival of both
completed active changes before delivery. This fresh instruction authorizes the
necessary branch pushes and PRs; neither PR is to be merged or closed by the
agent. The accepted implementation and post-acceptance rules audit are recorded
in [3.1.0 compliance](compliance-3.1.0.md). Earlier pending/no-publication entries
in verification histories describe their original time, not the current gate.
