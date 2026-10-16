package com.questcape.persistence;

/** Stable storage contracts and bounded IO policy, separate from domain evaluation. */
public final class CachePolicy
{
	public static final String GUIDE = "guide", PROGRESS = "progress", STANDARD_ROUTE = "standard",
		SCHEMA_FIELD = "schema", EXTENSION = ".json", TEMP_PREFIX = "candidate-", TEMP_SUFFIX = ".tmp";
	public static final int SCHEMA = 1, MAX_BYTES = 4_000_000, READ_BUFFER = 8192;
	public static final long SAVE_RETRY_MILLIS = 60_000, UNCHANGED_SAVE_MILLIS = 60_000, GUIDE_COOLDOWN_MILLIS = 5_000;

	private CachePolicy()
	{
	}
}
