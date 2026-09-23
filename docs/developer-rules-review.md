# Developer rules review — 2026-09-23

Scope: all production Java, build configuration, packaged resources, progress storage, external requests, and sidebar and cross-plugin boundaries on the local character sync branch. This is a source review, not a claim of RuneLite approval or in-game acceptance.

RuneLite reviewers [explicitly disallowed manipulating another plugin’s UI](https://github.com/runelite/plugin-hub/pull/16488#issuecomment-5799882730). This supersedes the earlier review’s mistaken conclusion that public Swing access was acceptable. The entire UI adapter, dependency registry, resume bridge/store/coordinator, configuration item, and related controls have been removed. No replacement cross-plugin integration is included. Any future integration requires a supported upstream API and separate work.

The review uses the repository's AGENTS.md, the [Plugin Hub submission guidance](https://github.com/runelite/plugin-hub#submitting-a-plugin), [RuneLite's rejected features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), and [Jagex's third-party client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), checked on the review date.

| Area | Source review finding |
| --- | --- |
| Character privacy | Quest/miniquest/subquest states and real skills come from the current client. No character HTTP request, upload, listening server, other-player lookup, credential storage, or crowdsourcing. |
| Remaining external access | The fixed public wiki guide endpoint is fetched for route content. User-triggered training actions validate Theoatrix URLs and redirects. Source/license/training links open with RuneLite LinkBrowser. No character data is included in those requests. The Hub manifest must retain its third-party IP-address warning. |
| HTTP and JSON | Injected OkHttpClient and Gson; bounded requests, timeouts, cancellation, and allowlisted destinations. Blocking HTTP runs on the dedicated network executor, never ClientThread. No new server-feature config toggle is introduced. |
| Threading and lifecycle | Client reads happen on ClientThread after readiness. Immutable snapshots go to the state worker. Disk operations no longer hold locks used by logout or guide cancellation. Shutdown cancels requests and shuts down both executors without waiting. No sleeps, latches, or recurring scheduled tasks. |
| Account isolation | Profile/account-hash/mode readiness precedes reads; profile/mode scopes remain stable. Obsolete resume settings and intent values are left in storage without being read, written, renamed, or deleted. Session tokens reject stale publication after logout. Manual checks survive same-character sync and restart. |
| Storage | Runtime JSON cache files remain below RuneLite.RUNELITE_DIR/questcape. Atomic replacement and schema validation retained. Test-only temporary/build files are outside the distribution JAR. |
| Game interaction | No injected game input, autotyping, outgoing chat modification, server action menu entries, game widget changes, or detached-camera interaction. UI access is limited to QuestCape’s own panel and registration/removal of its own navigation button. No other plugin is discovered, selected, searched, or clicked. |
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
| Guide title fallback and repeated URL validation | The seven normalized headers are validated before row extraction. Title text now comes directly from its resolved cell. The title helper receives the already validated absolute wiki URL, so it no longer repeats validation or catches impossible URI failures. External URL validation remains at the boundary. |
| Duplicate Sync eligibility condition | GuidePanel already requires both readiness and the same non-null progress snapshot before enabling Sync. The caller now supplies readiness alone. |
| Unused progress-view alias | ProgressService.viewed had no callers and duplicated current; current remains the single accessor. |

Lifecycle/session checks on both sides of asynchronous or disk work are intentional: logout, restart, or character switching can occur between them. Profile/account-hash/mode matching, saved-data validation, and external-link allowlists also remain because those states are not guaranteed by the client API.

The panel follow-up uses SpriteManager's public asynchronous API with gameval constants for three standard icons, then updates Swing on the EDT. Custom question/lock/task marks are small vector icons; no new downloaded assets, runtime dependencies, or external requests are introduced. Combat-level thresholds use Experience.getCombatLevel from existing real-skill snapshots. Cached rows are reclassified on the state worker using stored guide fields; profile data and existing manual action keys are preserved. Informational notes are excluded from progress and navigation. The user accepted both the prior character-sync revision and the panel/classification and removal checklist on 2026-09-23.

The previously identified cross-plugin UI violation is removed. The remaining source was checked against the rules above; this is not RuneLite approval. The user confirmed the [in-game checklist](verification.md) for `84330bc` and authorized source merge and a Hub re-enable PR on 2026-09-23.
