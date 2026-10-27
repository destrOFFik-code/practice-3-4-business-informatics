package ru.edu.pr01;

public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("УСПЕХ: " + name);
        } else {
            failed++;
            System.out.println("ОШИБКА: " + name);
        }
    }

    public static void main(String[] args) {
        check("базовая стоимость", OrderCalculator.calculateBase(2, 100.0) == 200.0);
        check("НДС для варианта 7 = 40 %", OrderCalculator.VAT_RATE == 40.0);
        check("итоговая стоимость для типового заказа", Math.abs(OrderCalculator.calculateTotal(10, 1000.0, 5.0) - 13300.0) < 0.001);
        check("нижняя граница количества", OrderCalculator.isValid(1, 0.01, 0.0));
        check("верхняя граница количества", OrderCalculator.isValid(10000, 5_000_000.0, 30.0));
        check("количество ниже допустимого", !OrderCalculator.isValid(0, 100.0, 0.0));
        check("количество выше допустимого", !OrderCalculator.isValid(10001, 100.0, 0.0));
        check("скидка выше допустимой", !OrderCalculator.isValid(1, 100.0, 30.01));
        check("цена ниже допустимой", !OrderCalculator.isValid(1, 0.0, 0.0));

        System.out.printf("Результат: успешно — %d, с ошибками — %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
