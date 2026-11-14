## Context

QuestCape receives the ordered route from the OSRS Wiki and reads character progress locally. The two November quests are present in Wiki revision 15401116 and the live game database. RuneLite 1.13.6 does not expose them in its `Quest` enum.

## Goals

- Display and track supported full quests even during the gap between game and enum updates.
- Preserve existing action classification, stage semantics and saved progress.

## Decisions

- Use the existing route response. A full quest requires a consistent action title, canonical article link, matching quick-guide link in its own column and positive individual quest points. Cached rows retain those inputs and are reclassified on load.
- Resolve normalized quest titles against a unique local game database row with valid progress thresholds. Read its state through `ScriptID.QUEST_STATUS_GET` on the client thread. The lookup index holds game definitions and each capture reads the current character's state.
- Save fallback observations by normalized game title. The progress codec resolves an observation to the enum identity when RuneLite adds it and gives precedence to an enum-keyed observation if both exist.

## Risks and Trade-offs

Quest rows with less structured markup or conflicting titles may remain unrecognized. A classified quest without usable local evidence shows Progress unavailable. Both rules avoid crediting a different action.
