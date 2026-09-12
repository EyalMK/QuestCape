package com.optimalquestguide;

import com.google.gson.Gson;
import com.optimalquestguide.guide.*;
import com.optimalquestguide.integration.*;
import com.optimalquestguide.persistence.*;
import com.optimalquestguide.progress.*;
import java.nio.file.Paths;
import okhttp3.OkHttpClient;

/** Explicit developer check of public HTTP contracts; does not start RuneLite or touch an account. */
public final class RemoteContractCheck
{
    public static void main(String[] args) throws Exception
    {
        BoundedHttp http = new BoundedHttp(new OkHttpClient());
        AccountProgress data = new WikiSyncProgressProvider(http).lookup("snooze meist");
        if (data.getQuests().isEmpty() || data.getLevels().isEmpty()) throw new AssertionError("Missing progress fields");
        System.out.println("WikiSync STANDARD: quests=" + data.getQuests().size() + ", levels=" + data.getLevels().size()
            + ", serverObserved=" + data.getObservedAt() + ", retrieved=" + data.getRetrievedAt());
        GuideRepository repository = new GuideRepository(new GuideParser(), http, new JsonStore(new Gson(), Paths.get("build", "remote-check-cache")));
        GuideSnapshot guide = repository.refresh(Runnable::run, System.currentTimeMillis()).join();
        System.out.println("Wiki: revision=" + guide.getRevision() + ", rows=" + guide.getRows().size());
        http.cancel();
        // The JDK HTML parser initializes AWT. This standalone verifier owns its JVM.
        System.exit(0);
    }
}
