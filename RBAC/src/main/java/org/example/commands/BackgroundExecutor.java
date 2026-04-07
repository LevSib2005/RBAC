package org.example.commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundExecutor {
    private static BackgroundExecutor instance;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;

    private BackgroundExecutor() {
        executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("bg-executor-" + System.currentTimeMillis());
            return t;
        });

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("bg-scheduler");
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

    public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduledExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public void shutdown() {
        executor.shutdown();
        scheduledExecutor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}