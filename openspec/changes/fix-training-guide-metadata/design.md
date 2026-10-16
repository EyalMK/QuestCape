## Context

`PluginMetadata.Loader` lazily reads `/runelite-plugin.properties` before `BoundedHttp` constructs its first request. The root descriptor is only copied into the classpath by a custom `processResources` rule. RuneLite's official `Plugin.java` lists `src/main/java`, `src/main/resources` and `lombok.config` as standard build inputs, and installs its own `standard-build.gradle`. Thus the published standard build omits the runtime descriptor even though this repository's ordinary build tests pass. Cached routes can hide the shared HTTP failure until a training action is clicked.

## Goals / Non-Goals

**Goals:** Restore training-link verification and shared HTTP identity across standard-resource, development and ordinary-JAR execution; retain metadata validation and worker-only lazy loading; prevent identity/version drift.

**Non-Goals:** Change guide URLs, request policy, UI gestures, persisted data, account/config keys or game interaction. No new runtime dependency.

## Decisions

1. Add `src/main/resources/com/questcape/plugin.properties` containing `displayName` and `version`. This plugin-owned path is the sole runtime metadata source and is copied by standard Java resource processing. Keep the required root descriptor for Hub discovery. Remove the custom root-descriptor copy. A hardcoded User-Agent or changing to `build=gradle` would hide the packaging contract and is unnecessary.
2. Regression tests compare runtime metadata with the root descriptor and Gradle version, then read it from an isolated ordinary JAR. Also construct a default-loader request from a source-resource classpath with no root descriptor and exercise a Theoatrix response using an in-memory OkHttp interceptor. No live external request is needed for these tests. The ordinary JAR must not rely on the root descriptor being present.
3. Reproduce resource/compile packaging with the official standard builder script in an ignored temporary project, without the repository's custom tasks. Inspect the resulting JAR and record the limits of this local check.
4. Use patch 3.1.1 from master release 3.1.0: this restores intended behavior and changes no saved-data or public API contract. Keep build, root descriptor, runtime properties and current developer documentation aligned.
5. Keep this change active pending user in-game confirmation. After acceptance and the required rules audit, sync its delta specs, archive, open the source PR targeting master, and use master's newly updated full commit for the Hub PR. Preserve the manifest warning and request `plugin change` and the actual available small-size label. The user's current release instruction supplies publication authorization; archive/publishing waits for their functional confirmation.

## Risks / Trade-offs

- Runtime identity appears in both a source resource and the Hub discovery descriptor → tests require exact display name/version equality and Gradle-version equality.
- Passing local Gradle tests previously missed standard-build omission → test source-only resource loading, isolate JAR loading, and independently build with the official standard template.
- Public endpoints or browser invocation can fail for unrelated reasons → retain existing allowlists, redirects, retry/copy feedback and manual in-game acceptance.
- Offline compilation uses cached RuneLite dependencies → document resolved versions and check remote Hub CI during delivery.

## Migration Plan

No data/config migration is required. Deploy only after user acceptance, using the accepted master commit. A source revert and a separately reviewed manifest update provide rollback if needed.

## Open Questions

Only the user can confirm real training-action/browser behavior in RuneLite. Source merge status and latest master SHA will be re-read at delivery time before constructing the Hub update.
