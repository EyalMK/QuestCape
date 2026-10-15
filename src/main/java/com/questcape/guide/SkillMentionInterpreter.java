package com.questcape.guide;

import java.util.*;

/** Decoration preserves original offsets; it never creates completion requirements. */
public final class SkillMentionInterpreter
{
	public static List<TrainingInstruction.Token> interpret(String source)
	{
		List<TrainingInstruction.Token> result = new ArrayList<>();
		for (SourceTextTokenizer.Token token : SourceTextTokenizer.words(source))
		{
			String identity = SkillCatalog.identity(token.getText());
			if (identity != null && !SkillCatalog.COMBAT.equals(identity))
			{
				result.add(new TrainingInstruction.Token(token.getStart(), token.getEnd(), identity));
			}
		}
		return Collections.unmodifiableList(result);
	}

	private SkillMentionInterpreter()
	{
	}
}
