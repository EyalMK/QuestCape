package com.questcape.integration;

/** Bounded transport policy and protocol names; no account information belongs in request identity. */
public final class HttpPolicy
{
	public static final int CONNECT_SECONDS = 10, READ_SECONDS = 20, CALL_SECONDS = 30, NOT_MODIFIED = 304, OK = 200,
		REDIRECT_MIN = 300, REDIRECT_MAX = 399, MAX_REDIRECTS = 5;
	public static final String USER_AGENT = "User-Agent", ETAG = "ETag", LAST_MODIFIED = "Last-Modified",
		LOCATION = "Location", RETRY_AFTER = "Retry-After", IF_NONE_MATCH = "If-None-Match",
		IF_MODIFIED_SINCE = "If-Modified-Since";

	private HttpPolicy()
	{
	}
}
