package com.questcape.ui.sidebar;

import com.questcape.guide.model.GuideRow;
import com.questcape.guide.model.GuideSnapshot;
import com.questcape.guide.repository.GuideRepository;
import com.questcape.integration.OperationOutcome;
import com.questcape.integration.OperationOutcome.State;
import com.questcape.integration.coordination.GuideRefreshCoordinator;
import com.questcape.integration.coordination.ProgressCoordinator;
import com.questcape.integration.lifecycle.PluginRun;
import com.questcape.integration.links.TrainingGuideResolver;
import com.questcape.persistence.StoredView;
import com.questcape.progress.model.AccountProgress;
import com.questcape.progress.tracking.ProgressService;
import com.questcape.ui.GuideActions;
import com.questcape.ui.SidebarStatus;
import com.questcape.ui.panel.GuidePanel;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.*;
import net.runelite.client.ui.*;
import net.runelite.client.util.ImageUtil;

/** Owns this run's sidebar attachment and coherent EDT publication, not component interaction state. */
@Slf4j
public final class SidebarController implements AutoCloseable
{
	private static final int NAVIGATION_PRIORITY = 6;
	private final PluginRun run;
	private final ClientToolbar toolbar;
	private final SpriteManager sprites;
	private final SkillIconManager skills;
	private final GuideRepository guide;
	private final ProgressService progress;
	private final ProgressCoordinator capture;
	private final GuideRefreshCoordinator refresh;
	private final TrainingGuideResolver training;
	private final GuideActions actions;
	private final AtomicBoolean queued = new AtomicBoolean();
	private GuidePanel panel;
	private NavigationButton navigation;

	public SidebarController(PluginRun run, ClientToolbar toolbar, SpriteManager sprites, SkillIconManager skills,
		GuideRepository guide, ProgressService progress,
		ProgressCoordinator capture, GuideRefreshCoordinator refresh, TrainingGuideResolver training,
		GuideActions actions)
	{
		this.run = run;
		this.toolbar = toolbar;
		this.sprites = sprites;
		this.skills = skills;
		this.guide = guide;
		this.progress = progress;
		this.capture = capture;
		this.refresh = refresh;
		this.training = training;
		this.actions = actions;
	}

	public void start()
	{
		run.work(() ->
		{
			try
			{
				training.prepare();
				SidebarAssets assets = SidebarAssets.prepare(skills);
				SwingUtilities.invokeLater(() ->
				{
					try
					{
						if (!run.active())
						{
							return;
						}
						GuidePanel created = new GuidePanel(actions, training);
						panel = created;
						assets.apply(created);
						if (!run.active())
						{
							created.dispose();
							return;
						}
						panel = created;
						navigation = NavigationButton.builder().tooltip("QuestCape").icon(assets.navigation())
							.priority(NAVIGATION_PRIORITY).panel(created).build();
						toolbar.addNavigation(navigation);
						loadCategoryImages();
						render();
					}
					catch (RuntimeException failure)
					{
						log.debug("QuestCape sidebar publication failed ({})", failure.getClass().getSimpleName());
						run.close();
					}
				});
			}
			catch (IOException | RuntimeException failure)
			{
				log.debug("QuestCape sidebar preparation failed ({})", failure.getClass().getSimpleName());
				run.close();
			}
		});
	}

	private void loadCategoryImages()
	{
		Map<GuideRow.Kind, Integer> definitions = Map.of(GuideRow.Kind.QUEST,
			SpriteID.AchievementDiaryIcons.BLUE_QUESTS,
			GuideRow.Kind.DIARY, SpriteID.AchievementDiaryIcons.GREEN_ACHIEVEMENT_DIARIES, GuideRow.Kind.TRAINING,
			SpriteID.SideIcons.STATS);
		definitions.forEach((kind, sprite) -> sprites.getSpriteAsync(sprite, 0, image -> run.work(() ->
		{
			java.awt.image.BufferedImage resized = ImageUtil.resizeImage(image, SidebarAssets.SKILL_SIZE,
				SidebarAssets.SKILL_SIZE);
			SwingUtilities.invokeLater(() ->
			{
				if (run.active() && panel != null)
				{
					panel.setStepSprite(kind, resized);
				}
			});
		})));
	}

	public void message(long authority, String text)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (run.active() && panel != null && (authority == -1 || authority == capture.authority()))
			{
				panel.message(text);
			}
		});
	}

	public void render()
	{
		if (!run.active() || !queued.compareAndSet(false, true))
		{
			return;
		}
		SwingUtilities.invokeLater(() ->
		{
			queued.set(false);
			if (!run.active() || panel == null)
			{
				return;
			}
			long authority = capture.authority(), session = progress.token();
			StoredView<AccountProgress> account = progress.view();
			StoredView<GuideSnapshot> route = guide.view();
			OperationOutcome content = refresh.status(), local = capture.status();
			SidebarStatus status = new SidebarStatus(content.getState(),
				local.getAccount() == authority ? local.getState() : State.IDLE,
				route.getSaveState(), account.getSaveState(), content.getDetail(),
				local.getAccount() == authority ? local.getDetail() : "");
			if (!run.active() || authority != capture.authority() || session != progress.token())
			{
				render();
				return;
			}
			panel.render(route.getData(), account.getData(), status, capture.ready());
		});
	}

	@Override
	public void close()
	{
		// Read this controller's attachment on EDT, including one created while close was being queued.
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.dispose();
				panel = null;
			}
			if (navigation != null)
			{
				toolbar.removeNavigation(navigation);
				navigation = null;
			}
		});
	}
}
