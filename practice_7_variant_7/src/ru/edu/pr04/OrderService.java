package ru.edu.pr04;

public class OrderService {
    public Order createOrder(Employee employee) {
        return new Order(employee);
    }

    public void addItemFromCsv(Order order, String line) {
        requireOrder(order);
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("пустая строка позиции");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException(
                    "нужно 4 поля productId;name;price;quantity, получено " + fields.length);
        }
        if (fields[0].isBlank()) {
            throw new IllegalArgumentException("productId (позиция 1): пустое значение");
        }
        if (fields[1].isBlank()) {
            throw new IllegalArgumentException("name (позиция 2): пустое значение");
        }
        double price;
        try {
            price = Double.parseDouble(fields[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("price (позиция 3): ожидается число");
        }
        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("price (позиция 3): ожидается конечное положительное число");
        }
        int quantity;
        try {
            quantity = Integer.parseInt(fields[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("quantity (позиция 4): ожидается целое число");
        }
        order.addProduct(new Product(fields[0], fields[1], price), quantity);
    }

    public void confirmOrder(Order order) {
        requireOrder(order);
        order.confirm();
    }

    private void requireOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("требуется заказ");
        }
    }
}
