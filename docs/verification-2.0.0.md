# QuestCape 2.0.0 candidate verification

Date: 2026-10-12 (Asia/Jerusalem). Extends unpublished candidate `610c76ab7769bd265d5ca94fa83489106c74c2c9` on `codex/automatic-activity-progress`. The exact candidate commit and artifact digest are recorded in the local OpenSpec acceptance record at handoff. No branch or PR has been published.

## Changes and evidence

The user clarified that only Kudos should retain manual completion, including removal of the Herbi Flax checkbox. This breaking behavior bumps 1.1.0 to 2.0.0 under the repository's semantic version policy. Build version, packaged metadata, User-Agent and current development documentation agree.

Four balloon routes, Stronghold's three coin rewards, Peter's Museum Camp reward and the Herbi Flax hand-in now use explicit local game state. [Activity detection](activity-detection.md) records named gameval predicates and exact Quest Helper/Task Checker source revisions. Existing manual keys for these actions are inert and stale callbacks cannot write overrides; both Kudos keys stay unchanged. Missing evidence and future unsupported actions remain unknown without checkboxes. Account/session guards and coalesced ClientThread observation remain in use.

The captured guide and current public guide already parse complete balloon titles in this candidate. Regressions verify all four full titles and their last rendered line inside narrow cards. An older cached title ending in “to” is repaired only if retained safe Wiki links identify exactly one supported destination; ambiguous or missing evidence stays unknown. This is a conservative repair, not proof of the user's original cache contents or a reproduced upstream Wiki defect.

Additional info uses an inert styled document with local RuneLite skill icons before whole skill-name mentions. Original wording, repeated mentions, aliases, wrapping and accessible text are preserved. Icons update when worker-loaded images reach the EDT. Projected levels retain number/icon order. No remote guide image loading, new dependencies, network character requests or game input automation are introduced.

## Automated verification

Windows, Temurin 11.0.22, Gradle 8.10, RuneLite 1.13.1. `./gradlew.bat --no-daemon --offline build --console=plain` passes 64 tests, including Java 11 distribution/metadata checks. New checks cover independent balloon flags and exact boundaries, reward-versus-prerequisite separation, Stronghold completion without boots, bitmap/sign-bit isolation, all seven retired manual overrides, stable Kudos persistence, cache repair/ambiguity, complete titles, skill aliases/icon order/late updates and narrow UI layout. Existing storage, session, lifecycle, filters/search and automatic diary/miniquest checks continue to pass.

`verifyRemoteContracts` fetches only the public Wiki guide and passes revision 15358869, 351 rows: all 24 diary rows, 14 miniquest rows and seven additional automatic actions map correctly. No account or RuneScape interaction occurs. Synthetic previews in `build/ui-evidence/` include `sidebar-balloon.png` and `sidebar-skill-info.png`; they are build outputs and are not committed. Logs are in ignored `.research/unlocks-build.log` and `.research/unlocks-public-contract.log`.

## Pending user gates

Only the user can verify actual game behavior. Test the [development client](development.md), following [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts), and the local OpenSpec acceptance checklist. Compare completed/incomplete routes and rewards, old checks, Kudos reversibility, skill text, filters/search, restarts/hops and account isolation.

The original Ubuntu permanent freeze is still unverified: there is no affected-machine reproduction, thread evidence or confirmed cause. Existing resilience changes do not prove it fixed. Require affected Ubuntu/Jagex Launcher acceptance before claiming resolution.

After explicit acceptance of the exact candidate, perform a fresh complete audit against then-current RuneLite/Jagex rules and resolve any violations before opening the source PR to `master`. Current rule references were checked during implementation; that is not the final post-acceptance audit or maintainer approval. The Plugin Hub PR remains gated by a separate explicit user instruction.
