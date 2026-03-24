package org.example;

import java.util.concurrent.locks.ReentrantLock;

public class DisplayManager {
    private final int numberOfThreads;
    private final int calculationLength;
    private final ProgressInfo[] progressInfo;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean initialDisplayed = false;
    private static final int BAR_WIDTH = 30;

    public DisplayManager(int numberOfThreads, int calculationLength) {
        this.numberOfThreads = numberOfThreads;
        this.calculationLength = calculationLength;
        this.progressInfo = new ProgressInfo[numberOfThreads + 1];
    }

    public void registerThread(int threadNumber, long threadId) {
        lock.lock();
        try {
            progressInfo[threadNumber] = new ProgressInfo(threadId);

            if (allThreadsRegistered()) {
                displayAllProgress();
                initialDisplayed = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public void updateProgress(int threadNumber, int currentStep) {
        lock.lock();
        try {
            if (progressInfo[threadNumber] != null) {
                progressInfo[threadNumber].currentStep = currentStep;

                if (initialDisplayed) {
                    displayAllProgress();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void finishThread(int threadNumber, long duration) {
        lock.lock();
        try {
            if (progressInfo[threadNumber] != null) {
                progressInfo[threadNumber].isFinished = true;
                progressInfo[threadNumber].duration = duration;
                displayAllProgress();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean allThreadsRegistered() {
        for (int i = 1; i <= numberOfThreads; i++) {
            if (progressInfo[i] == null) {
                return false;
            }
        }
        return true;
    }

    private void displayAllProgress() {
        clearConsole();

        System.out.println("Multi-threaded calculation\n");

        for (int i = 1; i <= numberOfThreads; i++) {
            ProgressInfo info = progressInfo[i];
            if (info != null) {
                StringBuilder line = new StringBuilder();

                line.append(String.format("Thread #%d", i));
                line.append(String.format(" (ID: %d)", info.threadId));

                if (info.isFinished) {
                    line.append(String.format(" [Completed] Time: %.2f sec", info.duration / 1000.0));
                    line.append("\n");
                } else {
                    int progress = (info.currentStep * 100) / calculationLength;
                    int filledLength = (info.currentStep * BAR_WIDTH) / calculationLength;

                    line.append(" [");
                    for (int j = 0; j < BAR_WIDTH; j++) {
                        if (j < filledLength) {
                            line.append("#");
                        } else if (j == filledLength && info.currentStep < calculationLength) {
                            line.append(">");
                        } else {
                            line.append(".");
                        }
                    }
                    line.append(String.format("] %3d%% (%d/%d)", progress, info.currentStep, calculationLength));
                    line.append("\n");
                }

                System.out.print(line.toString());
            } else {
                System.out.printf("Thread #%d (ID: waiting...) [%s]   0%%\n", i, getEmptyBar());
            }
        }

        System.out.println("\nCalculation in progress...");

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String getEmptyBar() {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < BAR_WIDTH; i++) {
            bar.append(".");
        }
        return bar.toString();
    }

    private void clearConsole() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    private static class ProgressInfo {
        final long threadId;
        int currentStep;
        boolean isFinished;
        long duration;

        ProgressInfo(long threadId) {
            this.threadId = threadId;
            this.currentStep = 0;
            this.isFinished = false;
            this.duration = 0;
        }
    }
}