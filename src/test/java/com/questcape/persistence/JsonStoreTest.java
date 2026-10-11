package com.questcape.persistence;

import com.google.gson.Gson;
import com.questcape.guide.GuideParser;
import com.questcape.progress.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import net.runelite.client.util.Filepath;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JsonStoreTest
{
	@Test
	public void directoryConflictsAndUnreadableRootsBecomeControlledFailures() throws Exception
	{
		Path root = Files.createTempDirectory("questcape-conflict");
		Files.writeString(root.resolve("progress"), "preserve me");
		JsonStore store = TestStores.store(new Gson(), root);
		try
		{
			store.write("progress", "live:A", Map.of());
			fail();
		}
		catch (IOException expected)
		{
			assertEquals("preserve me", Files.readString(root.resolve("progress")));
		}
		JsonStore denied = new JsonStore(new Gson(), () ->
		{
			throw new SecurityException("denied");
		});
		try
		{
			denied.read("progress", "live:A", AccountProgress.class);
			fail();
		}
		catch (IOException expected)
		{
			assertTrue(expected.getCause() instanceof SecurityException);
		}
	}

	@Test
	public void unsupportedAtomicMoveAndFailedCleanupPreserveOriginalErrorAndFile() throws Exception
	{
		Path root = Files.createTempDirectory("questcape-atomic");
		Filepath directory = spy(Filepath.Unchecked.getRooted(root));
		Filepath category = spy(directory.joinSegment("progress"));
		category.createDirectories();
		Filepath destination = category.joinSegment(GuideParser.digest("live:A") + ".json");
		destination.write("original");
		Filepath temp = spy(category.createTempFile("candidate-", ".tmp"));
		doReturn(category).when(directory).joinSegment("progress");
		doReturn(temp).when(category).createTempFile("candidate-", ".tmp");
		Filepath destinationSpy = spy(destination);
		doReturn(destinationSpy).when(category).joinSegment(destination.getFileName());
		doReturn(category).when(destinationSpy).getParent();
		AtomicMoveNotSupportedException original = new AtomicMoveNotSupportedException("candidate", "destination", "unsupported");
		doThrow(original).when(temp).moveTo(any(), any(), any());
		doThrow(new IOException("cleanup failed")).when(temp).deleteIfExists();
		JsonStore store = new JsonStore(new Gson(), () -> directory);
		try
		{
			store.write("progress", "live:A", Map.of());
			fail();
		}
		catch (IOException expected)
		{
			assertSame(original, expected);
			assertEquals("original", Files.readString(root.resolve("progress").resolve(destination.getFileName())));
		}
	}

	@Test
	public void oversizedAndJsonNullCachesRemainIntact() throws Exception
	{
		Path root = Files.createTempDirectory("questcape-invalid");
		JsonStore store = TestStores.store(new Gson(), root);
		Path file = root.resolve("progress").resolve(GuideParser.digest("live:A") + ".json");
		Files.createDirectories(file.getParent());
		for (String content : List.of("null", "[]", "x".repeat(4_000_001)))
		{
			Files.writeString(file, content);
			try
			{
				store.read("progress", "live:A", AccountProgress.class);
				fail();
			}
			catch (IOException expected)
			{
				assertEquals(content, Files.readString(file));
			}
		}
	}

	@Test
	public void unknownSchemaAndInvalidJsonAreRetained() throws Exception
	{
		Path root = Files.createTempDirectory("oqg-store");
		JsonStore store = com.questcape.persistence.TestStores.store(new Gson(), root);
		Path file = root.resolve("progress").resolve(GuideParser.digest("live:A") + ".json");
		Files.createDirectories(file.getParent());
		for (String content : List.of("{\"schema\":987}", "{not json"))
		{
			Files.writeString(file, content);
			try
			{
				store.read("progress", "live:A", AccountProgress.class);
				fail();
			}
			catch (IOException expected)
			{
			}
			assertEquals(content, Files.readString(file));
		}
	}

	@Test
	public void atomicRoundtripAndCategorySeparation() throws Exception
	{
		Path root = Files.createTempDirectory("oqg-store");
		JsonStore store = com.questcape.persistence.TestStores.store(new Gson(), root);
		AccountProgress account = ProgressTest.observation("live:A", "Alice", "STANDARD", Map.of(),
			Map.of("ATTACK", 20));
		store.write("progress", "live:A", account);
		assertEquals(account, store.read("progress", "live:A", AccountProgress.class));
		assertNull(store.read("guide", "live:A", AccountProgress.class));
		try (java.util.stream.Stream<Path> paths = Files.walk(root))
		{
			assertFalse(paths.anyMatch(p -> p.toString().endsWith(".tmp")));
		}
	}
}
