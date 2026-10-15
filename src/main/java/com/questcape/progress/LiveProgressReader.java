package com.questcape.progress;

import java.util.*;
import javax.inject.*;
import net.runelite.api.*;
import net.runelite.client.config.*;

@Singleton
public class LiveProgressReader
{
	private final Client client;
	private final ConfigManager config;

	@Inject
	public LiveProgressReader(Client client, ConfigManager config)
	{
		this.client = client;
		this.config = config;
	}

	/** Only call on ClientThread after the login's first game tick. */
	public AccountProgress read()
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null
			|| config.getRSProfileKey() == null)
		{
			return null;
		}
		String name = client.getLocalPlayer().getName();
		if (name == null || name.isBlank() || client.getAccountHash() == -1L)
		{
			return null;
		}
		RuneScapeProfileType mode = RuneScapeProfileType.getCurrent(client);
		boolean established = config.getRSProfiles().stream().anyMatch(p -> p.getKey().equals(config.getRSProfileKey())
			&& p.getAccountHash() == client.getAccountHash() && p.getType() == mode);
		if (!established)
		{
			return null;
		}
		Map<String, AccountProgress.QuestStatus> quests = new HashMap<>();
		for (Quest quest : Quest.values())
		{
			QuestState state = quest.getState(client);
			quests.put(quest.name(), state == QuestState.FINISHED ? AccountProgress.QuestStatus.COMPLETE
				: state == QuestState.IN_PROGRESS ? AccountProgress.QuestStatus.IN_PROGRESS
					: AccountProgress.QuestStatus.INCOMPLETE);
		}
		Map<String, Integer> levels = new HashMap<>();
		for (Skill skill : Skill.values())
		{
			int level = client.getRealSkillLevel(skill);
			if (level > 0)
			{
				levels.put(skill.name(), level);
			}
		}
		com.questcape.progress.activity.ActivityObservation extra = com.questcape.progress.activity.ActivityRegistry.STANDARD
			.capture(client);
		extra.getQuests()
			.forEach((key, value) -> quests.put(com.questcape.persistence.LegacyKeyCodec.encode(key), value));
		Map<String, Boolean> facts = new HashMap<>();
		extra.getFacts()
			.forEach((key, value) -> facts.put(com.questcape.persistence.LegacyKeyCodec.encode(key), value));
		long now = System.currentTimeMillis();
		return new AccountProgress("live:" + config.getRSProfileKey() + ":" + mode.name(), name, mode.name(),
			"Live RuneLite",
			now, now, quests, levels, facts, Collections.emptySet());
	}
}
