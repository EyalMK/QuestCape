package com.questcape.guide.interpretation;

import com.questcape.guide.interpretation.TrainingInstruction.Target;
import java.util.*;
import net.runelite.api.Skill;

/** Required clauses and reviewed explanatory suffixes are different parts of a training instruction. */
public final class TrainingTargetInterpreter
{
	private final List<SourceTextTokenizer.Token> words;
	private int at;

	private TrainingTargetInterpreter(String source)
	{
		words = SourceTextTokenizer.words(withoutRecommendations(source));
	}

	public static TrainingInstruction interpret(String source)
	{
		TrainingTargetInterpreter parser = new TrainingTargetInterpreter(source);
		Map<String, Target> targets = new LinkedHashMap<>();
		boolean unresolved = !parser.take("train");
		while (!unresolved && parser.at < parser.words.size())
		{
			List<String> names = new ArrayList<>();
			String name = parser.word();
			if (name == null)
			{
				unresolved = true;
				break;
			}
			names.add(name);
			while (parser.peek(",") || parser.peek("and") || parser.peek("&"))
			{
				parser.at++;
				if (parser.peek("and"))
				{
					parser.at++;
				}
				name = parser.word();
				if (name == null)
				{
					unresolved = true;
					break;
				}
				names.add(name);
			}
			Integer base = null;
			if (parser.take("from"))
			{
				parser.take("level");
				base = parser.number();
				if (base == null)
				{
					unresolved = true;
					break;
				}
			}
			parser.take("level");
			if (!parser.take("to"))
			{
				unresolved = true;
				break;
			}
			parser.take("level");
			Integer goal = parser.number();
			if (goal == null)
			{
				unresolved = true;
				break;
			}
			for (String raw : names)
			{
				String skill = SkillCatalog.identity(raw);
				String identity = skill == null ? Character.toUpperCase(raw.charAt(0)) + raw.substring(1) : skill;
				boolean valid = SkillCatalog.validLevel(skill, goal);
				Target target = new Target(identity,
					valid && base != null && SkillCatalog.validLevel(skill, base) && base < goal ? base : null, goal);
				Target previous = targets.putIfAbsent(identity, target);
				unresolved |= !valid || previous != null && !previous.equals(target);
			}
			if (parser.take("and") || parser.take(",") || parser.take("&"))
			{
				parser.take("train");
				continue;
			}
			if (parser.explanation(targets.values()))
			{
				break;
			}
			while (parser.take(".") || parser.take(";"))
			{
			}
			if (parser.at < parser.words.size())
			{
				unresolved = true;
			}
			break;
		}
		return new TrainingInstruction(Collections.unmodifiableList(new ArrayList<>(targets.values())),
			SkillCatalog.tokens(source), unresolved || targets.isEmpty());
	}

	private boolean explanation(Collection<Target> targets)
	{
		if (targets.size() != 1)
		{
			return false;
		}
		Target target = targets.iterator().next();
		int start = at;
		if (target.key() != null && target.key().getSkill() == Skill.PRAYER && take("for") && take("protect")
			&& take("from")
			&& (take("magic") || take("missiles") || take("melee")))
		{
			take(".");
			if (at == words.size())
			{
				return true;
			}
		}
		at = start;
		if (target.key() != null && target.key().getSkill() == Skill.FARMING && take("-") && take("you") && take("can")
			&& take("work")
			&& take("on"))
		{
			take("the");
			if (number() != null && take("other") && take("skills") && take("while"))
			{
				take("letting");
				if (take("crops") && take("grow"))
				{
					take(".");
					if (at == words.size())
					{
						return true;
					}
				}
			}
		}
		at = start;
		return false;
	}

	private boolean peek(String value)
	{
		return at < words.size() && words.get(at).getText().equals(value);
	}

	private boolean take(String value)
	{
		if (!peek(value))
		{
			return false;
		}
		at++;
		return true;
	}

	private String word()
	{
		if (at >= words.size() || !words.get(at).getText().codePoints().allMatch(Character::isLetter))
		{
			return null;
		}
		return words.get(at++).getText();
	}

	private Integer number()
	{
		if (at >= words.size())
		{
			return null;
		}
		String value = words.get(at).getText();
		if (value.length() > 3 || !value.chars().allMatch(Character::isDigit))
		{
			return null;
		}
		at++;
		return Integer.valueOf(value);
	}

	private static String withoutRecommendations(String source)
	{
		StringBuilder result = new StringBuilder();
		int depth = 0;
		for (char value : source.toCharArray())
		{
			if (value == '(')
			{
				depth++;
			}
			else if (value == ')' && depth > 0)
			{
				depth--;
			}
			else if (depth == 0)
			{
				result.append(value);
			}
		}
		return depth == 0 ? result.toString() : source;
	}
}
