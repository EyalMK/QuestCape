package com.questcape.ui;

import com.questcape.guide.model.GuideRow;

/** User actions shared by Swing views and run-scoped application coordination. */
public interface GuideActions
{
	void syncPlayer();

	void refresh();

	void training(String skill);

	void manual(String scope, GuideRow row, boolean checked);

	void source(String url);

	void report();

	boolean copyLink(String url);
}
