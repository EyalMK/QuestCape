package com.questcape.integration;

import com.questcape.guide.GuideParser;
import java.io.IOException;
import java.util.function.*;
import javax.swing.SwingUtilities;

/** Explicit browser actions and bounded asynchronous destination verification, scoped to one run. */
public final class TrainingLinkController
{
	private static final String CHECKING = "Checking Theoatrix destination…",
		QUEUE_BUSY = "Request queue is busy. Retry shortly.",
		RETRY = " · Click the training action to retry. Browse Theoatrix guides: ", LINK = " Link: ";
	private final PluginRun run;
	private final TrainingGuideResolver resolver;
	private final BrowserLinks browser;
	private final LongSupplier account;
	private final BiConsumer<Long, String> message;

	public TrainingLinkController(PluginRun run, TrainingGuideResolver resolver, BrowserLinks browser,
		LongSupplier account, BiConsumer<Long, String> message)
	{
		this.run = run;
		this.resolver = resolver;
		this.browser = browser;
		this.account = account;
		this.message = message;
	}

	public void training(String skill)
	{
		if (!run.active())
		{
			return;
		}
		String url = resolver.resolve(skill);
		long authority = account.getAsLong();
		message.accept(authority, CHECKING);
		if (!run.network(() ->
		{
			try
			{
				if (!current(authority))
				{
					return;
				}
				String destination = resolver.verifyDestination(url);
				SwingUtilities.invokeLater(() ->
				{
					if (current(authority))
					{
						message.accept(authority, browser.open(destination));
					}
				});
			}
			catch (IOException | RuntimeException failure)
			{
				if (current(authority))
				{
					message.accept(authority,
						failure.getMessage() + LINK + url + RETRY
							+ TrainingGuideResolver.FALLBACK);
				}
			}
		}))
		{
			message.accept(authority, QUEUE_BUSY);
		}
	}

	private boolean current(long authority)
	{
		return run.active() && authority == account.getAsLong();
	}

	public void source(String url)
	{
		if (run.active() && (GuideParser.safeWikiLink(url) != null || ApplicationLinks.CONTENT_LICENSE.equals(url)))
		{
			message.accept(-1L, browser.open(url));
		}
	}

	public void report()
	{
		if (run.active())
		{
			message.accept(-1L, browser.open(ApplicationLinks.ISSUES));
		}
	}

	/** Explicit clipboard gesture only; no destination request or browser action. */
	public boolean copyLink(String url)
	{
		return run.active() && url != null && (GuideParser.safeWikiLink(url) != null || TrainingGuideResolver.safe(url)
			|| ApplicationLinks.ISSUES.equals(url) || ApplicationLinks.CONTENT_LICENSE.equals(url))
			&& browser.copy(url);
	}
}
