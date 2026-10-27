package ru.edu.pr02;

public class TimesheetAnalyzer {
    public static final int DAYS = 5;
    public static final int MIN_HOURS = 0;
    public static final int MAX_HOURS = 16;
    public static final int DAY_NORM = 40;
    public static final int WEEK_NORM = 40;

    public static int[] parseHours(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("пустая строка табеля");
        }

        String[] parts = line.split(";", -1);
        if (parts.length != DAYS) {
            throw new IllegalArgumentException(
                    "нужно ровно " + DAYS + " значений, получено " + parts.length);
        }

        int[] hours = new int[DAYS];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            int position = i + 1;
            if (part.isEmpty()) {
                throw new IllegalArgumentException("позиция " + position + ": пустое значение");
            }

            int value;
            try {
                value = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "позиция " + position + ": ожидалось целое число, получено «" + part + "»");
            }

            if (value < MIN_HOURS || value > MAX_HOURS) {
                throw new IllegalArgumentException(
                        "позиция " + position + ": часы вне диапазона "
                                + MIN_HOURS + ".." + MAX_HOURS + " (" + value + ")");
            }
            hours[i] = value;
        }
        return hours;
    }

    public static int total(int[] hours) {
        int sum = 0;
        for (int i = 0; i < hours.length; i++) {
            sum += hours[i];
        }
        return sum;
    }

    public static double average(int[] hours) {
        if (hours.length == 0) {
            return 0.0;
        }
        return (double) total(hours) / hours.length;
    }

    public static int overtimeDays(int[] hours) {
        int count = 0;
        for (int i = 0; i < hours.length; i++) {
            if (hours[i] > DAY_NORM) {
                count++;
            }
        }
        return count;
    }

    public static int weeklyOvertime(int[] hours) {
        int extra = total(hours) - WEEK_NORM;
        return extra > 0 ? extra : 0;
    }
}
