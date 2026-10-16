package com.questcape.guide.source;

import com.questcape.guide.interpretation.SkillCatalog;
import com.questcape.guide.model.GuideRow;
import com.questcape.guide.parsing.GuideParser;
import com.questcape.progress.compatibility.Identities;
import java.net.URI;
import java.util.*;
import lombok.Value;

/** Optional additive schema-1 enrichment: safe cell links, allowlisted attributes and source skill spans. */
@Value
public class RowEvidence
{
	public static final int VERSION = 2;
	public static final String ROW_ID = "data-rowid", SKILL = "data-skill", LEVEL = "data-skill-level";
	private static final int MAX_ATTRIBUTE_LENGTH = GuideSourcePolicy.ATTRIBUTE_LENGTH;
	int version;
	Map<String, String> attributes;
	Map<GuideFieldRole, List<SkillSpan>> skillSpans;

	@Value
	public static class SkillSpan
	{
		String skill;
		String text;
	}

	Map<GuideFieldRole, Map<String, String>> cellLinks;

	public RowEvidence(Map<GuideFieldRole, Map<String, String>> links)
	{
		this(links, Collections.emptyMap());
	}

	public RowEvidence(Map<GuideFieldRole, Map<String, String>> links, Map<String, String> attrs)
	{
		this(links, attrs, Collections.emptyMap());
	}

	public RowEvidence(Map<GuideFieldRole, Map<String, String>> links, Map<String, String> attrs,
		Map<GuideFieldRole, List<SkillSpan>> spans)
	{
		version = VERSION;
		Map<GuideFieldRole, List<SkillSpan>> retained = new EnumMap<>(GuideFieldRole.class);
		spans.forEach((role, values) -> retained.put(role, List.copyOf(values)));
		skillSpans = Collections.unmodifiableMap(retained);
		Map<String, String> sanitized = new LinkedHashMap<>();
		for (String name : List.of(ROW_ID, SKILL, LEVEL))
		{
			String value = attrs.get(name);
			if (value != null && value.length() <= MAX_ATTRIBUTE_LENGTH
				&& value.codePoints().noneMatch(Character::isISOControl))
			{
				sanitized.put(name, value);
			}
		}
		attributes = Collections.unmodifiableMap(sanitized);
		Map<GuideFieldRole, Map<String, String>> copy = new EnumMap<>(GuideFieldRole.class);
		links.forEach((role, values) -> copy.put(role, Collections.unmodifiableMap(new LinkedHashMap<>(values))));
		cellLinks = Collections.unmodifiableMap(copy);
	}

	public List<SkillSpan> spans(GuideFieldRole role)
	{
		if (version != VERSION || skillSpans == null || skillSpans.get(role) == null)
		{
			return List.of();
		}
		List<SkillSpan> safe = new ArrayList<>();
		for (SkillSpan span : skillSpans.get(role))
		{
			if (span != null && SkillCatalog.identity(span.getSkill()) != null && span.getText() != null
				&& span.getText().length() <= MAX_ATTRIBUTE_LENGTH)
			{
				safe.add(span);
			}
		}
		return Collections.unmodifiableList(safe);
	}

	public String attribute(String name)
	{
		if (version != VERSION || attributes == null)
		{
			return "";
		}
		String value = attributes.get(name);
		return value != null && value.length() <= MAX_ATTRIBUTE_LENGTH
			&& value.codePoints().noneMatch(Character::isISOControl) ? value : "";
	}

	public Map<String, String> links(GuideFieldRole role, Map<String, String> retained)
	{
		Map<String, String> result = new LinkedHashMap<>();
		Map<String, String> canonical = new LinkedHashMap<>();
		retained.forEach((url, label) ->
		{
			String safe = GuideParser.safeWikiLink(url);
			if (safe != null)
			{
				canonical.putIfAbsent(safe, label);
			}
		});
		if ((version == VERSION || version == 1) && cellLinks != null && cellLinks.get(role) != null)
		{
			cellLinks.get(role).forEach((url, label) ->
			{
				String safe = GuideParser.safeWikiLink(url);
				if (safe != null && canonical.containsKey(safe))
				{
					result.put(safe, canonical.get(safe));
				}
			});
		}
		return Collections.unmodifiableMap(result);
	}

	public static Map<String, String> resolve(GuideFieldRole role, GuideRow row)
	{
		if (role == GuideFieldRole.QUICK_GUIDE && row.sourceFields().stream().noneMatch(field -> field.getRole() == role
			&& !field.getValue().isBlank() && !field.getValue().equals(row.getTitle())
			&& !Identities.normalize(field.getValue()).equals("n/a")))
		{
			return Collections.emptyMap();
		}
		RowEvidence evidence = row.getEvidence();
		if (evidence != null && (evidence.version == VERSION || evidence.version == 1) && evidence.cellLinks != null
			&& evidence.cellLinks.containsKey(role))
		{
			return evidence.links(role, row.getLinks());
		}
		Map<String, String> result = new LinkedHashMap<>();
		List<GuideRow.Field> fields = row.sourceFields();
		String value = fields.stream()
			.filter(field -> field.getRole() == role && !field.getValue().equals(row.getTitle()))
			.map(GuideRow.Field::getValue).findFirst().orElse("");
		row.getLinks().forEach((url, label) ->
		{
			String safe = GuideParser.safeWikiLink(url);
			if (safe == null)
			{
				return;
			}
			URI uri = URI.create(safe);
			String page = uri.getPath().replaceFirst("^/w/", "").replace('_', ' ');
			if (role == GuideFieldRole.QUICK_GUIDE && !value.isBlank() && !Identities.normalize(value).equals("n/a")
				&& page.toLowerCase(Locale.ROOT).endsWith("/quick guide")
				&& Identities.normalize(page.substring(0, page.lastIndexOf('/')))
					.equals(Identities.normalize(row.getWikiTarget())))
			{
				result.put(safe, label);
			}
			if (role == GuideFieldRole.LOCATION && !value.isBlank()
				&& Identities.normalize(value).equals(Identities.normalize(label)))
			{
				result.put(safe, label);
			}
			if (role == GuideFieldRole.TITLE && uri.getFragment() == null && uri.getQuery() == null
				&& !row.getWikiTarget().isBlank()
				&& Identities.normalize(page).equals(Identities.normalize(row.getWikiTarget())))
			{
				result.put(safe, label);
			}
		});
		return role == GuideFieldRole.LOCATION && result.size() != 1 ? Collections.emptyMap()
			: Collections.unmodifiableMap(result);
	}
}
