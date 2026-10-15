package com.questcape.guide;

import com.questcape.progress.Identities;
import com.questcape.progress.ActivityProgress;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.inject.Inject;

public class GuideParser
{
	private static final String CAPTION = GuideSourcePolicy.CAPTION;
	private static final List<String> HEADERS = Arrays.asList("Quest/Activity", "Quick Guide", "New levels after quest",
		"Quest points", "Total QP", "Additional info", "Location");

	@Inject
	public GuideParser()
	{
	}

	public List<GuideRow> parse(String html) throws IOException
	{
		if (html == null || html.length() > GuideSourcePolicy.MAX_CONTENT)
		{
			throw new IOException("Guide response exceeds limits");
		}
		HtmlTree.Node document = HtmlTree.parse(html);
		List<HtmlTree.Node> candidates = new ArrayList<>();
		for (HtmlTree.Node table : document.all("table"))
		{
			if (table.children("caption").stream().anyMatch(c -> Identities.normalize(c.text()).equals(CAPTION)))
			{
				candidates.add(table);
			}
		}
		if (candidates.size() != 1)
		{
			throw new IOException("Expected one captioned guide table; found " + candidates.size());
		}
		List<HtmlTree.Node> physical = candidates.get(0).all("tr");
		if (physical.size() < GuideSourcePolicy.MIN_ROWS || physical.size() > GuideSourcePolicy.MAX_ROWS)
		{
			throw new IOException("Guide table is empty or oversized");
		}
		Map<Integer, Span> spans = new HashMap<>();
		List<GuideRow> rows = new ArrayList<>();
		List<String> headers = null;
		for (HtmlTree.Node tr : physical)
		{
			List<HtmlTree.Node> cells = new ArrayList<>();
			for (Object child : tr.content)
			{
				if (child instanceof HtmlTree.Node)
				{
					HtmlTree.Node node = (HtmlTree.Node)child;
					if (node.tag.equals("td") || node.tag.equals("th"))
					{
						cells.add(node);
					}
				}
			}
			if (cells.isEmpty())
			{
				continue;
			}
			HtmlTree.Node[] grid = new HtmlTree.Node[GuideSourcePolicy.COLUMNS];
			for (Iterator<Map.Entry<Integer, Span>> it = spans.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry<Integer, Span> entry = it.next();
				grid[entry.getKey()] = entry.getValue().node;
				if (--entry.getValue().remaining == 0)
				{
					it.remove();
				}
			}
			int column = 0;
			for (HtmlTree.Node cell : cells)
			{
				while (column < GuideSourcePolicy.COLUMNS && grid[column] != null)
				{
					column++;
				}
				int colspan = span(cell, "colspan"), rowspan = span(cell, "rowspan");
				if (column + colspan > GuideSourcePolicy.COLUMNS)
				{
					throw new IOException("Unexpected guide column count");
				}
				for (int j = 0; j < colspan; j++)
				{
					if (grid[column] != null)
					{
						throw new IOException("Overlapping merged cells");
					}
					grid[column] = cell;
					if (rowspan > 1)
					{
						spans.put(column, new Span(cell, rowspan - 1));
					}
					column++;
				}
			}
			if (Arrays.stream(grid).anyMatch(Objects::isNull))
			{
				throw new IOException("Incomplete guide row");
			}
			if (headers == null)
			{
				headers = new ArrayList<>();
				for (HtmlTree.Node cell : grid)
				{
					headers.add(cell.text());
				}
				for (String header : HEADERS)
				{
					if (headers.stream().noneMatch(h -> GuideFieldRole.of(h) == GuideFieldRole.of(header)))
					{
						throw new IOException("Guide headers changed: " + headers);
					}
				}
				continue;
			}
			Map<String, String> fields = new LinkedHashMap<>(), links = new LinkedHashMap<>();
			for (int j = 0; j < GuideSourcePolicy.COLUMNS; j++)
			{
				fields.put(headers.get(j), grid[j].text());
			}
			for (HtmlTree.Node anchor : tr.all("a"))
			{
				String href = safeWikiLink(anchor.attr("href"));
				if (href != null && !anchor.text().isEmpty())
				{
					links.put(href, anchor.text());
				}
			}
			HtmlTree.Node titleCell = grid[headers.indexOf(
				headers.stream().filter(h -> GuideFieldRole.of(h) == GuideFieldRole.TITLE).findFirst().get())];
			String title = titleCell.text();
			if (title.isBlank())
			{
				throw new IOException("Empty guide action");
			}
			String wikiTarget = "";
			for (HtmlTree.Node a : titleCell.all("a"))
			{
				String href = safeWikiLink(a.attr("href"));
				if (href != null && !a.text().isBlank())
				{
					wikiTarget = wikiTitle(href);
					break;
				}
			}
			boolean fullWidth = Arrays.stream(grid).allMatch(cell -> cell == titleCell);
			Map<GuideFieldRole, Map<String, String>> cellLinks = new EnumMap<>(GuideFieldRole.class);
			Map<GuideFieldRole, List<RowEvidence.SkillSpan>> skillSpans = new EnumMap<>(GuideFieldRole.class);
			for (int j = 0; j < grid.length; j++)
			{
				Map<String, String> destinations = new LinkedHashMap<>();
				for (HtmlTree.Node anchor : grid[j].all("a"))
				{
					String safe = safeWikiLink(anchor.attr("href"));
					if (safe != null && !anchor.text().isBlank())
					{
						destinations.put(safe, anchor.text());
						links.putIfAbsent(safe, anchor.text());
					}
				}
				if (!fullWidth)
				{
					cellLinks.put(GuideFieldRole.of(headers.get(j)), destinations);
				}
				List<RowEvidence.SkillSpan> mentions = new ArrayList<>();
				for (HtmlTree.Node span : grid[j].all("span"))
				{
					String skill = SkillCatalog.identity(span.attr(RowEvidence.SKILL));
					if (skill != null && span.text().length() <= GuideSourcePolicy.ATTRIBUTE_LENGTH)
					{
						mentions.add(new RowEvidence.SkillSpan(skill, span.text()));
					}
				}
				if (!mentions.isEmpty() && (!fullWidth || j == 0))
				{
					skillSpans.put(fullWidth ? GuideFieldRole.TITLE : GuideFieldRole.of(headers.get(j)),
						List.copyOf(mentions));
				}
			}
			rows.add(classify(rows.size(), title, wikiTarget, fields, links, fullWidth,
				new RowEvidence(cellLinks, tr.attrs, skillSpans)));
		}
		if (!spans.isEmpty() || rows.isEmpty())
		{
			throw new IOException("Truncated guide table");
		}
		return distinguishRepeatedRows(rows);
	}

