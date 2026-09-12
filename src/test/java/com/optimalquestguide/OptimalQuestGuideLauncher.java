package com.optimalquestguide;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class OptimalQuestGuideLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(OptimalQuestGuidePlugin.class);
        RuneLite.main(args);
    }
}
