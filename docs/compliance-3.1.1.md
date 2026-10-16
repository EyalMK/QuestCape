# QuestCape 3.1.1 post-acceptance review

On 2026-10-16 the user confirmed **"I confirm the 3 checks pass successfully"**
for implementation `5b1416c05cf6845a8b1e803eb40f7911613eaa20`, production tree
`27794bf60c901857ae98e0b6f3cded267bb3e09b`. Acceptance covers the final three
training-link/restart/copy-refresh/progress checks in
[the verification record](verification-3.1.1.md). No machine versions were supplied.

After acceptance, re-read repository AGENTS.md and the current
[Plugin Hub guidance](https://github.com/runelite/plugin-hub),
[RuneLite restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
and [Jagex client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1).
The scope includes all 101 production Java files, build/descriptor/resources,
licenses and the ordinary/standard-template JAR evidence. Compared with the
previously audited 3.1.0 production tree, the only Java change is the metadata
resource path; one two-field runtime properties file is added. Whole-tree searches
were followed by review of network, persistence, client capture, own-sidebar
publication and lifecycle call paths. No new prohibited API or feature was found.

| Boundary | Finding |
| --- | --- |
| Java and packaging | Java 11 target and packaged major version 55 retained. No production reflection, native/process APIs, dynamic code, Java serialization or new runtime dependency. Tests/artifacts are excluded from the ordinary JAR. Source-resident metadata, icons and license/notices are present in standard packaging. |
| HTTP and identity | Injected OkHttp-derived client; timeouts, bounded bodies, explicit allowlists/redirects and cancellation retained. Metadata remains lazy and validated on HTTP workers. Fixed Wiki and user-triggered Theoatrix requests contain no character identity/progress. No paid service. |
| Client and input | Observations are current-character quest/real-skill/gameval completion facts on ClientThread. No game input, chat/server actions, scene scans, other-player collection, combat/PvP prediction or game-widget manipulation. |
| UI and cleanup | Own sidebar attachment only; EDT publication, native LinkBrowser.browse and explicit local clipboard gestures retained. Popup/timer disposal and navigation removal remain owned by the originating run. No other-plugin/core-window manipulation. |
| Persistence and concurrency | Injected Gson and RuneLite plugin Filepath; bounded schema-checked JSON, hashed names and atomic replacement retained. Worker IO, account/generation checks and shutdownNow without blocking remain. No sleeps, latches or unowned periodic work. |
| Release identity | Build, root Hub descriptor, runtime metadata and current documentation agree on patch 3.1.1. No config/data/public API migration is needed. The unchanged Hub warning is preserved. |

No functional correction was required after acceptance. Existing evidence remains
124 passing tests in 28 suites on Temurin 11.0.22 / Gradle 8.10 / cached RuneLite
1.13.2, plus the independent official-standard-template build and actual default
loader probe for both JARs. The accepted ordinary artifact still hashes to
`2D31CFE2478E9B596E3CB215A19458731A17F55E64290B6635B7D058B30B0320`.
This source review does not replace remote Hub checks or maintainer review.

The user's current request authorizes archival, the source PR targeting master,
then a Hub PR using master's newly updated full commit. The verified Hub labels
are `plugin change` and `size-s`. The upstream manifest currently points to
`8f7566fa15da8f510de68dfa54633dbc96cbdc39` and retains the third-party IP warning;
re-read both manifest and master at delivery time. The Hub PR remains open for
RuneLite maintainers. Historical 3.1.0 no-merge instructions describe that release.
