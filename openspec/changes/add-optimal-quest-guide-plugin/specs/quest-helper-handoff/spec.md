## ADDED Requirements

### Requirement: Quest click starts the matching helper
The plugin SHALL launch the matching supported quest or subquest in the installed Quest Helper through a verified supported integration when its primary quest action is activated while logged in. It SHALL expose the helper panel, respect Quest Helper's required branch selection, and confirm activation before recording launch success.

#### Scenario: Supported quest selected
- **WHEN** the player clicks a supported quest and compatible Quest Helper is available
- **THEN** that quest opens in Quest Helper and the guide selection is retained for return

#### Scenario: Multi-part quest
- **WHEN** the player selects a mapped subquest or branch-dependent quest
- **THEN** the integration opens the correct subquest or invokes Quest Helper's appropriate branch-selection flow without silently choosing a different quest

#### Scenario: Unconfirmed launch
- **WHEN** a launch request is sent but actual activation cannot be confirmed
- **THEN** the plugin reports pending or failed activation and does not record a successful resume target

### Requirement: Integration availability is explicit
The plugin SHALL keep the guide usable when Quest Helper is absent, disabled, incompatible, or lacks the selected helper. It SHALL provide an actionable explanation and SHALL NOT report unsupported message delivery or manual opening as successful one-click integration.

#### Scenario: Missing dependency
- **WHEN** a player selects a quest without compatible Quest Helper enabled
- **THEN** the panel identifies the installation, enablement, or compatibility issue and leaves progress unchanged

#### Scenario: Logged out selection
- **WHEN** a player selects a quest while logged out
- **THEN** the plugin explains that login is required and does not create a confirmed resume target

#### Scenario: Upstream hook remains unshipped
- **WHEN** no supported launch contract works against the distributed Quest Helper build
- **THEN** launch and resume acceptance remain incomplete even if a development stub or manual fallback works

### Requirement: Quest Helper search fallback
When no supported quest-specific launch receiver is available, a logged-in user's click on the quest's Open button SHALL open the existing Quest Helper sidebar tab and populate its search field with the matching ordinary quest or subquest name. This user-requested UI fallback SHALL use the existing RuneLite/Swing UI and preserve the helper's configuration. Exactly one visible result SHALL be opened through its own enabled arrow; zero or multiple results SHALL be left for the user. Search readiness and result selection SHALL NOT create a confirmed resume intent.

#### Scenario: Open and search
- **WHEN** a logged-in player clicks a quest's Open button with Quest Helper active and no supported launch receiver
- **THEN** RuneLite activates the existing Quest Helper tab, its search field contains the quest's matching display name, and the fallback opens a single result or leaves multiple matches for manual choice

#### Scenario: Single result opens through its arrow
- **WHEN** the search displays exactly one quest result with an enabled, uniquely identified arrow
- **THEN** the fallback presses that arrow once and leaves any assist or branch setup to Quest Helper's own flow

#### Scenario: No result or multiple results
- **WHEN** there are zero or multiple visible quest results, including a disabled result among multiple matches
- **THEN** the fallback does not press any result arrow

#### Scenario: Assist or settings view hides search
- **WHEN** Quest Helper's verified assist/settings view hides its search field
- **THEN** the fallback uses the existing view toggle to reveal the quest list before filling the search field

#### Scenario: Changed or missing search UI
- **WHEN** the expected panel or search field cannot be uniquely identified
- **THEN** the fallback leaves other inputs untouched and reports an actionable message above the selected guide card

#### Scenario: Search is not launch confirmation
- **WHEN** the fallback populates search or presses a single result's arrow
- **THEN** the plugin reports search readiness or result selection, does not claim confirmed helper activation, and does not save or automatically replay a resume target

### Requirement: Default automatic login resume
The plugin SHALL enable Resume quest on login by default and persist the last confirmed unfinished quest launched through this plugin for the actual logged-in account. After that same account logs back in, it SHALL wait for identity, quest state, and helper readiness, then reopen the remembered quest once and allow Quest Helper to determine the current step.

#### Scenario: Logout mid-quest
- **WHEN** an account logs out during a confirmed quest and logs back in with that quest unfinished
- **THEN** the quest automatically reopens at the game-reported current progress without another guide click

#### Scenario: Full client restart
- **WHEN** the client restarts before the same account logs back in
- **THEN** its persisted unfinished quest remains eligible for automatic resume

#### Scenario: Different account login
- **WHEN** another account logs in or the panel has looked up another username
- **THEN** the previous account's resume target is not launched for that other account

### Requirement: Resume respects current state and explicit intent
The plugin SHALL provide a persistent, default-enabled Resume quest on login on/off option in this plugin's RuneLite settings page, accessible without opening a quest row, and a separate Clear remembered quest action. Disabling the option SHALL cancel pending automatic resume attempts and prevent future automatic launches without stopping the active helper or preventing manual quest clicks. Enabling the option SHALL make the remembered unfinished quest eligible on the next login without immediately launching it. The plugin SHALL suppress resume for completed quests, disabled resume, cleared intent, or a conflicting active helper; avoid duplicate launches during repeated login/hop events; and bound readiness retries per session.

#### Scenario: Completed or cleared target
- **WHEN** a saved quest is now finished or its intent was explicitly cleared
- **THEN** it does not reopen at login

#### Scenario: Resume disabled
- **WHEN** the player disables Resume quest on login in the plugin settings before logging back in
- **THEN** no automatic helper launch occurs

#### Scenario: Settings persist across restart
- **WHEN** the player turns resume off in the plugin settings and restarts RuneLite
- **THEN** the option remains off and the remembered quest does not automatically reopen

#### Scenario: Resume enabled again
- **WHEN** the player turns resume on in the plugin settings
- **THEN** the preference is saved and the remembered unfinished quest becomes eligible at the next login without an immediate launch

#### Scenario: Disable while waiting for readiness
- **WHEN** the player turns resume off while an automatic launch is waiting for account or helper readiness
- **THEN** the pending launch is canceled and no helper is opened automatically

#### Scenario: Manual launch with resume disabled
- **WHEN** the player clicks a supported quest while Resume quest on login is off
- **THEN** the normal manual Quest Helper launch still works and the resume preference stays off

#### Scenario: Duplicate readiness events
- **WHEN** repeated state events or a world hop occur with the same helper already active
- **THEN** the plugin does not restart the helper or repeatedly take panel focus

#### Scenario: Different helper already active
- **WHEN** automatic resume becomes ready while another helper is active
- **THEN** the active helper is left in place and automatic resume is suppressed for that session

#### Scenario: Dependency stays unavailable
- **WHEN** Quest Helper remains unavailable throughout the bounded readiness window
- **THEN** the plugin retains the remembered quest, reports the problem once, and stops retrying for that session
