package com.questcape;

import com.google.gson.Gson;
import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.persistence.*;
import java.nio.file.Paths;
import okhttp3.OkHttpClient;

/** Explicit developer check of public HTTP contracts; does not start RuneLite or touch an account. */
public final class RemoteContractCheck
{
	public static void main(String[] args) throws Exception
	{
		BoundedHttp http = new BoundedHttp(new OkHttpClient());
		if (args.length != 0)
		{
			throw new IllegalArgumentException("This verifier accepts no arguments.");
		}
		GuideRepository repository = new GuideRepository(new GuideParser(), http,
			com.questcape.persistence.TestStores.store(new Gson(), Paths.get("build", "remote-check-cache")));
		GuideSnapshot guide = repository.refresh(Runnable::run, System.currentTimeMillis()).join();
		System.out.println("Wiki: revision=" + guide.getRevision() + ", rows=" + guide.getRows().size());
		System.out.println("Structured rows: ids="
			+ guide.getRows().stream().filter(row -> !row.getEvidence().attribute(RowEvidence.ROW_ID).isBlank()).count()
			+ ", skill targets=" + guide.getRows().stream()
				.filter(row -> !row.getEvidence().attribute(RowEvidence.LEVEL).isBlank()).count());
		for (GuideRow row : guide.getRows())
		{
			if (row.getKind() == GuideRow.Kind.DIARY || row.getKind() == GuideRow.Kind.MINIQUEST
				|| !row.isManual() && (row.getKind() == GuideRow.Kind.UNLOCK || row.getKind() == GuideRow.Kind.ACTIVITY))
			{
				String identity = row.getKind() == GuideRow.Kind.MINIQUEST
					? row.getQuestIdentity() : row.getActivityIdentity();
				if (identity == null || row.isManual())
				{
					throw new IllegalStateException("Unmapped automatic activity: " + row.getTitle());
				}
				System.out.println(row.getTitle() + " -> " + identity);
			}
		}
		http.cancel();
		// The JDK HTML parser initializes AWT. This standalone verifier owns its JVM.
		System.exit(0);
	}
}
