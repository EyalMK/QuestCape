package com.questcape.ui.panel;

import com.questcape.ui.GuideActions;
import javax.swing.JButton;

/** Real panel links for interaction tests without exposing panel implementation classes. */
public final class TestGuideLinks
{
	public static JButton link(String url, String label, GuideActions actions)
	{
		return GuideRowDetails.link(new GuideDetailProjector.Link(url, label), actions);
	}

	private TestGuideLinks()
	{
	}
}
