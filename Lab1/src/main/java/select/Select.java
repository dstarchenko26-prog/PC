package select;

import select.parallelism.ParBucketSelect;
import select.sequence.BucketSelect;

import java.util.Arrays;
import java.util.Scanner;

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

            printResult("Послідовно", k, result, start, end);
        } catch (Exception e) {
            System.out.println("\nПомилка: " + e.getMessage());
        }
    }

    public void par(int[] array) {
        if (!isValid(array)) return;
        int k = getK(array.length);
        if (k == -1) return;

        try {
            long start = System.nanoTime();
            SelectionResult result = ParBucketSelect.select(array, k);
            long end = System.nanoTime();
            printResult("Паралельно", k, result, start, end);
        } catch (Exception e) {
            System.out.println("\nПомилка: " + e.getMessage());
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

    private void printResult(String methodName, int k, SelectionResult result, long start, long end) {
        double timeMs = (end - start) / 1_000_000.0;
        System.out.println("\n--- Результат пошуку (" + methodName + ") ---");
        System.out.println(k + "-й найбільший елемент: " + result.value);
        System.out.println("Унікальних елементів: " + result.totalUnique);
        System.out.printf("Час виконання: %.3f мс\n", timeMs);
    }
}
