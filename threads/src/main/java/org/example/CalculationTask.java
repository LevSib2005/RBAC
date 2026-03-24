package org.example;

import java.util.concurrent.TimeUnit;

public class CalculationTask implements Runnable {
    private final int threadNumber;
    private final int calculationLength;
    private final DisplayManager displayManager;

    public CalculationTask(int threadNumber, int calculationLength, DisplayManager displayManager) {
        this.threadNumber = threadNumber;
        this.calculationLength = calculationLength;
        this.displayManager = displayManager;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();

        displayManager.registerThread(threadNumber, threadId);

        for (int step = 1; step <= calculationLength; step++) {
            try {
                TimeUnit.MILLISECONDS.sleep(300);

                displayManager.updateProgress(threadNumber, step);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        displayManager.finishThread(threadNumber, duration);
    }
}