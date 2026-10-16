## ADDED Requirements

### Requirement: One classpath metadata source
PluginMetadata SHALL load immutable validated displayName/version from /runelite-plugin.properties once, off ClientThread/EDT. The same resource SHALL exist once on the development source-set classpath and once in the ordinary distribution JAR, without working-directory assumptions or duplicate packaging. Metadata/resource loading SHALL NOT require a blocking lifecycle future, extra runtime dependency or reflection.

#### Scenario: Development and isolated JAR execution
- **WHEN** the metadata reader runs on the development classpath and an isolated packaged-JAR classpath
- **THEN** both read the same values without requiring the source checkout and each classpath contains exactly one intended properties resource

#### Scenario: Concurrent requests
- **WHEN** multiple HTTP operations need product metadata
- **THEN** the validated value is loaded once and safely shared without per-request resource IO

### Requirement: Header-safe versioned product identity
BoundedHttp SHALL derive its User-Agent product identity from validated packaged displayName/version with named HTTP policy/suffix values. It SHALL reject missing required fields or invalid/control/header-injection values, derive a valid product token when a display name contains spaces, and SHALL NOT keep a second hardcoded plugin name/version. Invalid metadata SHALL produce a controlled typed transport limitation while available local browsing/progress remains usable. No account identity, path or credential SHALL enter the header.

#### Scenario: Version changes
- **WHEN** a test metadata resource changes version or a valid display name
- **THEN** the constructed request header changes accordingly without a live-server request or transport source edit

#### Scenario: Malformed or absent metadata
- **WHEN** required fields are absent or contain CR/LF/control values
- **THEN** transport reports a controlled metadata failure without sending an injected header, fabricating a fallback version or disabling usable local data
