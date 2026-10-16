## Why

Every Theoatrix training action can fail with `Plugin metadata resource is unavailable` before any HTTP request is sent. Runtime identity currently depends on a root descriptor copied by this repository's custom Gradle task, which is not a portable source resource for IDE or standard Plugin Hub builds.

## What Changes

- Store validated runtime product metadata in a plugin-owned resource under `src/main/resources`, and load it without relying on a custom root-file copy.
- Check runtime identity against the root Plugin Hub descriptor and Gradle release version to prevent drift.
- Cover source-resource execution and isolated distribution packaging with regression tests, including a successful user-triggered training request.
- Release the backward-compatible fix as patch **3.1.1** against master 3.1.0.
- Require user in-game confirmation before archival and source delivery; then submit the Hub update using the newly updated master commit and the requested classification/size labels.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `plugin-product-metadata`: Make the sole runtime metadata source a plugin-owned standard source resource, with build-time checks against the Hub descriptor.
- `training-guide-links`: Ensure packaged metadata permits verified training navigation in development and standard builds.

## Impact

Changes affect `PluginMetadata`, metadata resources, build/version declarations, packaging/HTTP tests and developer release evidence. Existing HTTP allowlists, cancellation, header validation, client/EDT boundaries, progress data and config keys remain in force. No runtime dependency or new external service is introduced. Source PR and Hub delivery remain gated on explicit user acceptance.
