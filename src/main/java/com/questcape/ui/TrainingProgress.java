package com.questcape.ui;

import com.questcape.progress.TrainingEvaluation;
import com.questcape.guide.TrainingInstruction;
import java.awt.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.accessibility.*;
import javax.swing.*;
import static com.questcape.ui.UiComponents.*;
import static com.questcape.ui.UiTheme.*;

/** Compact presentation of prepared real-level comparisons; never calculates completion. */
final class TrainingProgress extends JPanel
{
	private TrainingEvaluation previous;
	private boolean supported;
	private final java.util.List<Circle> circles = new java.util.ArrayList<>();

	TrainingProgress(TrainingInstruction instruction)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setAlignmentX(LEFT_ALIGNMENT);
		if (instruction.getTargets().isEmpty())
		{
			Circle circle = new Circle();
			circles.add(circle);
			add(circle);
		}
		for (TrainingInstruction.Target target : instruction.getTargets())
		{
			Circle circle = new Circle();
			circles.add(circle);
			String name = human(target.getSkill());
			circle.getAccessibleContext().setAccessibleName(name);
			if (instruction.getTargets().size() > 1)
			{
				JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, UiMetrics.PROGRESS_GAP, 0));
				line.setAlignmentX(LEFT_ALIGNMENT);
				line.setOpaque(false);
				line.add(circle);
				JLabel skill = new JLabel(name);
				skill.setFont(font(UiMetrics.META, false));
				skill.setForeground(MUTED);
				line.add(skill);
				add(line);
			}
			else
			{
				add(circle);
			}
		}
	}

	void update(TrainingEvaluation result, boolean supported)
	{
		if (Objects.equals(previous, result) && this.supported == supported)
		{
			return;
		}
		previous = result;
		this.supported = supported;
		for (int index = 0; index < circles.size(); index++)
		{
			if (result == null || index >= result.getComparisons().size())
			{
				circles.get(index).update(null, false);
			}
			else
			{
				TrainingEvaluation.Comparison comparison = result.getComparisons().get(index);
				circles.get(index).update(supported ? comparison.getPercent() : null,
					supported && comparison.isComplete());
			}
		}
		repaint();
	}

	static final class Circle extends JComponent
	{
		Integer percent;
		boolean complete;

		Circle()
		{
			Dimension size = new Dimension(UiMetrics.TRAINING_CIRCLE, UiMetrics.TRAINING_CIRCLE);
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);
			setAlignmentX(LEFT_ALIGNMENT);
			setFont(font(UiMetrics.META, true));
			update(null, false);
		}

		void update(Integer percent, boolean complete)
		{
			this.percent = complete ? Integer.valueOf(UiMetrics.FULL_PERCENT) : percent;
			this.complete = complete;
			setToolTipText(this.percent == null ? UiCopy.UNDEFINED : this.percent + "%");
			getAccessibleContext()
				.setAccessibleDescription((complete ? UiCopy.TRAINING_COMPLETE : UiCopy.TRAINING_PROGRESS) + ". "
					+ getToolTipText() + ". " + UiCopy.INTERVAL_TOOLTIP);
		}

		@Override
		protected void paintComponent(Graphics original)
		{
			Graphics2D g = (Graphics2D)original.create();
			try
			{
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				double size = Math.min(getWidth(), getHeight()) - UiMetrics.CIRCLE_INSET * 2;
				double x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
				g.setStroke(new BasicStroke(UiMetrics.CIRCLE_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.setColor(BORDER);
				g.draw(new Ellipse2D.Double(x, y, size, size));
				g.setColor(complete ? GREEN : percent != null && percent > 0 ? GOLD : MUTED);
				if (percent != null && percent > 0)
				{
					g.draw(
						new Arc2D.Double(x, y, size, size, UiMetrics.CIRCLE_START_DEGREES,
							(double)UiMetrics.FULL_CIRCLE_DEGREES * percent / UiMetrics.FULL_PERCENT, Arc2D.OPEN));
				}
				if (complete)
				{
					Path2D check = new Path2D.Double();
					check.moveTo(x + size * .28, y + size * .52);
					check.lineTo(x + size * .44, y + size * .67);
					check.lineTo(x + size * .73, y + size * .34);
					g.draw(check);
				}
				else
				{
					String number = percent == null ? UiCopy.UNDEFINED : percent.toString();
					g.setColor(TEXT);
					g.setFont(getFont());
					FontMetrics metrics = g.getFontMetrics();
					g.drawString(number, (getWidth() - metrics.stringWidth(number)) / 2,
						(getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
				}
			}
			finally
			{
				g.dispose();
			}
		}

		@Override
		public AccessibleContext getAccessibleContext()
		{
			if (accessibleContext == null)
			{
				accessibleContext = new AccessibleCircle();
			}
			return accessibleContext;
		}

		private final class AccessibleCircle extends AccessibleJComponent implements AccessibleValue
		{
			@Override
			public AccessibleRole getAccessibleRole()
			{
				return AccessibleRole.PROGRESS_BAR;
			}

			@Override
			public AccessibleValue getAccessibleValue()
			{
				return this;
			}

			public Number getCurrentAccessibleValue()
			{
				return percent;
			}

			public Number getMinimumAccessibleValue()
			{
				return 0;
			}

			public Number getMaximumAccessibleValue()
			{
				return UiMetrics.FULL_PERCENT;
			}

			public boolean setCurrentAccessibleValue(Number value)
			{
				return false;
			}
		}
	}
}
