package com.questcape.guide;

import java.util.*;
import java.net.URI;
import com.questcape.progress.Identities;
import lombok.Value;

/** Optional additive schema-1 enrichment; only safe cell link associations are persisted. */
@Value
public class RowEvidence
{
	public static final int VERSION = 1;
	int version;
	Map<GuideFieldRole, Map<String, String>> cellLinks;

	public RowEvidence(Map<GuideFieldRole, Map<String, String>> links)
	{
		version = VERSION;
		Map<GuideFieldRole, Map<String, String>> copy = new EnumMap<>(GuideFieldRole.class);
		links.forEach((role, values) -> copy.put(role, Collections.unmodifiableMap(new LinkedHashMap<>(values))));
		cellLinks = Collections.unmodifiableMap(copy);
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
		if (version == VERSION && cellLinks != null && cellLinks.get(role) != null)
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

	static Map<String, String> resolve(GuideFieldRole role, GuideRow row)
	{
		if (role == GuideFieldRole.QUICK_GUIDE && row.sourceFields().stream().noneMatch(field -> field.getRole() == role
			&& !field.getValue().isBlank() && !field.getValue().equals(row.getTitle())
			&& !Identities.normalize(field.getValue()).equals("n/a")))
		{
			return Collections.emptyMap();
		}
		RowEvidence evidence = row.getEvidence();
		if (evidence != null && evidence.version == VERSION && evidence.cellLinks != null
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
