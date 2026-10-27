package ru.edu.pr04;

import java.util.List;

public class SelfCheck {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        check("исходная проверка цены Product", () ->
                equal(1000.0, new Product("P1", "Mouse", 1000).getPrice()));
        check("сотрудник: пробелы удаляются", () -> {
            Employee employee = new Employee(" E1 ", " Иванов Иван ");
            require(employee.id().equals("E1") && employee.fullName().equals("Иванов Иван"));
        });
        check("сотрудник без id", () -> expect(IllegalArgumentException.class,
                () -> new Employee(null, "Иванов Иван")));
        check("сотрудник без ФИО", () -> expect(IllegalArgumentException.class,
                () -> new Employee("E1", "  ")));
        check("заказ без сотрудника", () -> expect(IllegalArgumentException.class, () -> new Order(null)));
        check("товар без id", () -> expect(IllegalArgumentException.class,
                () -> new Product(" ", "Мышь", 100)));
        check("товар без наименования", () -> expect(IllegalArgumentException.class,
                () -> new Product("P1", null, 100)));
        check("нулевая цена", () -> expect(IllegalArgumentException.class,
                () -> new Product("P1", "Мышь", 0)));
        check("отрицательная цена", () -> expect(IllegalArgumentException.class,
                () -> new Product("P1", "Мышь", -1)));
        check("NaN вместо цены", () -> expect(IllegalArgumentException.class,
                () -> new Product("P1", "Мышь", Double.NaN)));
        check("бесконечная цена", () -> expect(IllegalArgumentException.class,
                () -> new Product("P1", "Мышь", Double.POSITIVE_INFINITY)));
        check("товар сохраняет нормализованные данные", () -> {
            Product product = new Product(" P1 ", " Мышь ", 0.01);
            require(product.getId().equals("P1") && product.getName().equals("Мышь"));
            equal(0.01, product.getPrice());
        });
        check("новый заказ пуст и имеет статус DRAFT", () -> {
            Order order = order();
            require(order.getStatus() == OrderStatus.DRAFT && order.getItems().isEmpty());
            equal(0, order.total());
        });
        check("типовой заказ: 2 товара по 1000", () -> {
            Order order = order();
            order.addProduct(product(), 2);
            equal(2000, order.total());
            require(order.getItems().size() == 1);
        });
        check("нижняя граница количества 1", () -> equal(1000, new OrderItem(product(), 1).getLineTotal()));
        check("верхняя граница варианта 7 равна 40", () -> {
            require(OrderItem.MAX_QUANTITY == 40);
            Order order = order();
            order.addProduct(product(), 40);
            require(order.getItems().get(0).getQuantity() == 40);
            equal(40000, order.total());
        });
        check("количество 0", () -> expect(IllegalArgumentException.class, () -> new OrderItem(product(), 0)));
        check("количество -1", () -> expect(IllegalArgumentException.class, () -> new OrderItem(product(), -1)));
        check("количество 41", () -> expect(IllegalArgumentException.class, () -> order().addProduct(product(), 41)));
        check("позиция без товара", () -> expect(IllegalArgumentException.class, () -> new OrderItem(null, 1)));
        check("добавление null не изменяет заказ", () -> {
            Order order = order();
            expect(IllegalArgumentException.class, () -> order.addProduct(null, 1));
            require(order.getItems().isEmpty());
        });
        check("положительное увеличение количества", () -> {
            OrderItem item = new OrderItem(product(), 2);
            item.increase(3);
            require(item.getQuantity() == 5);
            equal(5000, item.getLineTotal());
        });
        check("нулевое увеличение запрещено", () -> {
            OrderItem item = new OrderItem(product(), 2);
            expect(IllegalArgumentException.class, () -> item.increase(0));
            require(item.getQuantity() == 2);
        });
        check("отрицательное увеличение запрещено", () -> expect(IllegalArgumentException.class,
                () -> new OrderItem(product(), 2).increase(-1)));
        check("переполнение int при увеличении исключено", () -> {
            OrderItem item = new OrderItem(product(), 1);
            expect(IllegalArgumentException.class, () -> item.increase(Integer.MAX_VALUE));
            require(item.getQuantity() == 1);
        });
        check("повтор товара объединяется по id", () -> {
            Order order = order();
            order.addProduct(product(), 2);
            order.addProduct(new Product("P1", "Мышь", 1000), 3);
            require(order.getItems().size() == 1 && order.getItems().get(0).getQuantity() == 5);
            equal(5000, order.total());
        });
        check("объединение до точной границы 40", () -> {
            Order order = order();
            order.addProduct(product(), 20);
            order.addProduct(product(), 20);
            require(order.getItems().get(0).getQuantity() == 40);
        });
        check("объединение сверх 40 отклоняется без изменений", () -> {
            Order order = order();
            order.addProduct(product(), 40);
            expect(IllegalArgumentException.class, () -> order.addProduct(product(), 1));
            require(order.getItems().size() == 1 && order.getItems().get(0).getQuantity() == 40);
            equal(40000, order.total());
        });
        check("тот же id с другой ценой — конфликт", () -> {
            Order order = order();
            order.addProduct(product(), 2);
            expect(IllegalArgumentException.class,
                    () -> order.addProduct(new Product("P1", "Мышь", 1500), 1));
            equal(2000, order.total());
            require(order.getItems().get(0).getQuantity() == 2);
        });
        check("тот же id с другим названием — конфликт", () -> {
            Order order = order();
            order.addProduct(product(), 1);
            expect(IllegalArgumentException.class,
                    () -> order.addProduct(new Product("P1", "Монитор", 1000), 1));
            require(order.getItems().size() == 1);
        });
        check("разные id создают разные позиции", () -> {
            Order order = order();
            order.addProduct(product(), 2);
            order.addProduct(new Product("P2", "Клавиатура", 500), 3);
            require(order.getItems().size() == 2);
            equal(3500, order.total());
        });
        check("удаление по id пересчитывает итог", () -> {
            Order order = order();
            order.addProduct(product(), 2);
            order.addProduct(new Product("P2", "Клавиатура", 500), 3);
            order.removeProduct(" P1 ");
            require(order.getItems().size() == 1);
            equal(1500, order.total());
            order.removeProduct("P2");
            require(order.getItems().isEmpty());
            equal(0, order.total());
        });
        check("удаление отсутствующего id не меняет состав", () -> {
            Order order = order();
            order.addProduct(product(), 1);
            expect(IllegalArgumentException.class, () -> order.removeProduct("UNKNOWN"));
            equal(1000, order.total());
        });
        check("удаление без id", () -> expect(IllegalArgumentException.class, () -> order().removeProduct(null)));
        check("пустой заказ нельзя подтвердить", () -> {
            Order order = order();
            expect(IllegalStateException.class, order::confirm);
            require(order.getStatus() == OrderStatus.DRAFT);
        });
        check("подтверждение через сервис", () -> {
            Order order = order();
            order.addProduct(product(), 2);
            new OrderService().confirmOrder(order);
            require(order.getStatus() == OrderStatus.CONFIRMED);
            equal(2000, order.total());
        });
        check("повторное подтверждение запрещено", () -> {
            Order order = confirmedOrder();
            expect(IllegalStateException.class, order::confirm);
        });
        check("после confirm нельзя добавить новый товар", () -> {
            Order order = confirmedOrder();
            expect(IllegalStateException.class,
                    () -> order.addProduct(new Product("P2", "Клавиатура", 500), 1));
            equal(1000, order.total());
        });
        check("после confirm нельзя объединить повтор", () -> {
            Order order = confirmedOrder();
            expect(IllegalStateException.class, () -> order.addProduct(product(), 1));
            require(order.getItems().get(0).getQuantity() == 1);
        });
        check("после confirm нельзя удалить товар", () -> {
            Order order = confirmedOrder();
            expect(IllegalStateException.class, () -> order.removeProduct("P1"));
            equal(1000, order.total());
        });
        check("внешний список нельзя очистить", () -> {
            Order order = confirmedOrder();
            expect(UnsupportedOperationException.class, () -> order.getItems().clear());
            require(order.getItems().size() == 1);
        });
        check("во внешний список нельзя добавить позицию", () -> {
            Order order = order();
            expect(UnsupportedOperationException.class,
                    () -> order.getItems().add(new OrderItem(product(), 1)));
            require(order.getItems().isEmpty());
        });
        check("замена элемента внешнего списка запрещена", () -> {
            Order order = confirmedOrder();
            expect(UnsupportedOperationException.class,
                    () -> order.getItems().set(0, new OrderItem(product(), 2)));
            equal(1000, order.total());
        });
        check("increase внешней копии не меняет подтверждённый заказ", () -> {
            Order order = confirmedOrder();
            OrderItem copy = order.getItems().get(0);
            copy.increase(3);
            require(copy.getQuantity() == 4 && order.getItems().get(0).getQuantity() == 1);
            equal(1000, order.total());
            require(order.getStatus() == OrderStatus.CONFIRMED);
        });
        check("снимок до confirm не даёт изменить заказ после confirm", () -> {
            Order order = order();
            order.addProduct(product(), 1);
            List<OrderItem> snapshot = order.getItems();
            order.confirm();
            snapshot.get(0).increase(2);
            equal(1000, order.total());
        });
        check("снимок не меняется при объединении внутри заказа", () -> {
            Order order = order();
            order.addProduct(product(), 1);
            List<OrderItem> snapshot = order.getItems();
            order.addProduct(product(), 1);
            require(snapshot.get(0).getQuantity() == 1 && order.getItems().get(0).getQuantity() == 2);
        });
        check("стоимость позиции с дробной ценой", () -> equal(0.3,
                new OrderItem(new Product("P1", "Кабель", 0.1), 3).getLineTotal()));
        check("переполнение стоимости позиции отклоняется", () -> expect(IllegalArgumentException.class,
                () -> new OrderItem(new Product("P1", "Товар", Double.MAX_VALUE), 2)));
        check("переполнение общей суммы не меняет заказ", () -> {
            Order order = order();
            order.addProduct(new Product("P1", "Товар 1", Double.MAX_VALUE), 1);
            expect(IllegalArgumentException.class,
                    () -> order.addProduct(new Product("P2", "Товар 2", Double.MAX_VALUE), 1));
            require(order.getItems().size() == 1 && Double.isFinite(order.total()));
        });
        check("CSV: типовой разбор и объединение", () -> {
            Order order = order();
            OrderService service = new OrderService();
            service.addItemFromCsv(order, " M01 ; Monitor ; 100 ; 2 ");
            service.addItemFromCsv(order, "M01;Monitor;100;1");
            require(order.getItems().size() == 1 && order.getItems().get(0).getQuantity() == 3);
            equal(300, order.total());
        });
        check("CSV: null и пустая строка", () -> {
            expect(IllegalArgumentException.class, () -> new OrderService().addItemFromCsv(order(), null));
            expect(IllegalArgumentException.class, () -> new OrderService().addItemFromCsv(order(), "  "));
        });
        check("CSV: недостаточно или слишком много полей", () -> {
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;100"));
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;100;1;extra"));
        });
        check("CSV: пустые текстовые поля", () -> {
            expect(IllegalArgumentException.class, () -> csv(";Мышь;100;1"));
            expect(IllegalArgumentException.class, () -> csv("P1;;100;1"));
        });
        check("CSV: неверная цена", () -> {
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;abc;1"));
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;NaN;1"));
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;-100;1"));
        });
        check("CSV: неверное количество и пустое последнее поле", () -> {
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;100;1.5"));
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;100;2147483648"));
            expect(IllegalArgumentException.class, () -> csv("P1;Мышь;100;"));
        });
        check("сервис отклоняет отсутствующий заказ", () -> {
            expect(IllegalArgumentException.class, () -> new OrderService().addItemFromCsv(null, "P1;Мышь;1;1"));
            expect(IllegalArgumentException.class, () -> new OrderService().confirmOrder(null));
        });
        System.out.printf("Результат: PASS — %d, FAIL — %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static Product product() {
        return new Product("P1", "Мышь", 1000);
    }

    private static Order order() {
        return new OrderService().createOrder(new Employee("E1", "Иванов Иван"));
    }

    private static Order confirmedOrder() {
        Order order = order();
        order.addProduct(product(), 1);
        order.confirm();
        return order;
    }

    private static void csv(String line) {
        new OrderService().addItemFromCsv(order(), line);
    }

    private static void check(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (RuntimeException | AssertionError error) {
            failed++;
            System.out.println("FAIL: " + name + " — " + error.getMessage());
        }
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (type.isInstance(e)) {
                return;
            }
            throw new AssertionError("ожидалось " + type.getSimpleName() + ", получено " + e);
        }
        throw new AssertionError("ожидалось " + type.getSimpleName());
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new AssertionError("неверное состояние заказа");
        }
    }

    private static void equal(double expected, double actual) {
        if (!Double.isFinite(actual) || Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError(expected + " != " + actual);
        }
    }
}
