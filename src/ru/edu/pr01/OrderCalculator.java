package ru.edu.pr01;

public class OrderCalculator {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 10_000;
    public static final double MIN_PRICE = 0.01;
    public static final double MAX_PRICE = 5_000_000.00;
    public static final double MIN_DISCOUNT = 0.0;
    public static final double MAX_DISCOUNT = 30.0;

    // Индивидуальный вариант 7: ставка НДС равна 40%.
    public static final double VAT_RATE = 40.0;

    private OrderCalculator() {
    }

    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return quantity >= MIN_QUANTITY && quantity <= MAX_QUANTITY
                && Double.isFinite(unitPrice)
                && unitPrice >= MIN_PRICE && unitPrice <= MAX_PRICE
                && Double.isFinite(discountPercent)
                && discountPercent >= MIN_DISCOUNT && discountPercent <= MAX_DISCOUNT;
    }

    public static String validationError(int quantity, double unitPrice, double discountPercent) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            return "Ошибка: количество должно быть от 1 до 10000.";
        }
        if (!Double.isFinite(unitPrice) || unitPrice < MIN_PRICE || unitPrice > MAX_PRICE) {
            return "Ошибка: цена должна быть от 0.01 до 5000000.00 руб.";
        }
        if (!Double.isFinite(discountPercent)
                || discountPercent < MIN_DISCOUNT || discountPercent > MAX_DISCOUNT) {
            return "Ошибка: скидка должна быть от 0 до 30 процентов.";
        }
        return null;
    }

    public static double calculateBase(int quantity, double unitPrice) {
        return quantity * unitPrice;
    }

    public static double applyDiscount(double base, double discountPercent) {
        return base * (1.0 - discountPercent / 100.0);
    }

    public static double calculateVat(double discounted, double vatPercent) {
        return discounted * vatPercent / 100.0;
    }

    public static double calculateTotal(int quantity, double unitPrice, double discountPercent, double vatPercent) {
        double base = calculateBase(quantity, unitPrice);
        double discounted = applyDiscount(base, discountPercent);
        double vat = calculateVat(discounted, vatPercent);
        return discounted + vat;
    }

    public static double calculateTotal(int quantity, double unitPrice, double discountPercent) {
        return calculateTotal(quantity, unitPrice, discountPercent, VAT_RATE);
    }
}
