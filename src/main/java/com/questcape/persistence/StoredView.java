package com.questcape.persistence;

import lombok.Value;

/** One publication pairs a value with the outcome of saving that exact value. */
@Value
public class StoredView<T>
{
	public enum SaveState
	{
		NONE, PENDING, SAVED, UNSAVED
	}

	T data;
	SaveState saveState;

	public static <T> StoredView<T> empty()
	{
		return new StoredView<>(null, SaveState.NONE);
	}
}
