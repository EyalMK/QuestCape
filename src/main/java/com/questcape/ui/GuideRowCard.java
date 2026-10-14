package com.questcape.ui;

import com.questcape.guide.*;
import com.questcape.integration.TrainingGuideResolver;
import com.questcape.progress.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import javax.accessibility.*;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;

/** One card owns disclosure and independent actions, never the parent panel or services. */
final class GuideRowCard extends JPanel
{
	final GuideRow row;
	final int stepNumber;
	final JLabel stepIcon = new JLabel();
	final Component spacer = Box.createVerticalStrut(9);
	private final JTextArea status = text("", MUTED, UiMetrics.BODY, false);
	private final JTextArea comparison = text("", MUTED, UiMetrics.META, false);
	private final JTextArea reason = text("", MUTED, UiMetrics.META, false);
	private final JCheckBox check = new JCheckBox("Mark step complete");
	private final GuideRowDetails details;
	private final JButton expand = textButton(UiCopy.DETAILS_CLOSED);
	private final BiConsumer<GuideRow, Boolean> disclosure;
	private final Runnable userIntent;
	String scope;
	Completion last;
	private boolean highlighted;
	private boolean disposed;
	private JTextComponent pendingText;
	private final javax.swing.Timer textClick = new javax.swing.Timer(multiClickDelay(), e ->
	{
		JTextComponent selected = pendingText;
		pendingText = null;
		if (!disposed && selected != null && selected.getSelectionStart() == selected.getSelectionEnd())
		{
			toggleDetails();
		}
	});

	private static int multiClickDelay()
	{
		Object interval = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
		return interval instanceof Number ? Math.max(1, ((Number)interval).intValue())
			: UiMetrics.MULTI_CLICK_FALLBACK_MS;
	}

	void dispose()
	{
		disposed = true;
		textClick.stop();
		pendingText = null;
	}

