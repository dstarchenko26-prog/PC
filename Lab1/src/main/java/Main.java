import util.Util;
import select.Select;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean run = true;

        final Util util = new Util(scanner);
        final Select select = new Select(scanner);

        int[] array = null;

        while (run) {
            System.out.print("""

                    === Меню ===
                    1 - Згенерувати набір даних
                    2 - Вивести на екран масив
                    3 - Записати у файл
                    4 - Прочитати з файлу
                    5 - Послідовний метод
                    6 - Паралельний метод
                    7 - Тестування
                    0 - Вихід
                    Оберіть дію:\s""");

            try {
                int input = scanner.nextInt();
                scanner.nextLine();

                switch (input) {
                    case 0:
                        System.out.println("Роботу завершено. До побачення!");
                        run = false;
                        break;
                    case 1:
                        array = util.create();
                        break;
                    case 2:
                        util.print(array);
                        break;
                    case 3:
                        util.write(array);
                        break;
                    case 4:
                        array = util.read();
                        break;
                    case 5:
                        select.seq(array);
                        break;
                    case 6:
                        select.par(array);
                        break;
                    case 7:
                        select.benchmark(array);
                        break;
                    default:
                        System.out.println("Невідома команда. Будь ласка, оберіть число від 0 до 7.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Помилка: введено некоректний тип даних. Будь ласка, введіть число.");
                scanner.nextLine();
            }
        }
        scanner.close();
    }
}