package ru.edu.pr01;

import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Введите количество: ");
            int quantity = scanner.nextInt();

            System.out.print("Введите цену одной единицы: ");
            double unitPrice = scanner.nextDouble();

            System.out.print("Введите скидку, %: ");
            double discountPercent = scanner.nextDouble();

            String error = OrderCalculator.validationError(quantity, unitPrice, discountPercent);
            if (error != null) {
                System.out.println(error);
                return;
            }

            double base = OrderCalculator.calculateBase(quantity, unitPrice);
            double discounted = OrderCalculator.applyDiscount(base, discountPercent);
            double vat = OrderCalculator.calculateVat(discounted, OrderCalculator.VAT_RATE);
            double total = OrderCalculator.calculateTotal(quantity, unitPrice, discountPercent);

            System.out.printf("Базовая сумма: %.2f руб.%n", base);
            System.out.printf("После скидки: %.2f руб.%n", discounted);
            System.out.printf("НДС (%.0f%%): %.2f руб.%n", OrderCalculator.VAT_RATE, vat);
            System.out.printf("Итого: %.2f руб.%n", total);
        } catch (java.util.InputMismatchException e) {
            System.out.println("Ошибка: введено значение неверного формата.");
        } finally {
            scanner.close();
        }
    }
}
