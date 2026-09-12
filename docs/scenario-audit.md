# Capability scenario audit — 2026-09-12

Implemented means code plus the listed local evidence; it does not claim in-game acceptance. Pending Quest Helper scenarios require a supported distributed launch/observation contract. The user confirmed in-game Sync acceptance on 2026-09-12 (task 8.2).

## guide-progress-tracking

| Scenario | Evidence / status |
| --- | --- |
| Login sync | Implemented in plugin events/services; public HTTP and client/registry tests pass. User accepted the in-game Sync check on 2026-09-12. |
| Player name contains a space | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Equivalent underscore spelling | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Explicit refresh | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Logged out | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Unavailable or partial profile | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Newest current-account request wins | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| WikiSync missing or disabled | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Recovery after enablement | Implemented in plugin events/services; public HTTP and client/registry tests pass. User accepted the in-game Sync check on 2026-09-12. |
| Quest completes during play | Implemented in plugin events/services; public HTTP and client/registry tests pass. User accepted the in-game Sync check on 2026-09-12. |
| Non-standard profile | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Boosted training level | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Repeated skill milestones | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Reward hand-in | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Unmapped quest | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Reverse manual check | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Reorder and restart | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Changed target or action | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Account or mode switch | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |
| Partial knowledge | Implemented: ProgressTest, JsonStoreTest, IntegrationTest; explicit current-player flow and generation checks. |

## optimal-guide-content

| Scenario | Evidence / status |
| --- | --- |
| A quest begins and finishes at different stages | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Mixed table content | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Unrecognized activity | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Wiki revision changes | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Invalid or truncated response | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| First launch while offline | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Duplicate refreshes | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Position-aware navigation | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Login and repeated live updates preserve scroll | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Inspect guide detail | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Larger text and progress indicator | GuidePanelTest and updated narrow previews: 13–18 pt typography, 22-pixel progress bar and readable percentage. |
| Whole-card detail action | GuidePanelTest: title/background/metadata click and keyboard toggle; Open and source links preserve their own actions. |
| Dedicated Quest Helper action | GuidePanelTest: Open beside step number/type, independent of details, fits with a 16-pixel icon. QuestHelperSearchTest: reuse current registered icon and clear after removal without selecting the tab. |
| Quest feedback follows the selected card | GuidePanelTest and sidebar-quest-message.png: feedback directly above the selected card. |
| Completion without color recognition | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Cached source provenance | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |
| Unexpected active markup | Implemented: GuideParserTest, GuideRepositoryTest, GuidePanelTest; captured revision and docs/ui images. |

## quest-helper-handoff

| Scenario | Evidence / status |
| --- | --- |
| Supported quest selected | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Multi-part quest | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Unconfirmed launch | Fail-closed behavior implemented: IntegrationTest and GuidePanelTest. No successful handoff claimed. |
| Missing dependency | Fail-closed behavior implemented: IntegrationTest and GuidePanelTest. No successful handoff claimed. |
| Logged out selection | Fail-closed behavior implemented: IntegrationTest and GuidePanelTest. No successful handoff claimed. |
| Upstream hook remains unshipped | Fail-closed behavior implemented: IntegrationTest and GuidePanelTest. No successful handoff claimed. |
| Open and search | QuestHelperSearchTest: existing tab selection and native search document listener; pinned panel source verified. The user supplied in-game Sheep Shearer search evidence. |
| Assist or settings view hides search | QuestHelperSearchTest: use existing view toggle before setting search text. |
| Changed or missing search UI | QuestHelperSearchTest: ambiguous, hidden, removed and detached UI leave unrelated inputs untouched. |
| Search is not launch confirmation | IntegrationTest: neither SEARCH_READY nor RESULT_SELECTED can write confirmed resume intent. |
| Single result opens through its arrow | QuestHelperSearchTest: click the sole visible result's native arrow once after filtering; preserve setup and search clearing. |
| No result or multiple results | QuestHelperSearchTest: zero/multiple matches, including a disabled second match, do not activate a result. |
| Logout mid-quest | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Full client restart | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Different account login | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Completed or cleared target | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Resume disabled | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Settings persist across restart | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Resume enabled again | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Disable while waiting for readiness | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Manual launch with resume disabled | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Duplicate readiness events | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Different helper already active | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |
| Dependency stays unavailable | PENDING: distributed Quest Helper launch/observation gate. Versioned intent, settings and resume coordination have local ResumeCoordinatorTest/PluginLifecycleTest coverage; distributed handoff/resume remains unverified. |

## training-guide-links

| Scenario | Evidence / status |
| --- | --- |
| Known skill training | Implemented: GuideParserTest and IntegrationTest; 21 verified destination pages in theoatrix-verification.json. Native OS recovery delegates to RuneLite LinkBrowser. |
| Several skills in one step | Implemented: GuideParserTest and IntegrationTest; 21 verified destination pages in theoatrix-verification.json. Native OS recovery delegates to RuneLite LinkBrowser. |
| Unmapped skill | Implemented: GuideParserTest and IntegrationTest; 21 verified destination pages in theoatrix-verification.json. Native OS recovery delegates to RuneLite LinkBrowser. |
| Browser invocation fails | Implemented: GuideParserTest and IntegrationTest; 21 verified destination pages in theoatrix-verification.json. Native OS recovery delegates to RuneLite LinkBrowser. |
| Unsafe destination | Implemented: GuideParserTest and IntegrationTest; 21 verified destination pages in theoatrix-verification.json. Native OS recovery delegates to RuneLite LinkBrowser. |
| Guide opened before training | Implemented: GuideParserTest and IntegrationTest; 21 verified destination pages in theoatrix-verification.json. Native OS recovery delegates to RuneLite LinkBrowser. |

## Remaining task gates

- 1.1 and 6.1/6.6: no supported distributed Quest Helper launch, panel exposure, and selected-helper observation contract demonstrated under separate Hub loaders.
- 6.3: default-on settings, cancellation and account/profile/mode-scoped versioned confirmation storage are implemented and locally tested; real confirmed intent awaits a working distributed bridge.
- 6.4: coordinator implemented with a 50-game-tick budget, completion/account checks, duplicate/hop suppression and active-helper precedence. Real resume acceptance still requires the launch/observation contract.
- 6.5: ResumeCoordinatorTest and PluginLifecycleTest cover local cancellation, default/off/on behavior, settings discovery, store reconstruction, confirmation timeout, stale-account/mode work, conflict and dependency handling. Successful resume through the distributed bridge and actual client-restart acceptance remain pending.
- 8.2 complete: real endpoint, cache and progress regression evidence plus the user response "Yes. Sync works." to the requested logged-in acceptance check.
