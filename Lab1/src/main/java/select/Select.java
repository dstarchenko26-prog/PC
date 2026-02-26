package select;

import select.parallelism.ParBucketSelect;
import select.sequence.BucketSelect;

import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class Select {
    private final Scanner scanner;

    public Select (Scanner scanner) {
        this.scanner = scanner;
    }

    public void seq(int[] array) {
        if (!isValid(array)) return;
        int k = getK(array.length);
        if (k == -1) return;

        try {
            long start = System.nanoTime();
            SelectionResult result = BucketSelect.select(array, k);
            long end = System.nanoTime();

            printResult("Послідовно", k, result, start, end, 0);
        } catch (Exception e) {
            System.out.println("\nПомилка: " + e.getMessage());
        }
    }

    public void  seq(int[] array, int k) {
        try {
            long start = System.nanoTime();
            SelectionResult result = BucketSelect.select(array, k);
            long end = System.nanoTime();

            printResult("Послідовно", k, result, start, end, 0);
        } catch (Exception e) {
            System.out.println("\nПомилка: " + e.getMessage());
        }
    }

    public void par(int[] array) {
        if (!isValid(array)) return;
        int k = getK(array.length);
        if (k == -1) return;

        int t = 15;

        try {
            ForkJoinPool customPool = new ForkJoinPool(t);

            long start = System.nanoTime();
            SelectionResult result = customPool.submit(() ->
                    ParBucketSelect.select(array, k)
            ).get();
//            SelectionResult result = ParBucketSelect.select(array, k);
            long end = System.nanoTime();
            printResult("Паралельно", k, result, start, end, t);
        } catch (Exception e) {
            System.out.println("\nПомилка: " + e.getMessage());
        }
    }

    public void par(int[] array, int t, int k) {
        try {
            ForkJoinPool customPool = new ForkJoinPool(t);

            long start = System.nanoTime();
            SelectionResult result = customPool.submit(() ->
                    ParBucketSelect.select(array, k)
            ).get();
            long end = System.nanoTime();
            printResult("Паралельно", k, result, start, end, t);
        } catch (Exception e) {
            System.out.println("\nПомилка: " + e.getMessage());
        }
    }

    public void benchmark(int[] array) {
        int k = 16;
        seq(array, k);

        for (int i = 1; i <= 32; i = i * 2) {
            par(array, i, k);
        }
    }

    private boolean isValid(int[] array) {
        if (array == null || array.length == 0) {
            System.out.println("Помилка: Масив порожній! Спочатку згенеруйте або прочитайте дані.");
            return false;
        }
        return true;
    }

    private int getK(int length) {
        System.out.print("Введіть k (від 1 до " + length + ") -> ");
        int k = scanner.nextInt();
        scanner.nextLine();

        if (k < 1 || k > length) {
            System.out.println("Помилка: k виходить за межі розміру масиву.");
            return -1;
        }
        return k;
    }

    private void printResult(String methodName, int k, SelectionResult result, long start, long end, int t) {
        double timeMs = (end - start) / 1_000_000.0;
        System.out.println("\n--- Результат пошуку (" + methodName + ") ---");
        if (t != 0) {
            System.out.println("Виділено потоків: " + t);
        }
        System.out.println(k + "-й найбільший елемент: " + result.value);
        System.out.println("Унікальних елементів: " + result.totalUnique);
        System.out.printf("Час виконання: %.3f мс\n", timeMs);
    }
}
