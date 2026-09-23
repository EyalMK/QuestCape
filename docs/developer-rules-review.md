# Developer rules review — 2026-09-23

Scope: all production Java, build configuration, packaged resources, progress storage, external requests, and user-facing integration paths on the local character sync branch. This is a source review, not a claim of RuneLite approval or in-game acceptance.

The review uses the repository's AGENTS.md, the [Plugin Hub submission guidance](https://github.com/runelite/plugin-hub#submitting-a-plugin), [RuneLite's rejected features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), and [Jagex's third-party client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), checked on the review date.

| Area | Source review finding |
| --- | --- |
| Character privacy | Quest/miniquest/subquest states and real skills come from the current client. No character HTTP request, upload, listening server, other-player lookup, credential storage, or crowdsourcing. |
| Remaining external access | The fixed public wiki guide endpoint is fetched for route content. User-triggered training actions validate Theoatrix URLs and redirects. Source/license/training links open with RuneLite LinkBrowser. No character data is included in those requests. The Hub manifest must retain its third-party IP-address warning. |
| HTTP and JSON | Injected OkHttpClient and Gson; bounded requests, timeouts, cancellation, and allowlisted destinations. Blocking HTTP runs on the dedicated network executor, never ClientThread. No new server-feature config toggle is introduced. |
| Threading and lifecycle | Client reads happen on ClientThread after readiness. Immutable snapshots go to the state worker. Disk operations no longer hold locks used by logout or guide cancellation. Shutdown cancels requests and shuts down both executors without waiting. No sleeps, latches, or recurring scheduled tasks. |
| Account isolation | Profile/account-hash/mode readiness precedes reads; profile/mode scopes and existing config keys remain stable. Session tokens reject stale publication after logout. Manual checks survive same-character sync and restart. |
| Storage | Runtime JSON cache files remain below RuneLite.RUNELITE_DIR/questcape. Atomic replacement and schema validation retained. Test-only temporary/build files are outside the distribution JAR. |
| Game interaction | No injected game input, autotyping, outgoing chat modification, server action menu entries, game widget changes, or detached-camera interaction. Quest Helper integration touches its existing Swing sidebar only after a user action. It does not simulate mouse/keyboard input into the game. |
| Combat and content | No boss predictions, combat prayer recommendations, PvP scouting, hazard overlays, simulations, or player-supplied-ID features. QuestCape renders route text and completion state in a sidebar. |
| Language and dependencies | Java 11 bytecode; no reflection, native memory, JNI/JNA calls, subprocesses, runtime code loading/generation, or Java serialization in production code. No new runtime dependencies or direct declarations of client transitive dependencies. |
| API and performance | Public Quest/Skill APIs; no raw game IDs or widget lookups. Event-triggered progress reads, no scene scanning, no overlays, and coalesced UI work. |
| Packaging and assets | Plugin-specific names; BSD-2 code license and separate content attribution. Resources loaded from classpath streams. Navigation/root icon is an actual 32x32 PNG. No service-loader entry or generated build files tracked. Character preview data is synthetic. |

The threading review resulted in changes to ProgressService and GuideRepository: disk writes are outside locks needed by foreground callers. Regression tests hold a disk write open and verify that logout/cancel returns promptly and canceled work cannot publish an active view.

The readability pass expands method, constructor, callback-block, and control-flow bodies throughout production and test Java, following [RuneLite's brace and indentation conventions](https://github.com/runelite/runelite/wiki/Code-Conventions). Formatting is isolated from the following simplifications in its own commit.

| Removed redundancy | Evidence and retained behavior |
| --- | --- |
| Null quest-state check | [RuneLite 1.12.39 Quest.getState](https://github.com/runelite/runelite/blob/runelite-parent-1.12.39/runelite-api/src/main/java/net/runelite/api/Quest.java) returns FINISHED, NOT_STARTED, or IN_PROGRESS on every branch. Login/profile readiness checks remain. |
| Aggregate-skill exclusion | [Skill 1.12.39](https://github.com/runelite/runelite/blob/runelite-parent-1.12.39/runelite-api/src/main/java/net/runelite/api/Skill.java) declares OVERALL as a deprecated null field, outside the enum values. Every real skill is read; nonpositive unready levels remain excluded. |
| Null direct-response body check | [OkHttp 3.14.9 Response.body](https://github.com/square/okhttp/blob/parent-3.14.9/okhttp/src/main/java/okhttp3/Response.java) guarantees a body on the result of Call.execute. Conditional 304 handling, declared/actual size limits, closure, timeouts, and cancellation remain. |
| Null enum-lookup result check | [Enum.valueOf](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Enum.html) returns the constant or throws. Null input and unknown-name validation remain. |
| Explicit same-component test before ancestry lookup | [Java 11 SwingUtilities.isDescendingFrom](https://github.com/openjdk/jdk11u/blob/master/src/java.desktop/share/classes/javax/swing/SwingUtilities.java) includes component identity. Direct and nested sidebar placement retain the same behavior. |
| Guide title fallback and repeated URL validation | The seven normalized headers are validated before row extraction. Title text now comes directly from its resolved cell. The title helper receives the already validated absolute wiki URL, so it no longer repeats validation or catches impossible URI failures. External URL validation remains at the boundary. |
| Duplicate Sync eligibility condition | GuidePanel already requires both readiness and the same non-null progress snapshot before enabling Sync. The caller now supplies readiness alone. |
| Unused progress-view alias | ProgressService.viewed had no callers and duplicated current; current remains the single accessor. |

Lifecycle/session checks on both sides of asynchronous or disk work are intentional: logout, restart, or character switching can occur between them. Profile/account-hash/mode matching, saved-data validation, external-link allowlists, and third-party Swing-layout checks also remain because those states are not guaranteed by the client API.

The panel follow-up uses SpriteManager's public asynchronous API with gameval constants for three standard icons, then updates Swing on the EDT. Custom question/lock/task marks are small vector icons; no new downloaded assets, runtime dependencies, or external requests are introduced. Combat-level thresholds use Experience.getCombatLevel from existing real-skill snapshots. Cached rows are reclassified on the state worker using stored guide fields; profile data and existing manual action keys are preserved. Informational notes are excluded from progress and navigation. The user accepted the prior character-sync revision; the new panel/classification checklist remains pending.

No prohibited feature was identified in this source pass. Final Hub acceptance remains with RuneLite reviewers. The optional Quest Helper search fallback depends on another plugin's Swing layout and can fail recoverably if that layout changes; automatic confirmed resume remains unavailable. The user must complete the [in-game checklist](verification.md) before this feature is merged or closed.
