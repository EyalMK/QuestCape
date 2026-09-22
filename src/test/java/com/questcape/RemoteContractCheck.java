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
        if (args.length != 0) throw new IllegalArgumentException("This verifier accepts no arguments.");
        GuideRepository repository = new GuideRepository(new GuideParser(), http, new JsonStore(new Gson(), Paths.get("build", "remote-check-cache")));
        GuideSnapshot guide = repository.refresh(Runnable::run, System.currentTimeMillis()).join();
        System.out.println("Wiki: revision=" + guide.getRevision() + ", rows=" + guide.getRows().size());
        http.cancel();
        // The JDK HTML parser initializes AWT. This standalone verifier owns its JVM.
        System.exit(0);
    }
}
