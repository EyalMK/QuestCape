package com.questcape.guide;

import java.io.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;

/** Inert DOM built with the JDK parser; no browser, remote image loads or extra Hub dependency. */
final class HtmlTree
{
	static final class Node
	{
		final String tag;
		final Map<String, String> attrs = new HashMap<>();
		final List<Object> content = new ArrayList<>();
		Node parent;

		Node(String tag)
		{
			this.tag = tag;
		}

		String attr(String key)
		{
			return attrs.getOrDefault(key, "");
		}

		List<Node> children(String tag)
		{
			List<Node> result = new ArrayList<>();
			for (Object item : content)
			{
				if (item instanceof Node && ((Node)item).tag.equals(tag))
				{
					result.add((Node)item);
				}
			}
			return result;
		}

		List<Node> all(String tag)
		{
			List<Node> result = new ArrayList<>();
			for (Object item : content)
			{
				if (item instanceof Node)
				{
					Node node = (Node)item;
					if (node.tag.equals(tag))
					{
						result.add(node);
					}
					result.addAll(node.all(tag));
				}
			}
			return result;
		}

		String text()
		{
			if (Arrays.asList("script", "style", "iframe", "object").contains(tag))
			{
				return "";
			}
			StringBuilder out = new StringBuilder();
			for (Object part : content)
			{
				out.append(part instanceof Node ? ((Node)part).text() : part).append(' ');
			}
			return out.toString().replace('\u00a0', ' ').trim().replaceAll("\\s+", " ");
		}
	}

	static Node parse(String html) throws IOException
	{
		Node root = new Node("root");
		new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback()
		{
			Node current = root;

			Node add(HTML.Tag tag, MutableAttributeSet attrs)
			{
				Node node = new Node(tag.toString());
				Enumeration<?> names = attrs.getAttributeNames();
				while (names.hasMoreElements())
				{
					Object name = names.nextElement();
					node.attrs.put(name.toString(), String.valueOf(attrs.getAttribute(name)));
				}
				node.parent = current;
				current.content.add(node);
				return node;
			}

			@Override
			public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int p)
			{
				current = add(t, a);
			}

			@Override
			public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int p)
			{
				add(t, a);
			}

			@Override
			public void handleEndTag(HTML.Tag t, int p)
			{
				for (Node n = current; n != root; n = n.parent)
				{
					if (n.tag.equals(t.toString()))
					{
						current = n.parent;
						break;
					}
				}
			}

			@Override
			public void handleText(char[] data, int p)
			{
				current.content.add(new String(data));
			}
		}, true);
		return root;
	}
}
