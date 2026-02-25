package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Util {
    private final Scanner scanner;
    private static final Random RANDOM = new Random();
    private final String PATH = "Lab1/src/main/resources/";

    public Util (Scanner scanner) {
        this.scanner = scanner;
    }

    public int[] create() {
        System.out.print("Розмір масиву -> ");
        int size = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Мінімальне значення -> ");
        int min = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Максимальне значення-> ");
        int max = scanner.nextInt();
        scanner.nextLine();
        return RANDOM.ints(size, min, max + 1).toArray();
    }

    public void print(int[] array) {
        if (array == null) {
            System.out.println("Масив порожній! Спочатку згенеруйте або прочитайте дані.");
            return;
        }

        System.out.println("Поточний масив: " + Arrays.toString(array));
    }

    public void write(int[] array) {
        if (array == null) {
            System.out.println("Помилка: Немає даних для запису.");
            return;
        }

        System.out.print("Назва файлу для збереження (напр., data.txt) -> ");
        String fileName = scanner.nextLine();

        try (PrintWriter writer = new PrintWriter(new File(PATH + fileName))) {
            for (int i = 0; i < array.length; i++) {
                writer.print(array[i]);
                if (i < array.length - 1) {
                    writer.print(" ");
                }
            }
            System.out.println("Дані успішно збережено у файл " + fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }

    public int[] read() {
        System.out.print("Назва файлу для читання -> ");
        String fileName = scanner.nextLine();

        List<Integer> list = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(new File(PATH + fileName))) {
            while (fileScanner.hasNextInt()) {
                list.add(fileScanner.nextInt());
            }
            System.out.println("Дані успішно прочитано з файлу " + fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Помилка: Файл не знайдено!");
            return null;
        }

        return list.stream().mapToInt(Integer::intValue).toArray();
    }
}
