# Release versions and gates

Every implementation release updates `build.gradle` and `runelite-plugin.properties` together. Packaging tests inspect the actual built JAR and require its metadata version to equal the Gradle version.

- Major: breaking behavior, persisted-data compatibility, or public API changes.
- Minor: backward-compatible features.
- Patch: backward-compatible fixes.

Choose the highest applicable increment for the release and reset lower components. The automatic completion candidate bumped 0.1.0 to 1.0.0 as explicitly requested, then compatible navigation features produced 1.1.0. Removal of every remaining non-Kudos manual control produced 2.0.0. Removing within-group multiple selection produced 3.0.0. Corrections to clipped titles, skill-icon readiness, Karamja completion and search traversal produce the current patch candidate 3.0.1. Historical evidence retains its original version.

Automated checks produce a test candidate. Only the user tests RuneScape; a successful build or JVM launch is insufficient. After explicit acceptance for the release's agreed scope, re-read the then-current RuneLite/Jagex rules and audit the complete production plugin for the exact candidate revision. Functional audit fixes require affected acceptance again. On 2026-10-12 the user accepted 3.0.1 and explicitly separated the unresolved Ubuntu 22 whole-system freeze into its own investigation, with its diagnosis and affected-machine acceptance still required there.

Open the source PR only after acceptance and the final audit. The user explicitly confirmed the existing `master` destination on 2026-10-11. This development branch remains unpublished until that gate. The source repository is public, so a branch pushed there would also be public; an unpublished local branch is not a private GitHub repository.

The Plugin Hub update PR requires explicit user authorization. Inspect the actual `plugins/questcape` manifest, reference the accepted publicly available full source commit, preserve required warnings, and inspect Hub CI after opening it. On 2026-10-12 the user authorized archiving the verified implementation, merging its source PR to `master`, then opening the Hub update from their fork with a description of at most five sentences and the appropriate classification/size labels. The Hub PR stays open for maintainer review; local audits cannot promise approval.
