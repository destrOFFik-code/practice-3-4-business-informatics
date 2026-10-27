package ru.edu.pr03;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Финансовый порог варианта 7: " + ApprovalRouter.FINANCE_LIMIT);
        if (args.length == 0) {
            try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
                System.out.println("Введите amount;urgent;risk;personal, например 39;true;LOW;false:");
                printResult(scanner.hasNextLine() ? scanner.nextLine() : null);
            }
        } else if (args.length == 1) {
            printResult(args[0]);
        } else if (args.length == 2 && "--file".equals(args[0])) {
            try {
                List<String> lines = Files.readAllLines(Path.of(args[1]), StandardCharsets.UTF_8);
                if (lines.isEmpty()) {
                    System.out.println("Ошибка: файл не содержит заявок");
                    return;
                }
                int start = lines.get(0).trim().equalsIgnoreCase("amount;urgent;risk;personal") ? 1 : 0;
                if (start == lines.size()) {
                    System.out.println("Ошибка: файл содержит только заголовок");
                    return;
                }
                for (int i = start; i < lines.size(); i++) {
                    System.out.println("Строка файла " + (i + 1) + ": " + lines.get(i));
                    printResult(lines.get(i));
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения файла: " + e.getMessage());
            }
        } else {
            System.out.println("Запуск: Main [\"39;true;LOW;false\"] или Main --file data/sample.csv");
        }
    }

    private static void printResult(String line) {
        try {
            Request request = RequestParser.parse(line);
            ApprovalRoute route = new ApprovalRouter().route(request.getAmount(), request.isUrgent(),
                    request.getRisk(), request.containsPersonalData());
            System.out.println("Маршрут: " + route + " — " + describe(route));
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка заявки: " + e.getMessage());
        }
    }

    private static String describe(ApprovalRoute route) {
        switch (route) {
            case SECURITY_REVIEW:
                return "проверка безопасности";
            case DIRECTOR_REVIEW:
                return "согласование директором";
            case FINANCE_REVIEW:
                return "финансовое согласование";
            case FAST_TRACK:
                return "ускоренный маршрут";
            case STANDARD:
                return "обычный маршрут";
            default:
                throw new IllegalArgumentException("неизвестный маршрут: " + route);
        }
    }
}
