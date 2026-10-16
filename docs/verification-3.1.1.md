# QuestCape 3.1.1 training-link fix

## Scope and cause

Theoatrix actions failed with `Plugin metadata resource is unavailable` before
their verification request could be sent. Shared `BoundedHttp` requires validated
product identity; the previous runtime resource was the root Hub descriptor,
copied only by this repository's custom Gradle `processResources` rule.

RuneLite's [standard packager](https://github.com/runelite/plugin-hub-tooling/blob/034eaffd5bce7bb678d35287b547874f3dba6b3b/package/src/main/java/net/runelite/pluginhub/packager/Plugin.java)
stages `src/main/java`, `src/main/resources` and `lombok.config`, then installs its
own [standard build script](https://github.com/runelite/plugin-hub-tooling/blob/034eaffd5bce7bb678d35287b547874f3dba6b3b/package/src/main/resources/net/runelite/pluginhub/packager/standard-build.gradle).
The root descriptor is read by the packager but is not a standard runtime
resource. A successful custom local build therefore missed the published failure.

Runtime metadata now lives at `/com/questcape/plugin.properties`, checked in
under `src/main/resources`. The root descriptor remains for Hub discovery;
the custom copy task is removed. Tests require displayName/version agreement
with that descriptor and version agreement with Gradle. Loading remains validated,
lazy, once per loader and worker-only. Existing URL/redirect limits, browser
ownership, progress meaning, config and data remain unchanged.

Version **3.1.1** is a patch from master 3.1.0 for a backward-compatible bug fix.
Build, descriptor, runtime resource and current development documentation agree.

## Automated evidence

The new source-resource-only regression failed before the fix with the reported
`PluginMetadata.MetadataException`. It isolates `src/main/resources` without
the root descriptor, resolves Magic's configured link and uses an in-memory
OkHttp interceptor to exercise request construction and successful destination
verification. It makes no external network request. Ignored log:
`.tmp/metadata-regression-before.log`.

On 16 October 2026, the affected metadata/packaging/training-link tests passed,
followed by `gradlew.bat --offline --no-daemon clean build resolvedVersions
--console=plain`: **124 tests across 28 suites**, zero failures/errors/skips.
Environment: Temurin **11.0.22**, Gradle **8.10**, Java `--release 11`, cached
RuneLite client/API **1.13.2**. The ordinary-JAR suite retains Java 11 bytecode,
own-class/resource/icon checks and verifies the root descriptor's registered
plugin class against the JAR. Logs: `.tmp/metadata-regression-after.log` and
`.tmp/metadata-full-build.log`. Existing unchecked-test and Gradle deprecation
notices remain.

An independent project at `.tmp/metadata-standard-build` stages only `src/main`
and uses the official unmodified standard template from tooling revision
`034eaffd5bce7bb678d35287b547874f3dba6b3b`. Template SHA-256:
`62AF0C5E207FFB0285773ECB6346EBC8F29D676691FFD919DDE0CB26AB73A3A5`.
A separate init script supplies the otherwise unspecified RuneLite dependency
version **1.13.2**; no repository custom resource or JAR task is applied.
The build used `gradlew.bat --no-daemon -p .tmp/metadata-standard-build -I
<absolute-init-script-path> jar --console=plain`, with the init script resolved
from `.tmp/metadata-standard-version.init.gradle`. It passed. The
offline attempt first identified an uncached template dependency,
`org.jetbrains:annotations:23.0.0`; the successful run downloaded it. Log:
`.tmp/metadata-standard-build.log`. This reproduces standard compilation/resource
packaging locally; it is not the full remote Hub API-review/signing pipeline or
a claim of compatibility with a newer RuneLite dependency.

A tiny Java 11 probe in `.tmp/metadata-probe` runs with only its own classes and
each built plugin JAR on the classpath. The actual default `PluginMetadata.Loader`
returns **`QuestCape/3.1.1 (RuneLite companion)`** for both JARs, and the probe
asserts `/runelite-plugin.properties` is absent. This verifies runtime loading
without a checkout/resource-directory classpath or reflective invocation.

- Ordinary artifact: `build/libs/questcape-3.1.1.jar`, SHA-256
  `2D31CFE2478E9B596E3CB215A19458731A17F55E64290B6635B7D058B30B0320`.
- Standard-template artifact: `.tmp/metadata-standard-build/build/libs/questcape-standard.jar`,
  SHA-256 `8F4877507C55FCD0885BF0ACDADF8A3243DE8AA8E51394E8C3870B559D614CE8`.

Both contain the plugin-owned runtime metadata; the standard artifact also retains
the source-resident META-INF license/notices. Artifacts/probe/logs remain ignored.
Base master: `8f7566fa15da8f510de68dfa54633dbc96cbdc39`; local branch:
`codex/fix-training-guide-metadata`. Strict OpenSpec validation and
`git diff --check` pass. Actual in-game/browser behavior is reserved for the user;
build success is not functional acceptance.

## User acceptance — pending

Offer to run `./gradlew run` (`.\gradlew.bat run` on Windows) from the plugin root.
Follow [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
to sign in to the development client. Only the user operates RuneScape.

1. From a fresh plugin start, click a Magic training step's Theoatrix guide.
   The relevant guide should open without the metadata error. The checking
   notice should clear after successful navigation.
2. Try another skill and each skill-specific action in a compound training step.
   Each should open its intended guide; opening it must not change completion.
3. Shift-right-click a training action: it should copy the URL with local feedback
   and no browser open. Disable/re-enable QuestCape and repeat a normal guide click.
4. Refresh the route once to exercise the shared metadata-backed Wiki transport.
   Existing progress and training state should remain correct.

## Delivery after acceptance

- [ ] Record explicit user confirmation for this candidate.
- [ ] Perform and record the current whole-plugin rules audit.
- [ ] Sync specs and archive `fix-training-guide-metadata` using the requested skill.
- [ ] Create and attach the source PR targeting master; inspect checks and update
  master as authorized by the user's release instruction.
- [ ] Re-read the newest full master SHA and upstream `plugins/questcape` manifest.
- [ ] Submit and attach the Hub update through `EyalMK/plugin-hub`, preserving the
  warning, and apply `plugin change` and the available small-size label. Inspect CI.

No archive, remote branch publication, source PR, source merge or Hub update has
occurred for this fix. Historical release acceptance does not cover this candidate.
