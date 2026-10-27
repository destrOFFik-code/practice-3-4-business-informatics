package ru.edu.pr04;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                runInteractive();
            } else if (args.length == 2 && "--file".equals(args[0])) {
                loadFile(Path.of(args[1]));
            } else {
                System.out.println("Запуск: Main или Main --file data/sample.csv");
            }
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void runInteractive() {
        OrderService service = new OrderService();
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            System.out.print("ID сотрудника: ");
            String id = scanner.hasNextLine() ? scanner.nextLine() : "";
            System.out.print("ФИО сотрудника: ");
            String name = scanner.hasNextLine() ? scanner.nextLine() : "";
            Order order = service.createOrder(new Employee(id, name));
            System.out.println("Команды: add id;name;price;quantity | remove id | show | confirm | exit");
            System.out.println("Количество одной позиции: " + OrderItem.MIN_QUANTITY
                    + ".." + OrderItem.MAX_QUANTITY + "; дробная цена вводится через точку.");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] command = line.split("\\s+", 2);
                String value = command.length == 2 ? command[1] : "";
                try {
                    switch (command[0].toLowerCase(Locale.ROOT)) {
                        case "add":
                            service.addItemFromCsv(order, value);
                            printOrder(order);
                            break;
                        case "remove":
                            order.removeProduct(value);
                            printOrder(order);
                            break;
                        case "show":
                            printOrder(order);
                            break;
                        case "confirm":
                            service.confirmOrder(order);
                            printOrder(order);
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

    private static void loadFile(Path path) throws IOException {
        OrderService service = new OrderService();
        Order order = service.createOrder(new Employee("E1", "Иванов Иван"));
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int start = !lines.isEmpty()
                && lines.get(0).trim().equalsIgnoreCase("productId;name;price;quantity") ? 1 : 0;
        for (int i = start; i < lines.size(); i++) {
            try {
                service.addItemFromCsv(order, lines.get(i));
                System.out.println("Строка " + (i + 1) + ": добавлена");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Строка " + (i + 1) + ": ошибка — " + e.getMessage());
            }
        }
        printOrder(order);
    }

    private static void printOrder(Order order) {
        System.out.println("Сотрудник: " + order.getEmployee().id() + " — " + order.getEmployee().fullName());
        System.out.println("Статус: " + order.getStatus());
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            System.out.printf(Locale.ROOT, "%s | %s | %.2f × %d = %.2f%n",
                    product.getId(), product.getName(), product.getPrice(),
                    item.getQuantity(), item.getLineTotal());
        }
        System.out.printf(Locale.ROOT, "Итого: %.2f%n", order.total());
    }
}
