package com.optimalquestguide;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(OptimalQuestGuideConfig.GROUP)
public interface OptimalQuestGuideConfig extends Config
{
    String GROUP = "optimalquestguide";

    @ConfigItem(keyName = "resumeQuestOnLogin", name = "Resume quest on login",
        description = "Resume the last confirmed unfinished quest on the next login, when a compatible Quest Helper integration is available.")
    default boolean resumeQuestOnLogin() { return true; }
}
