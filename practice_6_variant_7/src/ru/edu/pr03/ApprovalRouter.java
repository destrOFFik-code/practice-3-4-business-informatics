package ru.edu.pr03;

/** Выбирает первый подходящий маршрут в порядке приоритета правил. */
public class ApprovalRouter {
    // Вариант 7: буквальное значение финансового порога — 40.
    public static final long FINANCE_LIMIT = 40L;

    public ApprovalRoute route(long amount, boolean urgent, RiskLevel risk,
                               boolean containsPersonalData) {
        if (amount < 0) {
            throw new IllegalArgumentException("сумма не может быть отрицательной");
        }
        if (risk == null) {
            throw new IllegalArgumentException("уровень риска не задан");
        }

        if (requiresSecurity(containsPersonalData)) {
            return ApprovalRoute.SECURITY_REVIEW;
        }
        if (requiresDirector(risk)) {
            return ApprovalRoute.DIRECTOR_REVIEW;
        }
        if (requiresFinance(amount)) {
            return ApprovalRoute.FINANCE_REVIEW;
        }
        if (requiresFastTrack(amount, urgent)) {
            return ApprovalRoute.FAST_TRACK;
        }
        return ApprovalRoute.STANDARD;
    }

    private boolean requiresSecurity(boolean containsPersonalData) {
        return containsPersonalData;
    }

    private boolean requiresDirector(RiskLevel risk) {
        return risk == RiskLevel.HIGH;
    }

    private boolean requiresFinance(long amount) {
        return amount >= FINANCE_LIMIT;
    }

    private boolean requiresFastTrack(long amount, boolean urgent) {
        return urgent && amount < FINANCE_LIMIT;
    }
}
