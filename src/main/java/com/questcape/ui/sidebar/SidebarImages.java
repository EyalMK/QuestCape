package com.questcape.ui.sidebar;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.*;

/** Classpath decoding is worker-owned and uses memory-backed image input only. */
public final class SidebarImages
{
	private SidebarImages()
	{
	}

	public static BufferedImage load(String resource) throws IOException
	{
		try (InputStream stream = SidebarImages.class.getResourceAsStream(resource))
		{
			if (stream == null)
			{
				throw new IOException("Missing sidebar resource");
			}
			try (ImageInputStream input = new MemoryCacheImageInputStream(stream))
			{
				Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
				if (!readers.hasNext())
				{
					throw new IOException("Unsupported sidebar image");
				}
				ImageReader reader = readers.next();
				try
				{
					reader.setInput(input);
					return reader.read(0);
				}
				finally
				{
					reader.dispose();
				}
			}
		}
	}
}
