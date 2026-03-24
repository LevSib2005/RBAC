package org.example;

public class Main {
    public static void main(String[] args) {
        int numberOfThreads = 5;
        int calculationLength = 15;

        DisplayManager displayManager = new DisplayManager(numberOfThreads, calculationLength);

        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            CalculationTask task = new CalculationTask(i + 1, calculationLength, displayManager);
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for (int i = 0; i < numberOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nAll threads completed!");
    }
}