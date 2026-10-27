package ru.edu.pr02;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
                System.out.printf("Введите %d значений часов через ; (от %d до %d):%n",
                        TimesheetAnalyzer.DAYS, TimesheetAnalyzer.MIN_HOURS,
                        TimesheetAnalyzer.MAX_HOURS);
                printReport(scanner.hasNextLine() ? scanner.nextLine() : null);
            }
        } else if (args.length == 1) {
            printReport(args[0]);
        } else if (args.length == 2 && "--file".equals(args[0])) {
            try {
                List<String> lines = Files.readAllLines(Path.of(args[1]), StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    System.out.println("Строка файла " + (i + 1) + ": " + lines.get(i));
                    printReport(lines.get(i));
                    System.out.println();
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения файла: " + e.getMessage());
            }
        } else {
            System.out.println("Запуск: Main [\"8;8;8;8;8\"] или Main --file data/sample.csv");
        }
    }

    private static void printReport(String line) {
        try {
            int[] hours = TimesheetAnalyzer.parseHours(line);
            System.out.println("Часы по дням: " + Arrays.toString(hours));
            System.out.println("Всего часов: " + TimesheetAnalyzer.total(hours));
            System.out.printf(Locale.ROOT, "Среднее за день: %.2f%n",
                    TimesheetAnalyzer.average(hours));
            System.out.printf("Дней с переработкой (более %d ч): %d%n",
                    TimesheetAnalyzer.DAY_NORM, TimesheetAnalyzer.overtimeDays(hours));
            System.out.printf("Недельная переработка (сверх %d ч): %d%n",
                    TimesheetAnalyzer.WEEK_NORM, TimesheetAnalyzer.weeklyOvertime(hours));
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка табеля: " + e.getMessage());
        }
    }
}
