package com.questcape.integration;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Explicit local fixture preparation; no network or game interaction. */
public final class TestTrainingLinks
{
	public static TrainingGuideResolver local()
	{
		TrainingGuideResolver resolver = new TrainingGuideResolver(null);
		try
		{
			resolver.prepare();
			return resolver;
		}
		catch (IOException failure)
		{
			throw new UncheckedIOException(failure);
		}
	}

	private TestTrainingLinks()
	{
	}
}
