package com.questcape.ui.panel;

import com.questcape.guide.model.GuideRow;
import com.questcape.progress.evaluation.Completion;
import com.questcape.ui.style.GuideControlIcons.*;
import com.questcape.ui.style.UiMetrics;
import com.questcape.ui.style.UiTheme;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.*;

import static com.questcape.ui.style.UiComponents.*;
import static com.questcape.ui.style.UiTheme.*;

/** Existing artwork and single-choice menus; selection is reported explicitly. */
final class GuideFilterBar extends JPanel
{
	final JButton types = iconButton("Filter by step type", new FilterIcon(false, false)),
		states = iconButton("Filter by progress state", new FilterIcon(true, false)),
		reset = iconButton("Reset filters and search", new ResetIcon());
	final Map<GuideRow.Kind, JRadioButtonMenuItem> typeOptions = new EnumMap<>(GuideRow.Kind.class);
	final Map<Completion.State, JRadioButtonMenuItem> stateOptions = new EnumMap<>(Completion.State.class);
	final JRadioButtonMenuItem allTypes = new JRadioButtonMenuItem("All step types", true),
		allStates = new JRadioButtonMenuItem("All progress states", true);
	final JTextArea filterStatus = text("", MUTED, UiMetrics.META, false);
	private final JPopupMenu typeMenu = new JPopupMenu(), stateMenu = new JPopupMenu();

	GuideFilterBar(Map<GuideRow.Kind, Icon> stepIcons, Consumer<GuideRow.Kind> typeSelected,
		Consumer<Completion.State> stateSelected, Runnable resetAction)
	{
		setOpaque(false);
		setAlignmentX(LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createVerticalStrut(5));
		ButtonGroup typeGroup = new ButtonGroup(), stateGroup = new ButtonGroup();
		for (JPopupMenu menu : List.of(typeMenu, stateMenu))
		{
			menu.setBackground(CARD);
			menu.setBorder(new LineBorder(BORDER));
		}
		allTypes.setIcon(new FilterIcon(false, false));
		styleOption(allTypes, MUTED, 12);
		allStates.setIcon(new FilterIcon(true, false));
		styleOption(allStates, MUTED, 4);
		typeGroup.add(allTypes);
		stateGroup.add(allStates);
		typeMenu.add(allTypes);
		stateMenu.add(allStates);
		typeMenu.addSeparator();
		stateMenu.addSeparator();
		allTypes.addActionListener(e ->
		{
			typeSelected.accept(null);
		});
		allStates.addActionListener(e ->
		{
			stateSelected.accept(null);
		});
		for (GuideRow.Kind kind : GuideRow.Kind.values())
		{
			if (kind == GuideRow.Kind.INFORMATION)
			{
				continue;
			}
			JRadioButtonMenuItem option = new JRadioButtonMenuItem(human(kind.name()), stepIcons.get(kind));
			styleOption(option, TEXT, 12);
			option.addActionListener(e ->
			{
				typeSelected.accept(kind);
			});
			typeGroup.add(option);
			typeOptions.put(kind, option);
			typeMenu.add(option);
		}
		for (Completion.State state : List.of(Completion.State.COMPLETE, Completion.State.INCOMPLETE,
			Completion.State.IN_PROGRESS, Completion.State.UNKNOWN))
		{
			JRadioButtonMenuItem option = new JRadioButtonMenuItem(human(state.name()), new StateIcon(state));
			styleOption(option, UiTheme.stateColor(state), 4);
			option.addActionListener(e ->
			{
				stateSelected.accept(state);
			});
			stateGroup.add(option);
			stateOptions.put(state, option);
			stateMenu.add(option);
		}
		types.addActionListener(e -> typeMenu.show(types, 0, types.getHeight()));
		states.addActionListener(e -> stateMenu.show(states, 0, states.getHeight()));
		JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
		buttons.setOpaque(false);
		buttons.add(types);
		buttons.add(states);
		buttons.add(reset);
		buttons.setAlignmentX(LEFT_ALIGNMENT);
		add(buttons);
		add(filterStatus);

		reset.addActionListener(e -> resetAction.run());
	}

	void dispose()
	{
		typeMenu.setVisible(false);
		stateMenu.setVisible(false);
	}

	private static void styleOption(JRadioButtonMenuItem option, Color color, int gap)
	{
		option.setFont(font(14, false));
		option.setForeground(color);
		option.setBackground(CARD);
		option.setBorder(new EmptyBorder(6, 8, 6, 8));
		option.setIconTextGap(gap);
	}

}
