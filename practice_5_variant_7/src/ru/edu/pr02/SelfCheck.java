package ru.edu.pr02;

import java.util.Arrays;

/** Самопроверка без JUnit. При ошибке процесс завершается с кодом 1. */
public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    private static void checkInvalid(String name, String line, String expectedMessage) {
        try {
            TimesheetAnalyzer.parseHours(line);
            check(name + " (ожидалась ошибка)", false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage().equals(expectedMessage));
        }
    }

    public static void main(String[] args) {
        int[] normal = TimesheetAnalyzer.parseHours("8;8;8;8;8");
        check("разбор пяти целых чисел", Arrays.equals(normal, new int[]{8, 8, 8, 8, 8}));
        check("исходная проверка суммы", TimesheetAnalyzer.total(normal) == 40);
        check("среднее нормальной недели", TimesheetAnalyzer.average(normal) == 8.0);
        check("точная недельная норма", TimesheetAnalyzer.weeklyOvertime(normal) == 0);

        int[] mixed = TimesheetAnalyzer.parseHours("9;8;7;10;8");
        check("сумма смешанных значений", TimesheetAnalyzer.total(mixed) == 42);
        check("дробное среднее", Math.abs(TimesheetAnalyzer.average(mixed) - 8.4) < 1e-9);
        // В исходном SelfCheck ожидалось 2 для нормы 8. Вариант 7 меняет эту норму.
        check("исходная проверка дней с учётом варианта 7",
                TimesheetAnalyzer.overtimeDays(mixed) == 0);
        check("недельная переработка 2 часа", TimesheetAnalyzer.weeklyOvertime(mixed) == 2);

        int[] zero = TimesheetAnalyzer.parseHours("0;0;0;0;0");
        check("нижняя граница включена", Arrays.equals(zero, new int[]{0, 0, 0, 0, 0}));
        check("сумма нулей", TimesheetAnalyzer.total(zero) == 0);
        check("среднее нулей", TimesheetAnalyzer.average(zero) == 0.0);
        check("недельная переработка не отрицательна", TimesheetAnalyzer.weeklyOvertime(zero) == 0);

        int[] maximum = TimesheetAnalyzer.parseHours("16;16;16;16;16");
        check("верхняя граница включена", Arrays.equals(maximum, new int[]{16, 16, 16, 16, 16}));
        check("максимальная сумма", TimesheetAnalyzer.total(maximum) == 80);
        check("максимальное среднее", TimesheetAnalyzer.average(maximum) == 16.0);
        check("дней с переработкой при максимальных допустимых часах нет",
                TimesheetAnalyzer.overtimeDays(maximum) == 0);
        check("максимальная недельная переработка", TimesheetAnalyzer.weeklyOvertime(maximum) == 40);

        check("пробелы вокруг чисел разрешены",
                Arrays.equals(TimesheetAnalyzer.parseHours(" 8 ; 9;10 ; 0; 16 "),
                        new int[]{8, 9, 10, 0, 16}));
        check("повтор часов не является конфликтом",
                Arrays.equals(TimesheetAnalyzer.parseHours("4;4;4;4;4"), new int[]{4, 4, 4, 4, 4}));
        check("неделя ниже нормы", TimesheetAnalyzer.weeklyOvertime(new int[]{7, 8, 8, 8, 8}) == 0);
        check("неделя на один час выше нормы",
                TimesheetAnalyzer.weeklyOvertime(new int[]{9, 8, 8, 8, 8}) == 1);

        // Изолированная проверка сравнения >; эти числа не подаются в parseHours.
        int norm = TimesheetAnalyzer.DAY_NORM;
        check("дневная норма: ниже, ровно, выше",
                TimesheetAnalyzer.overtimeDays(new int[]{norm - 1, norm, norm + 1}) == 1);
        check("сумма пустого массива", TimesheetAnalyzer.total(new int[0]) == 0);
        check("среднее пустого массива", TimesheetAnalyzer.average(new int[0]) == 0.0);
        check("дни переработки пустого массива", TimesheetAnalyzer.overtimeDays(new int[0]) == 0);
        check("недельная переработка пустого массива", TimesheetAnalyzer.weeklyOvertime(new int[0]) == 0);

        checkInvalid("null", null, "пустая строка табеля");
        checkInvalid("пустая строка", "", "пустая строка табеля");
        checkInvalid("только пробелы", "   ", "пустая строка табеля");
        checkInvalid("четыре значения", "8;8;8;8", "нужно ровно 5 значений, получено 4");
        checkInvalid("шесть значений", "8;8;8;8;8;8", "нужно ровно 5 значений, получено 6");
        checkInvalid("пустой первый день", ";8;8;8;8", "позиция 1: пустое значение");
        checkInvalid("пустой день в середине", "8;8;;8;8", "позиция 3: пустое значение");
        checkInvalid("пустой последний день", "8;8;8;8;", "позиция 5: пустое значение");
        checkInvalid("пробел вместо значения", "8; ;8;8;8", "позиция 2: пустое значение");
        checkInvalid("неверный формат", "8;abc;8;8;8",
                "позиция 2: ожидалось целое число, получено «abc»");
        checkInvalid("дробные часы", "8;8;8.5;8;8",
                "позиция 3: ожидалось целое число, получено «8.5»");
        checkInvalid("переполнение int", "8;8;8;8;2147483648",
                "позиция 5: ожидалось целое число, получено «2147483648»");
        checkInvalid("отрицательные часы", "-1;8;8;8;8", "позиция 1: часы вне диапазона 0..16 (-1)");
        checkInvalid("обязательная проверка 17", "8;8;17;8;8", "позиция 3: часы вне диапазона 0..16 (17)");
        checkInvalid("дневная норма не расширяет диапазон ввода", "40;8;8;8;8",
                "позиция 1: часы вне диапазона 0..16 (40)");
        checkInvalid("лишняя точка с запятой", "8;8;8;8;8;", "нужно ровно 5 значений, получено 6");
        checkInvalid("разделитель запятая", "8,8,8,8,8", "нужно ровно 5 значений, получено 1");

        System.out.printf("Результат: PASS — %d, FAIL — %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
