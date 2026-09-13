package com.questcape;

import com.google.gson.Gson;
import com.questcape.guide.*;
import com.questcape.integration.*;
import com.questcape.persistence.*;
import com.questcape.progress.*;
import java.nio.file.Paths;
import okhttp3.OkHttpClient;

/** Explicit developer check of public HTTP contracts; does not start RuneLite or touch an account. */
public final class RemoteContractCheck
{
    public static void main(String[] args) throws Exception
    {
        BoundedHttp http = new BoundedHttp(new OkHttpClient());
        if (args.length > 1) throw new IllegalArgumentException("Supply at most one player name.");
        if (args.length == 1)
        {
            AccountProgress data = new WikiSyncProgressProvider(http).lookup(args[0]);
            if (data.getQuests().isEmpty() || data.getLevels().isEmpty()) throw new AssertionError("Missing progress fields");
            System.out.println("WikiSync STANDARD: quests=" + data.getQuests().size() + ", levels=" + data.getLevels().size()
                + ", serverObserved=" + data.getObservedAt() + ", retrieved=" + data.getRetrievedAt());
        }
        else System.out.println("WikiSync check skipped; supply -PwikiSyncPlayer to choose an account explicitly.");
        GuideRepository repository = new GuideRepository(new GuideParser(), http, new JsonStore(new Gson(), Paths.get("build", "remote-check-cache")));
        GuideSnapshot guide = repository.refresh(Runnable::run, System.currentTimeMillis()).join();
        System.out.println("Wiki: revision=" + guide.getRevision() + ", rows=" + guide.getRows().size());
        http.cancel();
        // The JDK HTML parser initializes AWT. This standalone verifier owns its JVM.
        System.exit(0);
    }
}
