package ru.edu.pr05;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BusinessTripService service = new BusinessTripService();
        System.out.println("Текущая дата (Europe/Moscow): " + service.today());
        try {
            if (args.length == 0) {
                runInteractive(service);
            } else if (args.length == 2 && "--file".equals(args[0])) {
                loadFile(service, Path.of(args[1]));
            } else {
                System.out.println("Запуск: Main или Main --file data/sample.csv");
            }
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void runInteractive(BusinessTripService service) {
        System.out.println("Команды: create id;start;end | submit id | approve id | reject id;причина");
        System.out.println("cancel id | complete id | show id | list [STATUS] | exit");
        System.out.println("Даты: YYYY-MM-DD; минимум причины отклонения: "
                + BusinessTrip.MIN_REJECTION_REASON_LENGTH + " символов.");
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toLowerCase(Locale.ROOT);
                String value = parts.length == 2 ? parts[1] : "";
                try {
                    switch (command) {
                        case "create":
                            printTrip(service.createFromText(value));
                            break;
                        case "show":
                            printTrip(service.getById(value));
                            break;
                        case "list":
                            List<BusinessTrip> trips = value.isBlank() ? service.getTrips()
                                    : service.findByStatus(TripStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)));
                            for (BusinessTrip trip : trips) {
                                printTrip(trip);
                            }
                            System.out.println("Найдено: " + trips.size());
                            break;
                        case "reject":
                            String[] reject = value.split(";", 2);
                            printTrip(service.execute(reject[0], command, reject.length == 2 ? reject[1] : ""));
                            break;
                        case "submit":
                        case "approve":
                        case "cancel":
                        case "complete":
                            printTrip(service.execute(value, command, ""));
                            break;
                        case "exit":
                            return;
                        default:
                            System.out.println("Неизвестная команда");
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
        }
    }

    private static void loadFile(BusinessTripService service, Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int start = 0;
        if (!lines.isEmpty()) {
            String header = lines.get(0).trim().toLowerCase(Locale.ROOT);
            if (header.equals("id;start;end;action") || header.equals("id;start;end;action;reason")) {
                start = 1;
            }
        }
        for (int i = start; i < lines.size(); i++) {
            try {
                System.out.print("Строка " + (i + 1) + ": ");
                printTrip(service.processCsvLine(lines.get(i)));
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ошибка — " + e.getMessage());
            }
        }
        System.out.println("Всего командировок: " + service.getTrips().size());
    }

    private static void printTrip(BusinessTrip trip) {
        System.out.println(trip.getId() + " | " + trip.getStart() + ".." + trip.getEnd()
                + " | " + trip.getStatus());
        if (trip.getRejectionReason() != null) {
            System.out.println("Причина: " + trip.getRejectionReason());
        }
    }
}
