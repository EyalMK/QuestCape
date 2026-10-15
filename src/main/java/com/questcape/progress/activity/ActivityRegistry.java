package com.questcape.progress.activity;

import com.questcape.progress.*;
import java.util.*;
import net.runelite.api.Client;

/** Explicit composition and duplicate validation, never discovery or last-writer-wins registration. */
@lombok.extern.slf4j.Slf4j
public final class ActivityRegistry
{
	public static final ActivityRegistry STANDARD = new ActivityRegistry(List.of(new DiaryTierDetector(),
		new BalloonUnlockDetector(), new StrongholdCoinsDetector(), new MuseumCampRewardDetector(),
		new HerbiFlaxHandInDetector(), new NaturalHistoryQuizDetector(), new KnightWavesDetector()));

	@lombok.Value
	private static class Registration
	{
		ActivityDetector detector;
		Set<ActivityKey> facts;
		Set<QuestKey> quests;
	}

	private final List<Registration> registrations;

	public ActivityRegistry(List<ActivityDetector> detectors)
	{
		List<Registration> definitions = new ArrayList<>();
		Set<ActivityKey> facts = new HashSet<>();
		Set<QuestKey> quests = new HashSet<>();
		for (ActivityDetector detector : detectors)
		{
			for (ActivityKey key : detector.facts())
			{
				if (!facts.add(key))
				{
					throw new IllegalArgumentException("Duplicate activity writer");
				}
			}
			for (QuestKey key : detector.quests())
			{
				if (!quests.add(key))
				{
					throw new IllegalArgumentException("Duplicate miniquest writer");
				}
			}
			definitions.add(new Registration(detector, Set.copyOf(detector.facts()), Set.copyOf(detector.quests())));
		}
		registrations = List.copyOf(definitions);
	}

	public ActivityObservation capture(Client client)
	{
		Map<ActivityKey, Boolean> facts = new HashMap<>();
		Map<QuestKey, AccountProgress.QuestStatus> quests = new HashMap<>();
		for (Registration registration : registrations)
		{
			ActivityObservation observation;
			try
			{
				observation = registration.getDetector().capture(client);
			}
			catch (RuntimeException failure)
			{
				log.debug("Activity family unavailable ({})", failure.getClass().getSimpleName());
				continue;
			}
			if (!registration.getFacts().containsAll(observation.getFacts().keySet())
				|| !registration.getQuests().containsAll(observation.getQuests().keySet()))
			{
				throw new IllegalStateException("Undeclared detector output");
			}
			facts.putAll(observation.getFacts());
			quests.putAll(observation.getQuests());
		}
		return new ActivityObservation(facts, quests);
	}
}
