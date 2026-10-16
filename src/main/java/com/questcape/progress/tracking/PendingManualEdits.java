package com.questcape.progress.tracking;

import java.util.*;
import lombok.Value;

/** Explicit removals survive failed cache recovery; acknowledgements cannot discard newer intent. */
final class PendingManualEdits
{
	@Value
	static class Edit
	{
		long revision;
		boolean checked;
	}

	private final Map<String, Map<String, Edit>> scopes = new HashMap<>();
	private long revision;

	synchronized void put(String scope, String key, boolean checked)
	{
		scopes.computeIfAbsent(scope, unused -> new HashMap<>()).put(key, new Edit(++revision, checked));
	}

	synchronized Map<String, Edit> snapshot(String scope)
	{
		return Collections.unmodifiableMap(new HashMap<>(scopes.getOrDefault(scope, Collections.emptyMap())));
	}

	synchronized Set<String> overlay(String scope, Set<String> saved)
	{
		Set<String> result = new HashSet<>(saved);
		scopes.getOrDefault(scope, Collections.emptyMap()).forEach((key, edit) ->
		{
			if (edit.isChecked())
			{
				result.add(key);
			}
			else
			{
				result.remove(key);
			}
		});
		return result;
	}

	synchronized void acknowledge(String scope, Map<String, Edit> captured)
	{
		Map<String, Edit> pending = scopes.get(scope);
		if (pending == null)
		{
			return;
		}
		captured.forEach((key, edit) -> pending.remove(key, edit));
		if (pending.isEmpty())
		{
			scopes.remove(scope);
		}
	}
}
