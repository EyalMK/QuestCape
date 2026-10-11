# QuestCape 3.0.1 post-acceptance compliance review

Reviewed on 2026-10-12, after the user stated: "I've done the acceptance tests in-game, all good". The user also explicitly separated the unresolved Ubuntu 22 entire-system freeze from this release. Their acceptance covers the current implementation; exact acceptance-machine OS/client/JDK versions were not supplied. No agent-run game interaction or clean JVM launch is counted as acceptance.

Accepted production source: `3025e3aa2593ffe254e8dc7829915df2fb9f8c7c`. Documentation/branding head at review: `17c1b51e8b50d6d65d79202bc2e85cce7c421786`. Both have the same `src/main` tree: `eccc59e7c384580a548e1ad9cde55d8303d5c72b`. This review changes documentation only, with no functional change requiring renewed acceptance.

## Guidance re-read after acceptance

- [RuneLite Plugin Hub development, packaging and submission rules](https://github.com/runelite/plugin-hub)
- [RuneLite rejected or rolled-back features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
- [Jagex third-party client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1)
- Repository AGENTS.md, including Java 11, gameval constants, Filepath, lifecycle, networking, logging and human acceptance requirements.

## Complete production review

All 23 production Java files, Gradle configuration, descriptor/metadata, resources and licensing were reviewed. Searches for prohibited APIs were followed by call-site/thread review rather than interpreted as proof on their own.

| Area | Finding and evidence |
| --- | --- |
| Language and dependencies | Java only, `options.release=11`; no production reflection, native access, process execution, downloaded/generated code or Java serialization. No additional runtime dependencies; Gson/OkHttp are injected from RuneLite. Mockito and the packaging test's isolated classloader are test-only and excluded from the ordinary distribution. |
| Game behavior | Reads the current character's supported Quest/skill/gameval state after readiness. No input injection, chat modification, server-action menu entries, game widget manipulation, combat/PvP predictions, scene scanning or other-player collection. Swing key bindings navigate the plugin panel only. |
| Storage and migration | `getPluginDirectory()` supplies RuneLite Filepath under `.runelite/plugin-data/questcape`; descriptor `legacyDataDirectory="questcape"` uses RuneLite's approved legacy migration. Fixed categories, hashed filenames, bounded UTF-8 JSON, same-directory atomic staging and controlled failure outcomes preserve prior records. No runtime arbitrary filesystem access. |
| Threading and lifecycle | Client reads stay on ClientThread; blocking HTTP/storage and icon preparation use background executors. Foreground startup/shutdown never wait for I/O or executor termination. Shutdown invalidates generations, cancels HTTP/guide work, stops the Swing timer/popups, removes navigation and calls `shutdownNow`. No sleeps, latches or synchronous foreground waits. Session guards reject late account/UI publications. |
| HTTP and privacy | Derived injected OkHttp client, bounded bodies/timeouts and validated HTTPS destinations. Synchronous HTTP call sites run only on network workers. No character identity/progress/credentials submitted to servers. Public wiki/training requests expose the ordinary connection IP; retain the manifest's existing third-party warning verbatim. Local Sync makes no account-service request. |
| Config and links | No config keys/groups added or renamed and no third-party feature toggle introduced. User-triggered external links use RuneLite LinkBrowser. No Desktop API or remote guide-image loading. |
| Performance and diagnostics | Event-coalesced observations, bounded network queue, refresh cooldown and 60-second failed-save retry gating; no per-frame overlay work. Developer failure logging uses debug without identity/credentials or high-frequency INFO logs. Search debounce/cancellation and disposal are covered by synthetic UI checks. |
| Packaging and assets | `build=standard`, renamed package/project/descriptor, BSD-2 license and content notices. Ordinary JAR contains own Java 11 classes/resources only, no tests/fixtures/native code/service registration. Runtime icons load from classpath streams; navigation/root icons are real 32x32 PNGs. README branding is documentation-only, uses fictional data and is labeled illustrative. No tracked build artifacts. |

No developer-rule violation was found in the accepted production code. This is a source and packaging review, not RuneLite maintainer approval or a promise of Hub acceptance.

## Verification

The final `./gradlew.bat --no-daemon --offline build --console=plain` succeeded using Temurin 11.0.22, Gradle 8.10 and resolved RuneLite 1.13.1 on Windows. Unchanged compile/test outputs were up to date; their XML reports contain 72 tests, zero failures, errors or skips. The packaging test checks every ordinary-JAR class for Java 11 bytecode, metadata/version consistency and packaged resource loading. Earlier candidate evidence and public guide contract checks are retained in [verification-3.0.1.md](verification-3.0.1.md).

Ordinary JAR: `build/libs/questcape-3.0.1.jar`; SHA-256 `68914B4A33548994E6C5F280C84BB286540110658A0EFA7395F4A8928A574B99`.

The Ubuntu 22 whole-OS freeze remains unresolved, with no confirmed root cause or affected-machine retest. It is tracked separately as OpenSpec change `diagnose-ubuntu-22-system-freeze`, retaining the original incomplete investigation/fix/Ubuntu acceptance tasks. Storage hardening and Windows checks are not evidence of a freeze fix. [Evidence collection instructions](ubuntu-freeze-diagnostics.md) remain available.

The user authorized source publication/merge to existing `master` after archive and a subsequent Plugin Hub PR from `EyalMK/plugin-hub`. The Hub manifest changes only the accepted merged source hash; its warning is preserved. The Hub's existing label names are `plugin change` and `size-xl` (the production-source delta exceeds 1,500 changed lines). Remote CI and Hub review status are checked during delivery and must not be described as passing before their results arrive.
