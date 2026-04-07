package org.example.commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackgroundExecutor {
    private static BackgroundExecutor instance;
    private final ExecutorService executor;

    private BackgroundExecutor() {
        executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("bg-executor-" + System.currentTimeMillis());
            return t;
        });
    }

    public static synchronized BackgroundExecutor getInstance() {
        if (instance == null) instance = new BackgroundExecutor();
        return instance;
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}