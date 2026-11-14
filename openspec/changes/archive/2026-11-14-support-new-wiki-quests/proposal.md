## Why

The OSRS Wiki can add new quests to its Optimal Quest Guide ahead of updates to RuneLite's Java quest enum. QuestCape showed A Shell of a Friend and The Birds and the Wasps as unknown and could not mark them complete.

## What Changes

- Recognize new full quests using consistent evidence in the Wiki route.
- Read completion from the local game quest database when an enum entry is unavailable.
- Preserve saved progress as quests gain enum entries and show unavailable progress when local evidence is missing.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `guide-domain-model` recognizes newly released full quests and represents their normalized identities.
- `guide-progress-tracking` reads completion from supported local game data for quests absent from the enum.

## Impact

The change extends parsing, local quest capture and existing cache decoding. It uses the existing guide request, RuneLite APIs and local progress store.
