package ru.edu.pr03;

/** Значения четырёх полей заявки, полученные после разбора строки. */
public final class Request {
    private final long amount;
    private final boolean urgent;
    private final RiskLevel risk;
    private final boolean containsPersonalData;

    public Request(long amount, boolean urgent, RiskLevel risk, boolean containsPersonalData) {
        this.amount = amount;
        this.urgent = urgent;
        this.risk = risk;
        this.containsPersonalData = containsPersonalData;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public RiskLevel getRisk() {
        return risk;
    }

    public boolean containsPersonalData() {
        return containsPersonalData;
    }
}
