## Task 1.1: Quest Helper integration prerequisite

Status: quest-specific launch/observation remains unverified; RuneLite plugin discovery is documented. Task remains unchecked. Checked 2026-09-12.

### Verified evidence

- The [Plugin Hub manifest](https://github.com/runelite/plugin-hub/blob/master/plugins/quest-helper) selects Quest Helper commit `94acc617e99fe8029ea62f9b8bceaf1ece0a75ea`.
- At that commit, [plugin metadata](https://github.com/Zoinkwiz/quest-helper/blob/94acc617e99fe8029ea62f9b8bceaf1ece0a75ea/runelite-plugin.properties) declares version `4.17.0` and build type `standard`.
- The [published entry-point source](https://github.com/Zoinkwiz/quest-helper/blob/94acc617e99fe8029ea62f9b8bceaf1ece0a75ea/src/main/java/com/questhelper/QuestHelperPlugin.java) contains no `PluginMessage` receiver. It exposes panel display and internal quest-management paths, but these observations do not prove a supported cross-plugin launch/confirmation contract.
- GitHub's API for [PR #2756](https://github.com/Zoinkwiz/quest-helper/pull/2756) reports `state: open`, `merged: false`, and `updated_at: 2026-07-21T22:28:30Z`. Its proposed inbound launch contract is not established as shipped by this PR.
- No ordinary quest or branch/subquest launch has been demonstrated in a RuneLite development client. No target RuneLite runtime version has yet been pinned or tested.

### RuneLite API analysis

The [Developer Guide](https://github.com/runelite/runelite/wiki/Developer-Guide) links both the game and client Javadocs. The requested [game API index](https://static.runelite.net/runelite-api/apidocs/) identifies 1.12.38 in the inspected response; individual cached pages can show different versions, so implementation must record its resolved dependencies.

- Game API: `Quest.getState(Client)` and `Client.getRealSkillLevel(Skill)` supply local progress. They do not open Quest Helper walkthroughs.
- Client API: [PluginManager](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/plugins/PluginManager.html) enumerates loaded instances and distinguishes enabled from active plugins. Its start method controls plugin lifecycle, not the selected quest.
- [Plugin.getInjector()](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/plugins/Plugin.html) is public. This permits access to the plugin's injector object, but does not by itself define typed quest operations or resolve sibling class visibility.
- Quest Helper's published [QuestMenuHandler](https://github.com/Zoinkwiz/quest-helper/blob/94acc617e99fe8029ea62f9b8bceaf1ece0a75ea/src/main/java/com/questhelper/managers/QuestMenuHandler.java) has public `startUpQuest(String)`, and [QuestManager](https://github.com/Zoinkwiz/quest-helper/blob/94acc617e99fe8029ea62f9b8bceaf1ece0a75ea/src/main/java/com/questhelper/managers/QuestManager.java) has a Lombok getter for the selected quest. These are plugin-owned interfaces, not RuneLite interfaces.
- RuneLite's [external loader](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/externalplugins/ExternalPluginManager.java) creates separate [PluginHubClassLoader](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/externalplugins/PluginHubClassLoader.java) instances for Hub JARs. This rules out assuming that a successful shared-classpath development import demonstrates distribution compatibility.
- [PluginMessage](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/events/PluginMessage.html) is documented for communication between Hub plugins. A receiver and an activation-observation contract still need to be supplied by Quest Helper; the event mechanism alone cannot invent them.

### Consequence

Task 1.1 requires a supported launch, panel activation, and confirmation mechanism against the distributed plugin. That evidence is not available, so neither this task nor dependent handoff/resume acceptance can be checked off. This is not a claim that all possible integration approaches are impossible.

The earlier apply run paused under its blocker instruction. This follow-up analyzes APIs and updates design/tasks only; no application code or successful runtime integration is claimed. WikiSync provider selection and the settings-page resume toggle remain exactly as requested.

### Proposed continuation

The revised plan uses RuneLitePluginRegistry for prerequisite discovery and a separate QuestHelperBridge for quest operations. Follow the external-plugin development path described by the Developer Guide and [Plugin Hub](https://github.com/runelite/plugin-hub). The task 1.1 verification gate applies to the handoff adapter and its live acceptance, while scaffold, guide, WikiSync, RuneLite progress/settings, and Theoatrix work are independent. A custom Quest Helper build or reflective compatibility workaround has not been selected by this analysis.
