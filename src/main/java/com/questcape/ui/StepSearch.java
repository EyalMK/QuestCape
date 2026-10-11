package com.questcape.ui;

import java.util.Locale;

/** Bounded title matching, with route order used to break equal scores. */
final class StepSearch
{
	private StepSearch()
	{
	}

	static String normalize(String value)
	{
		return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
	}

	static int score(String title, String query)
	{
		String name = normalize(title), needle = normalize(query);
		if (needle.isEmpty() || needle.length() > 160)
		{
			return Integer.MAX_VALUE;
		}
		if (name.equals(needle))
		{
			return 0;
		}
		if (name.startsWith(needle))
		{
			return 10;
		}
		if (name.contains(needle))
		{
			return 20;
		}
		boolean tokens = true;
		for (String word : needle.split(" "))
		{
			tokens &= name.contains(word);
		}
		if (tokens)
		{
			return 30;
		}
		if (needle.length() < 3)
		{
			return Integer.MAX_VALUE;
		}
		int distance = distance(name, needle);
		for (String word : name.split(" "))
		{
			distance = Math.min(distance, distance(word, needle));
		}
		return distance <= Math.max(1, needle.length() / 4) ? 40 + distance : Integer.MAX_VALUE;
	}

	private static int distance(String a, String b)
	{
		if (Math.abs(a.length() - b.length()) > Math.max(1, b.length() / 4))
		{
			return Integer.MAX_VALUE;
		}
		int[] previous = new int[b.length() + 1];
		for (int j = 0; j <= b.length(); j++)
		{
			previous[j] = j;
		}
		for (int i = 1; i <= a.length(); i++)
		{
			int[] current = new int[b.length() + 1];
			current[0] = i;
			for (int j = 1; j <= b.length(); j++)
			{
				current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1),
					previous[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
			}
			previous = current;
		}
		return previous[b.length()];
	}
}
