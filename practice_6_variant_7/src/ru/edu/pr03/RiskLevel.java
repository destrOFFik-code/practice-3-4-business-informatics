package ru.edu.pr03;

import java.util.Locale;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH;

    public static RiskLevel parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("уровень риска не задан");
        }
        try {
            return RiskLevel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "неверный уровень риска «" + raw.trim() + "»: ожидается LOW, MEDIUM или HIGH");
        }
    }
}
