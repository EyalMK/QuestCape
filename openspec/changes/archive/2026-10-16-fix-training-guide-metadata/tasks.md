## 1. Metadata and release fix

- [x] 1.1 Add a regression test reproducing metadata failure with only standard source resources and a user-initiated Theoatrix request.
- [x] 1.2 Add the plugin-owned runtime metadata resource, update the loader and remove the custom root-descriptor copy while preserving validation/lazy worker loading.
- [x] 1.3 Set patch version 3.1.1 consistently in Gradle, the Hub descriptor, runtime metadata and current developer documentation.
- [x] 1.4 Verify descriptor/runtime identity equality, source-resource uniqueness and isolated ordinary-JAR packaging.

## 2. Automated verification and user acceptance

- [x] 2.1 Run the affected regression tests, then the Java 11 build and existing full suite; validate this OpenSpec change and the diff.
- [x] 2.2 Build with the official standard template without custom repository tasks, inspect the resulting runtime metadata, and record reproducible evidence and limitations.
- [x] 2.3 Offer to launch the development client and provide the Jagex-account instructions and focused in-game checklist.
- [x] 2.4 Receive explicit user confirmation for training links after startup/restart, another/compound skill action and retained copy/progress behavior.
- [x] 2.5 After acceptance, perform and record the required current whole-plugin rules audit; any functional changes require renewed affected acceptance.

Delivery follows implementation acceptance and archival in the order recorded in design.md; delivery actions are tracked in the current release verification record so archiving does not require its own future publication tasks to be complete.
