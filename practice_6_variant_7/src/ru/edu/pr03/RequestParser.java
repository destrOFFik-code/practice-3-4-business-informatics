package ru.edu.pr03;

public class RequestParser {
    public static Request parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("пустая строка заявки");
        }
        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "нужно ровно 4 поля amount;urgent;risk;personal, получено " + parts.length);
        }

        String rawAmount = parts[0].trim();
        if (rawAmount.isEmpty()) {
            throw new IllegalArgumentException("поле amount (позиция 1): пустое значение");
        }
        long amount;
        try {
            amount = Long.parseLong(rawAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "поле amount (позиция 1): ожидается целое число в диапазоне long");
        }
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "поле amount (позиция 1): сумма не может быть отрицательной");
        }

        boolean urgent = parseBoolean(parts[1], "urgent", 2);
        RiskLevel risk;
        try {
            risk = RiskLevel.parse(parts[2]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("поле risk (позиция 3): " + e.getMessage());
        }
        boolean personal = parseBoolean(parts[3], "personal", 4);
        return new Request(amount, urgent, risk, personal);
    }

    private static boolean parseBoolean(String raw, String field, int position) {
        String value = raw.trim();
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("поле " + field + " (позиция " + position
                + "): ожидается true или false");
    }
}
