package com.questcape.integration.links;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;
import javax.inject.Inject;
import net.runelite.client.util.LinkBrowser;

/** RuneLite provides native failure-dialog/copy-link recovery for asynchronous OS errors. */
@lombok.extern.slf4j.Slf4j
public class BrowserLinks
{
	private final Consumer<String> opener;
	private final Consumer<String> copier;

	@Inject
	public BrowserLinks()
	{
		this(LinkBrowser::browse);
	}

	BrowserLinks(Consumer<String> opener)
	{
		this(opener,
			value -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null));
	}

	BrowserLinks(Consumer<String> opener, Consumer<String> copier)
	{
		this.opener = opener;
		this.copier = copier;
	}

	public String open(String url)
	{
		try
		{
			opener.accept(url);
			return "";
		}
		catch (RuntimeException e)
		{
			return "Could not open your browser. Click the guide to retry, or copy this link: " + url;
		}
	}

	public boolean copy(String url)
	{
		try
		{
			copier.accept(url);
			return true;
		}
		catch (RuntimeException failure)
		{
			log.debug("QuestCape link copy failed ({})", failure.getClass().getSimpleName());
			return false;
		}
	}
}
