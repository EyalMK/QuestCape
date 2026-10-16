package com.questcape.progress.activity.definitions;

import lombok.Getter;
import net.runelite.api.gameval.VarbitID;

public enum BalloonDestination
{
	CRAFTING_GUILD("CRAFTING_GUILD", "crafting guild", VarbitID.ZEP_MULTI_CRAFT),
	VARROCK("VARROCK", "varrock", VarbitID.ZEP_MULTI_VARR),
	CASTLE_WARS("CASTLE_WARS", "castle wars", VarbitID.ZEP_MULTI_CAST),
	GRAND_TREE("GRAND_TREE", "grand tree", VarbitID.ZEP_MULTI_GNO);

	@Getter
	private final String token, source;
	@Getter
	private final int varbit;

	BalloonDestination(String token, String source, int varbit)
	{
		this.token = token;
		this.source = source;
		this.varbit = varbit;
	}
}
