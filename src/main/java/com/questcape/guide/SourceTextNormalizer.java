package com.questcape.guide;

import java.text.Normalizer;
import java.util.Locale;

/** Pure source spelling normalization, independent of identity catalogs. */
public final class SourceTextNormalizer
{
	public static String normalize(String text)
	{
		return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKC)
			.replace('_', ' ').replace('\u00a0', ' ').replace('\u2019', '\'')
			.replace('\u2013', '-').replace('\u2014', '-').trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

	private SourceTextNormalizer()
	{
	}
}
