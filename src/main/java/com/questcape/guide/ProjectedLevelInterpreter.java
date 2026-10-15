package com.questcape.guide;

import java.util.*;
import net.runelite.api.Skill;
import lombok.Value;

/** Projected source levels are presentation data. Any unparsed annotation retains the raw field. */
public final class ProjectedLevelInterpreter
{
	@Value
	public static class Entry
	{
		int level;
		Skill skill;
	}

	public static List<Entry> interpret(String source)
	{
		List<SourceTextTokenizer.Token> tokens = SourceTextTokenizer.words(source);
		List<Entry> result = new ArrayList<>();
		int at = 0;
		while (at < tokens.size())
		{
			String number = tokens.get(at++).getText();
			if (number.equals(",") || number.equals(";"))
			{
				continue;
			}
			if (number.length() > SkillCatalog.MAX_LEVEL_DIGITS || !number.chars().allMatch(Character::isDigit)
				|| at >= tokens.size())
			{
				return List.of();
			}
			String identity = SkillCatalog.identity(tokens.get(at++).getText());
			int level = Integer.parseInt(number);
			if (identity == null || SkillCatalog.COMBAT.equals(identity) || !SkillCatalog.validLevel(identity, level))
			{
				return List.of();
			}
			result.add(new Entry(level, Skill.valueOf(identity)));
		}
		return Collections.unmodifiableList(result);
	}

	public static List<Entry> interpret(String source, List<RowEvidence.SkillSpan> spans)
	{
		List<Entry> structured = new ArrayList<>();
		StringJoiner text = new StringJoiner(" ");
		for (RowEvidence.SkillSpan span : spans)
		{
			List<Entry> entries = interpret(span.getText());
			if (entries.size() != 1 || !entries.get(0).getSkill().name().equals(SkillCatalog.identity(span.getSkill())))
			{
				return interpret(source);
			}
			structured.addAll(entries);
			text.add(span.getText());
		}
		return !structured.isEmpty()
			&& SourceTextNormalizer.normalize(text.toString()).equals(SourceTextNormalizer.normalize(source))
				? Collections.unmodifiableList(structured)
				: interpret(source);
	}

	private ProjectedLevelInterpreter()
	{
	}
}
