package com.questcape.guide;

/** Source shape limits and the reviewed Wiki transport host. */
public final class GuideSourcePolicy
{
	public static final double MIN_RETAINED_ROW_RATIO = 0.8;
	public static final String RESPONSE_PARSE = "parse", RESPONSE_TEXT = "text", RESPONSE_HTML = "*",
		RESPONSE_REVISION = "revid", CAPTION = "old school runescape quest guide";
	public static final String HOST = "oldschool.runescape.wiki", SECURE_SCHEME = "https",
		SECURE_ORIGIN = "https://oldschool.runescape.wiki";
	public static final int MAX_CONTENT = 4_000_000, MIN_ROWS = 2, MAX_ROWS = 2000, COLUMNS = 7, MAX_SPAN = 100,
		SECURE_PORT = 443, ATTRIBUTE_LENGTH = 256;

	private GuideSourcePolicy()
	{
	}
}
