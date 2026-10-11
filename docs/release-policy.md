# Release versions and gates

Every implementation release updates `build.gradle` and `runelite-plugin.properties` together. Packaging tests inspect the actual built JAR and require its metadata version to equal the Gradle version.

- Major: breaking behavior, persisted-data compatibility, or public API changes.
- Minor: backward-compatible features.
- Patch: backward-compatible fixes.

Choose the highest applicable increment for the release and reset lower components. The automatic completion candidate bumped 0.1.0 to 1.0.0 as explicitly requested. The additional backward-compatible filters, search and projected icon features bump that candidate to 1.1.0. Historical evidence retains its original version.

Automated checks produce a test candidate. Only the user tests RuneScape; a successful build or JVM launch is insufficient. After explicit functional and Ubuntu stability acceptance, re-read the then-current RuneLite/Jagex rules and audit the complete production plugin for the exact candidate revision. Functional audit fixes require affected acceptance again.

Open the source PR only after acceptance and the final audit. The user explicitly confirmed the existing `master` destination on 2026-10-11. This development branch remains unpublished until that gate. The source repository is public, so a branch pushed there would also be public; an unpublished local branch is not a private GitHub repository.

The Plugin Hub update PR requires a separate later user instruction. Inspect the actual `plugins/questcape` manifest, reference the accepted publicly available full source commit, preserve required warnings, and run Hub checks before opening it. Source and Hub PRs remain subject to maintainer review; local audits cannot promise approval.
