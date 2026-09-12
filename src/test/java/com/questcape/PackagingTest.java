package com.questcape;

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
    @Test public void distributionHasOnlyOwnJava11ClassesAndLoadsPackagedResources() throws Exception
    {
        Path path = Paths.get("build/libs/questcape-0.1.0.jar");
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
                    { assertEquals(0xCAFEBABE, in.readInt()); in.readUnsignedShort(); assertEquals(55, in.readUnsignedShort()); }
                }
                assertFalse(entry.getName().contains("fixtures/"));
            }
            for (String name : List.of("LICENSE", "THIRD-PARTY-NOTICES.md", "runelite-plugin.properties")) assertNotNull(jar.getEntry(name));
            Properties metadata = new Properties();
            try (InputStream in = jar.getInputStream(jar.getEntry("runelite-plugin.properties"))) { metadata.load(in); }
            for (String pluginClass : metadata.getProperty("plugins").split(","))
                assertNotNull("Registered plugin class must be packaged", jar.getEntry(pluginClass.trim().replace('.', '/') + ".class"));
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[]{path.toUri().toURL()}, ClassLoader.getPlatformClassLoader()))
        {
            for (String resource : List.of("quest-route-icon.png", "theoatrix.properties"))
                try (InputStream in = loader.getResourceAsStream(resource)) { assertNotNull(resource, in); assertTrue(in.read() >= 0); }
            try (InputStream in = loader.getResourceAsStream("quest-route-icon.png"))
            {
                java.awt.image.BufferedImage icon = ImageIO.read(in);
                assertNotNull("Navigation icon must decode from the packaged JAR", icon);
                assertEquals(32, icon.getWidth()); assertEquals(32, icon.getHeight());
                assertTrue("Navigation icon needs transparent edges", icon.getColorModel().hasAlpha());
                assertEquals(0, icon.getRGB(0, 0) >>> 24);
                assertTrue("Navigation icon must contain visible artwork", (icon.getRGB(16, 16) >>> 24) > 0);
            }
        }
    }
}
