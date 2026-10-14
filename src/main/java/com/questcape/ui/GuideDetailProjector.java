package com.questcape.ui;

import com.questcape.guide.*;
import java.util.*;
import lombok.Value;

/** Pure source presentation; remote text remains data and safe destinations retain their roles. */
@Value
final class GuideDetailProjector
{
	@Value
	static class Field
	{
		GuideFieldRole role;
		String label;
		String value;
	}

	@Value
	static class Link
	{
		String url;
		String label;
	}

	Link primary;
	List<Field> fields;
	List<Link> location;
	List<Link> related;
	List<Field> raw;

	static GuideDetailProjector of(GuideRow row)
	{
		Map<GuideFieldRole, List<Field>> grouped = new EnumMap<>(GuideFieldRole.class);
		List<Field> raw = new ArrayList<>();
		row.sourceFields().forEach(source ->
		{
			String label = source.getLabel(), value = source.getValue();
			GuideFieldRole role = source.getRole();
			if (!value.isBlank() && !value.equals(row.getTitle()))
			{
				Field field = new Field(role, label, value);
				if (role == GuideFieldRole.UNKNOWN)
				{
					raw.add(field);
				}
				else
				{
					grouped.computeIfAbsent(role, ignored -> new ArrayList<>()).add(field);
				}
			}
		});
		Map<String, String> safe = new LinkedHashMap<>();
		row.getLinks().forEach((url, label) ->
		{
			String destination = GuideParser.safeWikiLink(url);
			if (destination != null)
			{
				safe.putIfAbsent(destination, label);
			}
		});
		Map<String, String> quick = row.links(GuideFieldRole.QUICK_GUIDE);
		Link primary = quick.size() == 1 ? new Link(quick.keySet().iterator().next(), UiCopy.QUICK_GUIDE) : null;
		if (primary == null)
		{
			raw.addAll(grouped.getOrDefault(GuideFieldRole.QUICK_GUIDE, List.of()));
		}
		Map<String, String> locations = row.links(GuideFieldRole.LOCATION);
		List<Field> fields = new ArrayList<>();
		for (GuideFieldRole role : List.of(GuideFieldRole.ADVICE, GuideFieldRole.PROJECTIONS,
			GuideFieldRole.QUEST_POINTS, GuideFieldRole.GUIDE_QP, GuideFieldRole.LOCATION))
		{
			fields.addAll(grouped.getOrDefault(role, List.of()));
		}
		List<Link> related = new ArrayList<>();
		safe.forEach((url, label) ->
		{
			if (primary != null && url.equals(primary.getUrl()) || locations.containsKey(url))
			{
				return;
			}
			String display = (row.getKind() == GuideRow.Kind.QUEST || row.getKind() == GuideRow.Kind.MINIQUEST)
				&& row.links(GuideFieldRole.TITLE).containsKey(url) ? UiCopy.QUEST_PAGE : label + " ↗";
			if (row.getKind() == GuideRow.Kind.TRAINING && SkillCatalog.identity(label) != null)
			{
				display = "Wiki: " + label + " ↗";
			}
			related.add(new Link(url, display));
		});
		return new GuideDetailProjector(primary, Collections.unmodifiableList(fields), links(locations),
			Collections.unmodifiableList(related), Collections.unmodifiableList(raw));
	}

	private static List<Link> links(Map<String, String> source)
	{
		List<Link> links = new ArrayList<>();
		source.forEach((url, label) -> links.add(new Link(url, label + " ↗")));
		return Collections.unmodifiableList(links);
	}
}
