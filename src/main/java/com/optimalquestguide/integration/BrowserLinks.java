package com.optimalquestguide.integration;

import java.util.function.Consumer;
import javax.inject.Inject;
import net.runelite.client.util.LinkBrowser;

/** RuneLite provides native failure-dialog/copy-link recovery for asynchronous OS errors. */
public class BrowserLinks
{
    private final Consumer<String> opener;
    @Inject public BrowserLinks() { this(LinkBrowser::browse); }
    BrowserLinks(Consumer<String> opener) { this.opener = opener; }
    public String open(String url)
    {
        try
        {
            opener.accept(url);
            return "Sent to your browser. If it cannot open, RuneLite offers to copy the link.";
        }
        catch (RuntimeException e)
        {
            return "Could not open your browser. Click the guide to retry, or copy this link: " + url;
        }
    }
}
