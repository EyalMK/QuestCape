# QuestCape 3.1.0 sidebar candidate

> Historical evidence. The current combined 3.2.0 candidate and its pending
> acceptance/audit are recorded in [verification-3.2.0.md](verification-3.2.0.md).
> Historical passes and screenshots do not validate the current candidate.

Status: **awaiting user acceptance and the subsequent final rules audit**. The
implementation is local on `codex/questcape-sidebar-ux`; the user explicitly
instructed that this branch must not be merged into master. No push, PR, Hub
update, visibility change or archive is authorized by this implementation request.

## Candidate and contract

Master baseline: `40eb54a520970db9edb77720d254d5b0f1b2f207`.
Proposal commit: `547ec67`. The exact tested implementation revision and artifact
fingerprint are recorded below.

The implementation follows `refine-questcape-sidebar-ux` and the approved revision
3 research/mockup. Research input SHA-256:
`1760F0B50E8C7AF7E6AF640D21CB6786D70381F35363F703F3A0FF4FA497EF9F`.
Approved full-client mockup SHA-256:
`D6EB2A6C41B9085CF4179115F39079938F80BB4FA919A0AE96740A4FE7CBAFA3`.
Private research captures remain ignored; no live account screenshots are shipped
or published as README art.

`3.1.0` is a compatible minor increment from `3.0.1`: new sidebar features and
training range progress preserve goal completion, schema-1 legacy fields, stable
row identities, account/config keys and the existing manual eligibility policy.
Build properties and packaged metadata agree. Optional versioned row evidence is
additive; unsupported enrichment falls back to retained raw source content.

Two implementation clarifications are recorded in the design: the primary Wiki
quick guide remains inside expanded details, immediately before route advice;
single clicks on selectable text wait the native double-click interval so word
selection can cancel disclosure. Background clicks and Enter/Space on the focused
card are immediate. Ordinary live updates do not schedule viewport navigation;
structural replacement and explicit disclosure/search restoration retain anchors.

## Automated and visual evidence

Environment: Windows, Temurin 11.0.22, Gradle 8.10, cached RuneLite client/API
1.13.1, Java `--release 11`. Offline dependency resolution does not establish the
newest upstream version. The existing JDK 17 failed to establish the Gradle
daemon's loopback connection in this environment; the Java 11 build succeeded.

The unchanged baseline passed **72 tests** in a separate extracted checkout.
The candidate suite passed **92 tests**, zero failures/errors, across 17 suites.
Checks include:

- Training boundaries, floored/clamped level percentages, target-only and invalid
  intervals, compound precedence, unsupported requirements, missing observations
  and combat derived from real levels; one evaluation feeds cards and completion.
- Fresh/legacy field roles, full advice and raw fallback, safe query/fragment
  retention, ambiguous/N/A quick guides, additive serialization and legacy readers.
- Clear beside borderless match navigation, unchanged filter menus/artwork,
  debounce and wrap, query-origin restoration, clamped Continue, expansion keys,
  selectable text and independent nested actions.
- Original-run startup and retained-widget callbacks after restart; account/logout
  isolation; immutable content/save tuples; delayed save, unsaved live facts,
  failed refresh with last-good route, cancellation and packaged resources.

Commands from the plugin root with the Java 11 `JAVA_HOME`:

```powershell
./gradlew.bat --no-daemon --offline clean build resolvedVersions --console=plain
./gradlew.bat --no-daemon --offline test verifyRemoteContracts --console=plain
openspec.cmd validate refine-questcape-sidebar-ux --strict
git diff --check
```

The public HTTP contract check passed for Wiki revision `15358869`, 351 rows.
It accessed only public Wiki content and used the ignored test-cache directory;
it did not start RuneLite or submit player information.

Ordinary JAR verification checks Java 11 bytecode, metadata, classpath icons and
notices, and exclusion of test/development artifacts. No new runtime dependency
was added. GitHub artwork is Primer's MIT-licensed mark, with root and packaged
notices; skill images come from RuneLite's local managers. Source inspection found
no production reflection, process execution, sleeps, blocking lifecycle waits or
injected game input. Client observations stay on ClientThread, IO on workers, UI
updates on EDT; pending callbacks and disclosure/search timers are cancelled or
fenced on shutdown. This is an implementation review, not the post-acceptance
audit required by the release policy.

Isolated native RuneLiteLAF previews are reproducible through
`NativeSidebarPreviewTest` in `build/ui-evidence/native-sidebar-3.1.0/`:

