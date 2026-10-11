# QuestCape 1.0.0 candidate verification

Date: 2026-10-11. Baseline: `4e861eb` (0.1.0). Development branch: `codex/automatic-activity-progress`, unpublished. The exact candidate commit is recorded in the local OpenSpec acceptance record at handoff. This report is preliminary; user acceptance and the final complete rules audit are pending.

## Environment and checks

Windows host, Temurin 11.0.22, Gradle 8.10, RuneLite client/API 1.13.1 from `latest.release`. Baseline `build resolvedVersions` passed. Java 17 on this host failed to establish Gradle's local loopback socket; Java 11 passed. That tooling issue is unrelated to the reported Ubuntu freeze.

Commands use `JAVA_HOME` pointing to Java 11:

```powershell
./gradlew.bat --no-daemon --offline build resolvedVersions verifyRemoteContracts --console=plain
```

Automated coverage includes all 48 diary tier identities, task/reward separation, partial/finished additional miniquests, captured and current route identities, retired manual-check rejection, unrelated checks, account/mode/session isolation, local explicit Sync, cache reclassification, malformed/oversized/unknown-schema caches, directory conflicts, denied access, atomic-move refusal, cleanup failure, write recovery, worker cancellation, sidebar controls, Java 11 class versions, actual distribution JAR contents and metadata. Failure injections are synthetic; they do not constitute Linux filesystem or game verification.

The delayed-save regression now requires a valid live observation to be available while saving is blocked, then requires logout to finish within one second and prevents stale character publication. Restoring the previous publication ordering makes that test fail at the live-observation assertion. The implementation publishes the observation before the worker saves it, and retains the session check. This proves the storage-resilience improvement, not the cause of the Ubuntu freeze.

## Storage and preliminary rules review

Final automated run: **49 tests passed; build, Java 11 packaging and public guide contract passed**. Current guide revision 15358869 has 351 rows, including 24 diary rows and 14 miniquest rows; all have automatic identities and no manual control eligibility. Local diagnostic logs are under ignored `.research/automatic-progress-final.log` and `.research/publication-before.log`. The distribution is `build/libs/questcape-1.0.0.jar`.

Production storage uses the supported `Plugin.getPluginDirectory()` and `Filepath` API, with descriptor `internalName="questcape"`, `legacyDataDirectory="questcape"`. RuneLite migrates the old plugin directory only when the new destination is absent. Initialization/migration is lazy on the worker. UTF-8 reads are bounded even if a file grows after the size check; writes use same-directory temporary files and require atomic replacement. Unreadable originals are retained until an explicit successful recovery read. Test-only `Filepath.Unchecked` roots are excluded from the JAR.

Icon decoding moved from startup to a background worker and uses `MemoryCacheImageInputStream`, avoiding ImageIO's default temporary-file path. Swing creation and updates remain on the EDT. HTTP remains injected OkHttp on background workers, Gson remains injected, and no runtime dependencies, config toggles, input automation, cross-plugin controls, or server actions were added. Logging is debug-only. No waits were added to startup/shutdown. No recurring tasks need cancellation.

Preliminary production scans found no direct filesystem access, reflection, native access, process execution, code loading/generation, Java serialization, sleeps, blocking executor shutdown, or client-thread network/disk operations. Java 11 and permissive licensing are retained. This is not a final compliance claim or maintainer approval. The fresh whole-plugin review must follow user acceptance, using the then-current [Hub guidance](https://github.com/runelite/plugin-hub#submitting-a-plugin), [rejected features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), [Jagex guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), current Hub API restrictions and repository AGENTS.md.

## Ubuntu evidence and remaining gates

The user reports Ubuntu 22, latest RuneLite through Jagex Launcher for Linux (Beta), permanently freezing immediately or within a few seconds of login, possibly before world entry. Exact OS/client/JDK versions and logs are unavailable because the affected PC is elsewhere. No Ubuntu desktop or game session was available for reproduction. Whether the entire desktop or only RuneLite freezes is unconfirmed.

Source inspection found disk operations on background workers, nonblocking logout/shutdown, bounded queues and response sizes, and session guards. Storage failures previously prevented live state publication; they now leave a usable in-memory state and concise unsaved status. A suspected unchanged-navigation-text layout loop was rejected: its regression also passed with the original implementation, and that change/test was removed. Do not present general hardening as a confirmed freeze diagnosis or claim that absence of exceptions proves responsiveness.

Only the user tests in-game behavior. Required checks: automatic complete/incomplete diaries and miniquests (especially quiz/Knight Waves), no manual controls or legacy overrides, preserved unrelated manual actions, totals/navigation, account/mode isolation, restart/hop/reconnect, and sustained responsiveness on the affected Ubuntu machine before and after world entry, Sync and guide refresh. Record unavailable cases as limitations. See the local OpenSpec `acceptance-tests.md` for the full checklist.

After acceptance: perform the final complete rules audit, resolve findings and repeat affected acceptance, then open the source PR with its intended base confirmed. Do not publish the Plugin Hub PR until separately instructed.
