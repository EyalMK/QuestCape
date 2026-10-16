package com.questcape.progress.activity;

import com.questcape.progress.activity.definitions.ActivityKey;
import com.questcape.progress.model.QuestKey;
import java.util.*;
import net.runelite.api.Client;

/** Invoked only at the established ClientThread capture boundary. */
public interface ActivityDetector
{
	default Set<ActivityKey> facts()
	{
		return Collections.emptySet();
	}

	default Set<QuestKey> quests()
	{
		return Collections.emptySet();
	}

	ActivityObservation capture(Client client);
}