	/** Upgrade cached classifications offline; the stored field layout identifies full-width rows. */
	public GuideSnapshot reclassify(GuideSnapshot snapshot)
	{
		List<GuideRow> rows = new ArrayList<>();
		for (GuideRow row : snapshot.getRows())
		{
			boolean fullWidth = row.getFields().values().stream().allMatch(row.getTitle()::equals);
			rows.add(classify(row.getPosition(), row.getTitle(), row.getWikiTarget(),
				row.getFields(), row.getLinks(), fullWidth, row.getEvidence()));
		}
		return new GuideSnapshot(snapshot.getRevision(), snapshot.getRetrievedAt(), snapshot.getValidatedAt(),
			snapshot.getEtag(), snapshot.getLastModified(), distinguishRepeatedRows(rows));
	}

	/** Repair older truncated balloon titles only when retained validated links identify one destination. */
	private static GuideRow classify(int position, String title, String wikiTarget,
		Map<String, String> fields, Map<String, String> links, boolean fullWidth, RowEvidence evidence)
	{
		if (Identities.normalize(title).equals("unlock: balloon transport system to"))
		{
			Set<String> destinations = new LinkedHashSet<>();
			for (String url : links.keySet())
			{
				String safe = safeWikiLink(url);
				if (safe != null)
				{
					String candidate = "Unlock: Balloon transport system to " + wikiTitle(safe);
					if (ActivityProgress.unlock(candidate) != null)
					{
						destinations.add(candidate);
					}
				}
			}
			if (destinations.size() == 1)
			{
				String oldTitle = title;
				title = destinations.iterator().next();
				Map<String, String> repaired = new LinkedHashMap<>(fields);
				String repairedTitle = title;
				repaired.replaceAll((key, value) -> value.equals(oldTitle) ? repairedTitle : value);
				fields = repaired;
			}
		}
		return GuideAssembler.source(new SourceRow(position, title, wikiTarget, fields, links, evidence, fullWidth));
	}

	private static List<GuideRow> distinguishRepeatedRows(List<GuideRow> rows)
	{
		// Keep even identical repetitions visible, but never share a manual check between indistinguishable actions.
		Map<String, Long> counts = rows.stream()
			.collect(java.util.stream.Collectors.groupingBy(GuideRow::getKey, java.util.stream.Collectors.counting()));
		Map<String, Integer> occurrences = new HashMap<>();
		for (int i = 0; i < rows.size(); i++)
		{
			GuideRow row = rows.get(i);
			if (counts.get(row.getKey()) > 1)
			{
				String key = com.questcape.persistence.LegacyKeyCodec.repeated(row.getKey(),
					occurrences.merge(row.getKey(), 1, Integer::sum));
				rows.set(i, GuideAssembler.repeated(row, key, i));
			}
		}
		return rows;
	}

	private static int span(HtmlTree.Node node, String name) throws IOException
	{
		if (node.attr(name).isEmpty())
		{
			return 1;
		}
		try
		{
			int value = Integer.parseInt(node.attr(name));
			if (value < 1 || value > GuideSourcePolicy.MAX_SPAN)
			{
				throw new NumberFormatException();
			}
			return value;
		}
		catch (NumberFormatException e)
		{
			throw new IOException("Invalid merged cell", e);
		}
	}

	private static final class Span
	{
		final HtmlTree.Node node;
		int remaining;

		Span(HtmlTree.Node node, int remaining)
		{
			this.node = node;
			this.remaining = remaining;
		}
	}

	public static String safeWikiLink(String href)
	{
		try
		{
			URI uri = URI.create(GuideSnapshot.SOURCE).resolve(href);
			if (!GuideSourcePolicy.SECURE_SCHEME.equalsIgnoreCase(uri.getScheme())
				|| !GuideSourcePolicy.HOST.equalsIgnoreCase(uri.getHost())
				|| uri.getUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != GuideSourcePolicy.SECURE_PORT))
			{
				return null;
			}
			uri = uri.normalize();
			return URI.create(GuideSourcePolicy.SECURE_ORIGIN + (uri.getRawPath() == null ? "" : uri.getRawPath())
				+ (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery())
				+ (uri.getRawFragment() == null ? "" : "#" + uri.getRawFragment())).toASCIIString();
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private static String wikiTitle(String validatedUrl)
	{
		return URI.create(validatedUrl).getPath().replaceFirst("^/w/", "").replace('_', ' ');
	}

	public static String digest(String value)
	{
		try
		{
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder out = new StringBuilder();
			for (byte b : bytes)
			{
				out.append(String.format("%02x", b));
			}
			return out.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
