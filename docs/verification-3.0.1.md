# QuestCape 3.0.1 correction candidate — 2026-10-12

Status: automated candidate awaiting affected in-game acceptance, the original Ubuntu investigation and the final full compliance audit. Exact revision and JAR digest are recorded in the local OpenSpec acceptance record. The branch remains unpublished; no PR has been opened. The canceled README/branding request is excluded.

## Findings and corrections

Both existing local guide caches contain complete balloon destinations. The previous synthetic tests used the default Swing look and feel. Installing RuneLiteLAF reproduces the reported clipping: Crafting Guild's last line ends at 68px, outside a 48px title. The custom root-view measurement undercounts wrapped height under FlatTextAreaUI. Delegating preferred height to the installed UI fixes the failing regression without changing parsed titles, links or automatic identities. This distinguishes the reproduced rendering defect from an unproven parsing defect.

SkillText substituted the generic Training icon whenever its skill map was empty. Skill images were queued after sidebar creation, potentially behind guide-cache work. Prepare bundled 16px/18px images in the initial worker task and apply them before toolbar publication. Remove generic substitution; absent images reserve blank space, with late actual images still supported on the EDT. Startup-order and pixel-comparison tests verify the actual Herblore/Cooking artwork. This fixes a demonstrated fallback/readiness weakness; the user's exact live appearance still requires acceptance.

Karamja Easy, Medium and Hard completion use value 2, not 1. RuneLite's [area_task_complete script at 78f72218](https://github.com/runelite/cs2-scripts/blob/78f7221849595b3439e8a5d36f40aea93d9685c6/scripts/%5Bproc%2Carea_task_complete%5D.cs2) confirms all three values and Elite's value 1. Store each tier's expected completion value explicitly. Reward flags remain separate and account/session guards are unchanged. See [activity predicates](activity-detection.md).

Search previously sorted by relevance before step number. Relevance now selects only the initial jump; results, counter indices and Enter traversal use ascending step numbers. The user's exact example is covered: 124, 257, 295. Initial best matches can start at an index other than 1/N. Filtering, restoration, live reconciliation and double-Enter wrap remain.

These corrections increment the patch from 3.0.0 to 3.0.1 under the existing release policy; Gradle, metadata and User-Agent agree. No new dependencies, network requests, config/storage schema changes or game input automation are introduced. RuneLite is never controlled by these tests.

## Verification and pending gates

Windows, Temurin 11.0.22, Gradle 8.10, RuneLite 1.13.1. The Java 11 full build passes 72 tests, including packaged version checks. UI regressions now use RuneLiteLAF and verify all four balloon destinations. New tests cover Karamja 0/1/2/3 boundaries, reward/Elite separation, route-order search with the reported step numbers, initial relevance and actual skill images at publication. The lifecycle fixture now switches its current account through an AtomicReference instead of restubbing a mock concurrently with worker reads; this removes a race caught by the final build. Existing persistence/lifecycle/session regressions remain passing. Synthetic `sidebar-balloon.png` and `sidebar-skill-info.png` previews were inspected; images/logs remain ignored build/research outputs. Public guide coverage is unchanged because parsing/mappings are unchanged; no game behavior is inferred from these checks.

Offer to launch `./gradlew run` (`./gradlew.bat run` on Windows) from the plugin root; use [Jagex Account development login instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts). User acceptance must confirm all destination names at normal width, actual inline skill icons after startup/restart, the completed Easy Karamja state, and 124/257/295 search order, alongside prior outstanding checks. The read-only RuneLite capture was rejected by automatic approval review; no game screenshot or input was obtained for this correction.

The affected Ubuntu freeze has no confirmed cause or retest. Only after acceptance of the exact candidate, including Ubuntu stability, perform the fresh complete RuneLite/Jagex audit and open the source PR to master. Functional audit fixes require affected acceptance again. The Plugin Hub PR remains gated by a separate explicit user instruction.