	GuideRowCard(GuideRow row, int number, boolean expanded, GuideAssets assets,
		GuidePanel.Actions actions, TrainingGuideResolver training, BiConsumer<GuideRow, Boolean> disclosure,
		Runnable userIntent)
	{
		this.row = row;
		stepNumber = number;
		this.disclosure = disclosure;
		this.userIntent = userIntent;
		textClick.setRepeats(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(CARD);
		setAlignmentX(LEFT_ALIGNMENT);
		updateBorder();
		JPanel heading = new JPanel(new BorderLayout(5, 0));
		heading.setOpaque(false);
		heading.setAlignmentX(LEFT_ALIGNMENT);
		stepIcon.setIcon(assets.steps.get(row.getKind()));
		stepIcon.setToolTipText(human(row.getKind().name()));
		stepIcon.getAccessibleContext().setAccessibleName(human(row.getKind().name()) + " icon");
		heading.add(stepIcon, BorderLayout.WEST);
		heading.add(
			text(String.format("%03d", number) + " / " + human(row.getKind().name()), MUTED, UiMetrics.META, false),
			BorderLayout.CENTER);
		add(heading);
		if (row.getKind() == GuideRow.Kind.TRAINING)
		{
			SkillText title = new SkillText(row.getTitle(), font(UiMetrics.CARD_TITLE, true), TEXT, assets.skills,
				row.training().getTokens());
			assets.texts.add(title);
			add(title);
		}
		else
		{
			add(text(row.getTitle(), TEXT, UiMetrics.CARD_TITLE, true));
		}
		add(status);
		if (row.getKind() == GuideRow.Kind.TRAINING)
		{
			add(comparison);
		}
		reason.setVisible(false);
		add(reason);
		if (row.isManual())
		{
			check.setFont(font(UiMetrics.META, false));
			check.setForeground(MUTED);
			check.setOpaque(false);
			check.setAlignmentX(LEFT_ALIGNMENT);
			check.addActionListener(e -> actions.manual(scope, row, check.isSelected()));
			add(check);
		}
		if (row.getKind() == GuideRow.Kind.TRAINING)
		{
			for (TrainingInstruction.Target target : row.training().getTargets())
			{
				String skill = target.getSkill();
				String label = training.resolve(skill).equals(TrainingGuideResolver.FALLBACK) ? UiCopy.FALLBACK
					: row.training().getTargets().size() == 1 ? UiCopy.THEOATRIX
						: "Theoatrix guide: " + human(skill) + " ↗";
				JButton action = textButton(label);
				action.setFont(font(UiMetrics.BODY, false));
				action.setToolTipText(training.label(skill));
				action.addActionListener(e -> actions.training(skill));
				add(action);
			}
		}
		expand.setFont(font(UiMetrics.BODY, false));
		expand.addActionListener(e -> toggleDetails());
		add(expand);
		details = new GuideRowDetails(GuideDetailProjector.of(row), assets, actions);
		details.setVisible(expanded);
		expand.setText(expanded ? UiCopy.DETAILS_OPEN : UiCopy.DETAILS_CLOSED);
		add(details);
		installDisclosure(this);
		setFocusable(true);
		addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				updateBorder();
			}

			public void focusLost(FocusEvent e)
			{
				updateBorder();
			}
		});
		getAccessibleContext().setAccessibleName(row.getTitle() + " details");
		accessibleDisclosure();
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "details");
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "details");
		getActionMap().put("details", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				toggleDetails();
			}
		});
	}

	private void toggleDetails()
	{
		if (disposed)
		{
			return;
		}
		textClick.stop();
		pendingText = null;
		boolean open = !details.isVisible();
		disclosure.accept(row, open);
		details.setVisible(open);
		expand.setText(open ? UiCopy.DETAILS_OPEN : UiCopy.DETAILS_CLOSED);
		accessibleDisclosure();
		revalidate();
		repaint();
	}

	private void accessibleDisclosure()
	{
		getAccessibleContext().setAccessibleDescription((details.isVisible() ? "Expanded" : "Collapsed")
			+ ". Click the card or press Enter or Space to toggle details.");
		expand.getAccessibleContext().setAccessibleDescription(details.isVisible() ? "Expanded" : "Collapsed");
	}

	@Override
	public AccessibleContext getAccessibleContext()
	{
		if (accessibleContext == null)
		{
			accessibleContext = new AccessibleCard();
		}
		return accessibleContext;
	}

	private final class AccessibleCard extends AccessibleJPanel
	{
		public AccessibleStateSet getAccessibleStateSet()
		{
			AccessibleStateSet states = super.getAccessibleStateSet();
			states.add(details != null && details.isVisible() ? AccessibleState.EXPANDED : AccessibleState.COLLAPSED);
			return states;
		}
	}

	private void installDisclosure(Component component)
	{
		if (component instanceof AbstractButton)
		{
			component.addMouseListener(new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
				{
					userIntent.run();
					textClick.stop();
					pendingText = null;
				}
			});
			((AbstractButton)component).addActionListener(e ->
			{
				userIntent.run();
				textClick.stop();
				pendingText = null;
			});
			return;
		}
		MouseAdapter listener = new MouseAdapter()
		{
			private boolean dragged;

			public void mousePressed(MouseEvent e)
			{
				userIntent.run();
				dragged = false;
				textClick.stop();
				pendingText = null;
			}

			public void mouseDragged(MouseEvent e)
			{
				dragged = true;
				textClick.stop();
				pendingText = null;
			}

			public void mouseClicked(MouseEvent e)
			{
				boolean selected = component instanceof JTextComponent
					&& ((JTextComponent)component).getSelectionStart() != ((JTextComponent)component).getSelectionEnd();
				if (e.getClickCount() > 1)
				{
					textClick.stop();
					pendingText = null;
					return;
				}
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && !dragged && !selected)
				{
					if (component instanceof JTextComponent)
					{
						pendingText = (JTextComponent)component;
						textClick.restart();
					}
					else
					{
						toggleDetails();
					}
				}
			}
		};
		component.addMouseListener(listener);
		component.addMouseMotionListener(listener);
		component.setCursor(
			Cursor.getPredefinedCursor(component instanceof JTextComponent ? Cursor.TEXT_CURSOR : Cursor.HAND_CURSOR));
		if (component instanceof Container)
		{
			for (Component child : ((Container)component).getComponents())
			{
				installDisclosure(child);
			}
		}
	}

	void update(GuideProjection.Entry projection, AccountProgress account)
	{
		Completion completion = projection.getCompletion();
		scope = account == null ? null : account.getScope();
		if (!completion.equals(last))
		{
			setText(status, completion.label());
			status.setForeground(UiTheme.stateColor(completion.getState()));
			setBackground(completion.getState() == Completion.State.COMPLETE ? COMPLETE_CARD : CARD);
			last = completion;
			updateBorder();
		}
		setText(comparison, projection.getComparison());
		comparison.setToolTipText(UiCopy.INTERVAL_TOOLTIP);
		setText(reason, projection.getExplanation());
		reason.setVisible(!projection.getExplanation().isEmpty());
		check.setEnabled(account != null);
		check.setSelected(account != null && account.getManual().contains(row.getKey()));
	}

	void highlight(boolean value)
	{
		if (highlighted == value)
		{
			return;
		}
		highlighted = value;
		updateBorder();
		status.setToolTipText(value ? "Your next step: the earliest unfinished step in route order." : null);
	}

	private void updateBorder()
	{
		Color accent = last != null && last.getState() == Completion.State.IN_PROGRESS ? GOLD
			: UiTheme.kindColor(row.getKind());
		if (isFocusOwner())
		{
			accent = BLUE;
		}
		else if (highlighted && (row.getKind() == GuideRow.Kind.QUEST || row.getKind() == GuideRow.Kind.MINIQUEST)
			&& (last == null || last.getState() != Completion.State.IN_PROGRESS))
		{
			accent = BLUE;
		}
		int outline = isFocusOwner() ? 2 : 1;
		setBorder(new CompoundBorder(new MatteBorder(outline, 3, outline, outline, accent),
			new EmptyBorder(9 - outline, 8, 7 - outline, 9 - outline)));
	}
}
