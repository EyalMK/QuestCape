package com.questcape.progress.activity;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.guide.parsing.GuideParserTest;
import com.questcape.persistence.JsonStore;
import com.questcape.progress.compatibility.ActivityProgress;
import com.questcape.progress.evaluation.Completion;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressService;
import java.util.*;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UnlockProgressTest
{
	private static GuideRow row(String title) throws Exception
	{
		return new GuideParser().parse(GuideParserTest.table(GuideParserTest.activity(title))).get(0);
	}

	private static AccountProgress account(Client client)
	{
		return new AccountProgress("live:test:STANDARD", "Synthetic", "STANDARD", "Live RuneLite", 1, 1L,
			Map.of("SHADES_OF_MORTTON", AccountProgress.QuestStatus.COMPLETE,
				"BONE_VOYAGE", AccountProgress.QuestStatus.COMPLETE), Map.of("HERBLORE", 99),
			ActivityProgress.readActivities(client), Set.of());
	}

	@Test
	public void balloonRoutesAreIndependentAndRequireTheirExactFlag() throws Exception
	{
		Client client = mock(Client.class);
		String[] destinations = {"Crafting Guild", "Varrock", "Castle Wars", "Grand Tree"};
		int[] flags = {VarbitID.ZEP_MULTI_CRAFT, VarbitID.ZEP_MULTI_VARR, VarbitID.ZEP_MULTI_CAST, VarbitID.ZEP_MULTI_GNO};
		Set<String> keys = new HashSet<>();
		for (int i = 0; i < flags.length; i++)
		{
			reset(client);
			GuideRow target = row("Unlock: Balloon transport system to " + destinations[i]);
			keys.add(target.getKey());
			assertFalse(target.isManual());
			for (int value : new int[] {0, 1, 2})
			{
				when(client.getVarbitValue(flags[i])).thenReturn(value);
				AccountProgress live = account(client);
				for (int j = 0; j < destinations.length; j++)
				{
					assertEquals(i == j && value == 1 ? Completion.State.COMPLETE : Completion.State.INCOMPLETE,
						Completion.of(row("Unlock: Balloon transport system to " + destinations[j]), live).getState());
				}
			}
		}
		assertEquals(4, keys.size());
	}

	@Test
	public void strongholdNeedsAllCoinRewardsWithoutRequiringBoots() throws Exception
	{
		Client client = mock(Client.class);
		GuideRow stronghold = row("Unlock: Stronghold of Security");
		when(client.getVarbitValue(VarbitID.SOS_EMOTE_STAMP)).thenReturn(1);
		assertEquals(Completion.State.INCOMPLETE, Completion.of(stronghold, account(client)).getState());
		when(client.getVarbitValue(VarbitID.SOS_EMOTE_STAMP)).thenReturn(0);
		when(client.getVarbitValue(VarbitID.SOS_EMOTE_FLAP)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.SOS_EMOTE_DOH)).thenReturn(1);
		assertEquals(Completion.State.INCOMPLETE, Completion.of(stronghold, account(client)).getState());
		when(client.getVarbitValue(VarbitID.SOS_EMOTE_IDEA)).thenReturn(1);
		assertEquals(Completion.State.COMPLETE, Completion.of(stronghold, account(client)).getState());
	}

	@Test
	public void campAndHandInRequireRewardsRatherThanPrerequisitesOrLevels() throws Exception
	{
		Client client = mock(Client.class);
		GuideRow camp = row("Unlock: Museum Camp");
		GuideRow handIn = row("Hand in the diary of Herbi Flax to the Apothecary. This trains Herblore.");
		when(client.getVarbitValue(VarbitID.FOSSIL_CAMP)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FOSSIL_BANK)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FOSSIL_TASKREWARDS_NORTH)).thenReturn(1);
		when(client.getVarpValue(VarPlayerID.MORTTONMULTI)).thenReturn(Integer.MIN_VALUE | (1 << 29));
		assertEquals(Completion.State.INCOMPLETE, Completion.of(camp, account(client)).getState());
		assertEquals(Completion.State.INCOMPLETE, Completion.of(handIn, account(client)).getState());
		when(client.getVarbitValue(VarbitID.FOSSIL_TASKREWARDS_CAMP)).thenReturn(1);
		when(client.getVarpValue(VarPlayerID.MORTTONMULTI)).thenReturn(Integer.MIN_VALUE | (1 << 30));
		assertEquals(Completion.State.COMPLETE, Completion.of(camp, account(client)).getState());
		assertEquals(Completion.State.COMPLETE, Completion.of(handIn, account(client)).getState());
		assertFalse(handIn.isManual());
		assertEquals(GuideRow.Kind.ACTIVITY, handIn.getKind());
	}

	@Test
	public void allNonKudosChecksAreInertAndStaleCallbacksCannotWriteThem() throws Exception
	{
		Client client = mock(Client.class);
		JsonStore store = mock(JsonStore.class);
		ProgressService service = new ProgressService(store);
		List<GuideRow> rows = new GuideParser().parse(GuideParserTest.fixture());
		Set<String> oldKeys = new HashSet<>();
		rows.forEach(r -> oldKeys.add(r.getKey()));
		AccountProgress old = account(client).withManual(oldKeys);
		when(store.read("progress", old.getScope(), AccountProgress.class)).thenReturn(old);
		service.acceptLive(account(client));
		clearInvocations(store);
		int manual = 0, automatic = 0;
		for (GuideRow row : rows)
		{
			if (row.isManual())
			{
				manual++;
				assertTrue(row.getTitle().endsWith("Kudos"));
				assertEquals(Completion.State.COMPLETE, Completion.of(row, service.current()).getState());
			}
			else
			{
				service.toggleManual(old.getScope(), row, true);
				if (row.getActivityIdentity() != null && row.getKind() != GuideRow.Kind.DIARY)
				{
					automatic++;
					assertEquals(row.getTitle(), Completion.State.INCOMPLETE, Completion.of(row, service.current()).getState());
				}
			}
		}
		assertEquals(2, manual);
		assertEquals(7, automatic);
		verifyNoInteractions(store);
		GuideRow future = row("Unlock: Uncharted balloon route");
		assertFalse(future.isManual());
		assertEquals(Completion.State.UNKNOWN, Completion.of(future, service.current()).getState());
		assertNull(ActivityProgress.handIn("Hand in a new item from Herbi Flax to someone else"));
	}
}
