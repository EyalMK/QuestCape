package com.questcape;

import com.questcape.integration.http.PluginMetadata;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

public class PackagingTest
{
	@Test
	public void distributionHasOnlyOwnJava11ClassesAndLoadsPackagedResources() throws Exception
	{
		Path path = Paths.get(System.getProperty("questcape.distributionJar"));
		try (JarFile jar = new JarFile(path.toFile()))
		{
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class"))
				{
					assertTrue(entry.getName(), entry.getName().startsWith("com/questcape/"));
					try (DataInputStream in = new DataInputStream(jar.getInputStream(entry)))
					{
						assertEquals(0xCAFEBABE, in.readInt());
						in.readUnsignedShort();
						assertEquals(55, in.readUnsignedShort());
					}
				}
				assertFalse(entry.getName().contains("fixtures/"));
			}
			String metadataResource = PluginMetadata.RESOURCE.substring(1);
			for (String name : List.of("LICENSE", "THIRD-PARTY-NOTICES.md", metadataResource))
			{
				assertNotNull(jar.getEntry(name));
			}
			Properties metadata = new Properties();
			try (InputStream in = jar.getInputStream(jar.getEntry(metadataResource)))
			{
				metadata.load(in);
			}
			assertEquals(System.getProperty("questcape.version"), metadata.getProperty("version"));
			Properties descriptor = new Properties();
			try (InputStream in = Files.newInputStream(Paths.get(System.getProperty("questcape.descriptor"))))
			{
				descriptor.load(in);
			}
			assertEquals(descriptor.getProperty("displayName"), metadata.getProperty("displayName"));
			assertEquals(descriptor.getProperty("version"), metadata.getProperty("version"));
			for (String pluginClass : descriptor.getProperty("plugins").split(","))
			{
				assertNotNull("Registered plugin class must be packaged",
					jar.getEntry(pluginClass.trim().replace('.', '/') + ".class"));
			}
		}
		try (URLClassLoader loader = new URLClassLoader(new URL[]
		{ path.toUri().toURL() }, ClassLoader.getPlatformClassLoader()))
		{
			for (String resource : List.of("quest-route-icon.png", "github-mark.png", "theoatrix.properties"))
			{
				try (InputStream in = loader.getResourceAsStream(resource))
				{
					assertNotNull(resource, in);
					assertTrue(in.read() >= 0);
				}
			}
			try (InputStream in = loader.getResourceAsStream("quest-route-icon.png"))
			{
				java.awt.image.BufferedImage icon = ImageIO.read(in);
				assertNotNull("Navigation icon must decode from the packaged JAR", icon);
				assertEquals(32, icon.getWidth());
				assertEquals(32, icon.getHeight());
				assertTrue("Navigation icon needs transparent edges", icon.getColorModel().hasAlpha());
				assertEquals(0, icon.getRGB(0, 0) >>> 24);
				assertTrue("Navigation icon must contain visible artwork", (icon.getRGB(16, 16) >>> 24) > 0);
			}
			try (InputStream in = loader.getResourceAsStream("github-mark.png"))
			{
				java.awt.image.BufferedImage icon = ImageIO.read(in);
				assertNotNull(icon);
				assertEquals(20,icon.getWidth()); assertEquals(20,icon.getHeight());
				assertTrue(icon.getColorModel().hasAlpha());
			}
		}
	}
}
