# plugin-product-metadata Specification

## Purpose
Read a single validated packaged product identity for development, distribution and bounded HTTP requests.
## Requirements
### Requirement: One classpath metadata source
PluginMetadata SHALL load immutable validated displayName/version from `/com/questcape/plugin.properties` once, off ClientThread/EDT. This sole runtime metadata source SHALL be checked in under `src/main/resources` and exist exactly once on the development source-resource classpath, the ordinary distribution JAR and a standard Plugin Hub build, without working-directory assumptions, custom root-file copying or duplicate packaging. Build verification SHALL require its displayName/version to match the root `runelite-plugin.properties` Hub descriptor and its version to match Gradle. Metadata/resource loading SHALL NOT require a blocking lifecycle future, extra runtime dependency or reflection.

#### Scenario: Development and isolated JAR execution
- **WHEN** the metadata reader runs on the development classpath and an isolated packaged-JAR classpath
- **THEN** both read the same values without requiring the source checkout and each classpath contains exactly one intended properties resource

#### Scenario: Concurrent requests
- **WHEN** multiple HTTP operations need product metadata
- **THEN** the validated value is loaded once and safely shared without per-request resource IO

#### Scenario: Standard build has no copied root descriptor
- **WHEN** Java source/resources are packaged using RuneLite's standard build template without the repository's custom Gradle tasks
- **THEN** the plugin-owned runtime resource is present and constructs the validated versioned User-Agent without requiring `/runelite-plugin.properties`

#### Scenario: Release identity drifts
- **WHEN** runtime displayName/version differs from the Hub descriptor or runtime version differs from Gradle
- **THEN** automated verification fails before release

### Requirement: Header-safe versioned product identity
BoundedHttp SHALL derive its User-Agent product identity from validated packaged displayName/version with named HTTP policy/suffix values. It SHALL reject missing required fields or invalid/control/header-injection values, derive a valid product token when a display name contains spaces, and SHALL NOT keep a second hardcoded plugin name/version. Invalid metadata SHALL produce a controlled typed transport limitation while available local browsing/progress remains usable. No account identity, path or credential SHALL enter the header.

#### Scenario: Version changes
- **WHEN** a test metadata resource changes version or a valid display name
- **THEN** the constructed request header changes accordingly without a live-server request or transport source edit

#### Scenario: Malformed or absent metadata
- **WHEN** required fields are absent or contain CR/LF/control values
- **THEN** transport reports a controlled metadata failure without sending an injected header, fabricating a fallback version or disabling usable local data
