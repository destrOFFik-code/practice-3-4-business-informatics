package ru.edu.pr03;

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

    private static void checkRoute(String name, long amount, boolean urgent, RiskLevel risk,
                                   boolean personal, ApprovalRoute expected) {
        ApprovalRoute actual = new ApprovalRouter().route(amount, urgent, risk, personal);
        check(name + " -> " + actual, actual == expected);
    }

    private static void checkInvalid(String name, Runnable action, String expectedMessage) {
        try {
            action.run();
            check(name + " (ожидалась ошибка)", false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage().contains(expectedMessage));
        }
    }

    public static void main(String[] args) {
        check("порог варианта 7 равен буквальному значению 40", ApprovalRouter.FINANCE_LIMIT == 40L);
        checkRoute("исходная проверка персональных данных", 10_000, true, RiskLevel.LOW, true,
                ApprovalRoute.SECURITY_REVIEW);
        checkRoute("исходная проверка финансового согласования", 500_000, false, RiskLevel.LOW, false,
                ApprovalRoute.FINANCE_REVIEW);

        checkRoute("нижняя граница суммы", 0, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        checkRoute("нулевая срочная заявка", 0, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("сумма 39 без срочности", 39, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        checkRoute("сумма 39 со срочностью", 39, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("сумма 40 без срочности", 40, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("сумма 40 со срочностью", 40, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("сумма 41 без срочности", 41, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("сумма 41 со срочностью", 41, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("обязательная граница 499999", 499_999, false, RiskLevel.LOW, false,
                ApprovalRoute.FINANCE_REVIEW);
        checkRoute("обязательная граница 500000 со срочностью", 500_000, true, RiskLevel.LOW, false,
                ApprovalRoute.FINANCE_REVIEW);
        checkRoute("обязательная граница 500001", 500_001, false, RiskLevel.LOW, false,
                ApprovalRoute.FINANCE_REVIEW);
        checkRoute("максимальный long", Long.MAX_VALUE, false, RiskLevel.LOW, false,
                ApprovalRoute.FINANCE_REVIEW);

        checkRoute("HIGH выше срочности при малой сумме", 1, true, RiskLevel.HIGH, false,
                ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("HIGH без срочности и суммы", 0, false, RiskLevel.HIGH, false,
                ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("HIGH выше финансового правила", 40, false, RiskLevel.HIGH, false,
                ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("HIGH выше финансового правила и срочности", Long.MAX_VALUE, true, RiskLevel.HIGH,
                false, ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("персональные данные выше HIGH, суммы и срочности", Long.MAX_VALUE, true,
                RiskLevel.HIGH, true, ApprovalRoute.SECURITY_REVIEW);
        checkRoute("персональные данные при нулевой сумме", 0, false, RiskLevel.LOW, true,
                ApprovalRoute.SECURITY_REVIEW);
        checkRoute("персональные данные выше срочности", 39, true, RiskLevel.LOW, true,
                ApprovalRoute.SECURITY_REVIEW);
        checkRoute("персональные данные выше финансового правила", 40, false, RiskLevel.MEDIUM, true,
                ApprovalRoute.SECURITY_REVIEW);
        checkRoute("MEDIUM не требует директора", 39, false, RiskLevel.MEDIUM, false,
                ApprovalRoute.STANDARD);
        checkRoute("MEDIUM допускает ускоренный маршрут", 39, true, RiskLevel.MEDIUM, false,
                ApprovalRoute.FAST_TRACK);
        checkRoute("MEDIUM допускает финансовое согласование", 40, true, RiskLevel.MEDIUM, false,
                ApprovalRoute.FINANCE_REVIEW);

        Request parsed = RequestParser.parse(" 39 ; TRUE ; low ; False ");
        check("разбор суммы", parsed.getAmount() == 39);
        check("разбор срочности без учёта регистра", parsed.isUrgent());
        check("разбор риска с пробелами и без учёта регистра", parsed.getRisk() == RiskLevel.LOW);
        check("разбор отсутствия персональных данных", !parsed.containsPersonalData());
        check("маршрут после разбора строки", new ApprovalRouter().route(parsed.getAmount(),
                parsed.isUrgent(), parsed.getRisk(), parsed.containsPersonalData()) == ApprovalRoute.FAST_TRACK);
        check("разбор MEDIUM", RiskLevel.parse(" medium ") == RiskLevel.MEDIUM);
        check("разбор HIGH", RiskLevel.parse("HIGH") == RiskLevel.HIGH);
        check("разбор максимального long", RequestParser.parse("9223372036854775807;false;LOW;false")
                .getAmount() == Long.MAX_VALUE);

        checkInvalid("отрицательная сумма в route",
                () -> new ApprovalRouter().route(-1, false, RiskLevel.LOW, false), "отрицательной");
        checkInvalid("Long.MIN_VALUE в route",
                () -> new ApprovalRouter().route(Long.MIN_VALUE, true, RiskLevel.HIGH, true), "отрицательной");
        checkInvalid("отсутствующий риск в route",
                () -> new ApprovalRouter().route(40, true, null, true), "уровень риска не задан");
        checkInvalid("отсутствующий риск при разборе enum", () -> RiskLevel.parse(null), "не задан");
        checkInvalid("пустой риск при разборе enum", () -> RiskLevel.parse("  "), "не задан");
        checkInvalid("неизвестный риск", () -> RiskLevel.parse("CRITICAL"), "LOW, MEDIUM или HIGH");
        checkInvalid("null вместо строки", () -> RequestParser.parse(null), "пустая строка заявки");
        checkInvalid("пустая строка", () -> RequestParser.parse(""), "пустая строка заявки");
        checkInvalid("только пробелы", () -> RequestParser.parse("   "), "пустая строка заявки");
        checkInvalid("недостаточно полей", () -> RequestParser.parse("39;true;LOW"), "получено 3");
        checkInvalid("лишнее поле", () -> RequestParser.parse("39;true;LOW;false;extra"), "получено 5");
        checkInvalid("пустая сумма", () -> RequestParser.parse(";true;LOW;false"), "amount (позиция 1)");
        checkInvalid("отрицательная сумма в строке", () -> RequestParser.parse("-1;true;LOW;false"),
                "amount (позиция 1): сумма не может быть отрицательной");
        checkInvalid("текст вместо суммы", () -> RequestParser.parse("abc;true;LOW;false"), "amount (позиция 1)");
        checkInvalid("дробная сумма", () -> RequestParser.parse("1.5;true;LOW;false"), "amount (позиция 1)");
        checkInvalid("переполнение long", () -> RequestParser.parse("9223372036854775808;true;LOW;false"),
                "amount (позиция 1)");
        checkInvalid("пустая срочность", () -> RequestParser.parse("39;;LOW;false"), "urgent (позиция 2)");
        checkInvalid("yes не превращается молча в false", () -> RequestParser.parse("39;yes;LOW;false"),
                "urgent (позиция 2)");
        checkInvalid("пустой риск в строке", () -> RequestParser.parse("39;true;;false"), "risk (позиция 3)");
        checkInvalid("неизвестный риск в строке", () -> RequestParser.parse("39;true;UNKNOWN;false"),
                "risk (позиция 3)");
        checkInvalid("пустое последнее поле сохраняется", () -> RequestParser.parse("39;true;LOW;"),
                "personal (позиция 4)");
        checkInvalid("число вместо boolean", () -> RequestParser.parse("39;true;LOW;0"), "personal (позиция 4)");

        System.out.printf("Результат: PASS — %d, FAIL — %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
