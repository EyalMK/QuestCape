package com.questcape.guide;

import java.util.*;
import java.util.regex.*;
import lombok.Value;

/** Small lexical boundary shared by role-specific source interpreters. Offsets refer to original text. */
public final class SourceTextTokenizer
{
	private static final Pattern LEXEME = Pattern.compile("[\\p{L}\\p{N}_]+|[^\\s]");

	@Value
	public static class Token
	{
		String text;
		int start;
		int end;
	}

	public static List<Token> words(String source)
	{
		List<Token> result = new ArrayList<>();
		Matcher matcher = LEXEME.matcher(source);
		while (matcher.find())
		{
			result.add(new Token(matcher.group().toLowerCase(Locale.ROOT), matcher.start(), matcher.end()));
		}
		return Collections.unmodifiableList(result);
	}

	private SourceTextTokenizer()
	{
	}
}
