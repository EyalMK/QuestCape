package com.questcape.guide;

import java.util.*;
import lombok.Value;

/** Sanitized source/cell evidence is distinct from the eventual domain action. */
@Value
public class SourceRow
{
	int position;
	String title;
	String wikiTarget;
	Map<String, String> fields;
	Map<String, String> links;
	RowEvidence evidence;
	boolean fullWidth;

	public SourceRow(int position, String title, String wikiTarget, Map<String, String> fields,
		Map<String, String> links, RowEvidence evidence, boolean fullWidth)
	{
		this.position = position;
		this.title = Objects.requireNonNull(title);
		this.wikiTarget = Objects.requireNonNull(wikiTarget);
		this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
		this.links = Collections.unmodifiableMap(new LinkedHashMap<>(links));
		this.evidence = evidence == null ? new RowEvidence(Collections.emptyMap()) : evidence;
		this.fullWidth = fullWidth;
	}

	public String attribute(String name)
	{
		return evidence.attribute(name);
	}
}