- 240×1364, 225×640 and 320×900: actual Swing layout, native font metrics and real
  local skill/header images; all nonempty wrapped text is checked through its
  final document character, with the full-width footer asserted.
- `sidebar-compound-notices-placeholders.png`: long recommendation, compound
  comparison with missing observation, multiple links, unavailable skill images
  and multiline recovery notices.
- Device transforms 1.25 and 1.5: raster/paint evidence only. Actual Windows display
  scaling, keyboard focus traversal, native tooltip screen clipping and real
  client integration still require user review.

These images use source fixtures and synthetic accounts, **not game captures**.
Synthetic Swing events never target RuneScape. Linux was unavailable; the
previous Ubuntu whole-system freeze remains a separate unresolved investigation.

## User acceptance checklist

Offer to launch the development client with `./gradlew run` from the plugin root
(`./gradlew.bat run` on Windows, using the Java 11 environment above). Follow
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
to log in. Only the user operates RuneScape; launching successfully is insufficient.

1. Check the logo/title gap, quiet GitHub border and small floating tooltip below
   it: **Report issues or contribute**. The button should open this repository's
   issues page. Wiki refresh must remain a separate circular-arrow source action.
   **Resync Progress** should read current local observations, use opposing arrows
   and disable until ready. Source/progress details should expose character/mode
   and independent freshness without a routine success banner.
2. Enter matching and unmatched queries. Match count/down chevron and clear X
   should coexist; no-match feedback belongs below the field, independently of
   shown-step counts. Clear should retain both filters, cancel queued search/wrap
   and restore search origin. Reset should clear query and filters. Test Enter,
   successive matches, double-Enter wrap and narrow layout.
3. Expand completed **The Tourist Trap**, not-started **Lunar Diplomacy**, and an
   available in-progress activity/quest. Keep full advice, guide projected levels,
   QP, location, source/unknown content and all relevant links. Wiki quick guide
   should be primary in expanded details; Quest page should be under Related wiki
   pages. Check actual skill icons, fonts, wrapping and provider labels.
4. Click card background, title and advice; test collapse/expand using the focused
   card's Enter/Space. Double-click words and drag-select text without disclosure.
   Wiki/Theoatrix/manual/details controls should perform only their own action.
   Check keyboard focus and visible focus borders, especially the small icons.
5. Inspect available real training observations: below base is Incomplete;
   base≤current<goal is In progress; current≥goal is Complete. A 54 level in a
   52→55 interval displays **Current level: 54 - 66%**. Target-only/missing levels
   omit percentage; compound rows show separate skill comparisons. Boosts and
   guide projections must not affect completion. Unavailable boundary states have
   synthetic coverage; do not gain levels or alter an account solely for testing.
6. Check circular top/bottom direction and full-width Continue, including filtered
   earliest unfinished rows, already aligned/clamped targets and disabled readiness.
   Scroll and expand while live progress updates; no unsolicited jumps/collapse
   should occur. Log out/in or switch an available profile normally, checking no
   old account facts, messages or pending navigation return. Actual native scaling
   and shorter client windows should remain usable.

Controlled refresh/save failures and stale callbacks have synthetic/fault-seam
coverage; do not damage local caches or force irreversible game states to recreate
them. Report visual/behavioral problems and the tested revision. Explicit user
confirmation remains pending. After acceptance, re-read the then-current official
RuneLite/Jagex rules and audit the exact candidate before considering archive or
delivery; any functional correction requires affected acceptance again.

## Exact tested candidate

Production/test commit: `7398b6351cbf7ca631b8bf01d2aa266cd3460abd`.
The final clean build on 2026-10-14 passed 92 tests across 17 suites, with zero
failures/errors. `resolvedVersions` confirmed client/API 1.13.1 and the existing
classpath. The ordinary distribution JAR is `build/libs/questcape-3.1.0.jar`;
SHA-256: `4D5481F7FEC62CB90176F430F87934948B1416ED0C53F8F52ED8D10E45B4B9E8`.

Final build log: ignored `.tmp/sidebar-tested-7398b63.log`. The successful public
contract check is in `.tmp/sidebar-contract-build.log`; the later clean build did
not repeat that unchanged HTTP adapter. Test reports and all six preview PNGs are
under ignored `build/`. Preview main-image SHA-256:
`BC6762E775AF2FE012D013D76EE78DA83AA8188B61C3BD783ECAA8AC545CEEC6`.
Evidence-only commits following this production commit do not change the tested
source, resources, build or metadata. Acceptance is requested for this candidate;
tasks 9.4 and 9.5 remain pending.
