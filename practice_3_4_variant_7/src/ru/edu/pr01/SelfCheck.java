package ru.edu.pr01;

public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    public static void main(String[] args) {
        check("base", OrderCalculator.calculateBase(2, 100.0) == 200.0);
        check("variant 7 VAT = 40", OrderCalculator.VAT_RATE == 40.0);
        check("typical total", Math.abs(OrderCalculator.calculateTotal(10, 1000.0, 5.0) - 13300.0) < 0.001);
        check("lower quantity", OrderCalculator.isValid(1, 0.01, 0.0));
        check("upper quantity", OrderCalculator.isValid(10000, 5_000_000.0, 30.0));
        check("quantity below", !OrderCalculator.isValid(0, 100.0, 0.0));
        check("quantity above", !OrderCalculator.isValid(10001, 100.0, 0.0));
        check("discount above", !OrderCalculator.isValid(1, 100.0, 30.01));
        check("price below", !OrderCalculator.isValid(1, 0.0, 0.0));

        System.out.printf("Result: %d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
