package ru.edu.pr04;

import java.util.ArrayList;
import java.util.List;

public final class Order {
    private final Employee employee;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("требуется сотрудник");
        }
        this.employee = employee;
    }

    public void addProduct(Product product, int quantity) {
        ensureDraft();
        OrderItem addition = new OrderItem(product, quantity);
        if (!Double.isFinite(total() + addition.getLineTotal())) {
            throw new IllegalArgumentException("сумма заказа слишком велика");
        }
        for (OrderItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                Product existing = item.getProduct();
                if (!existing.getName().equals(product.getName())
                        || Double.compare(existing.getPrice(), product.getPrice()) != 0) {
                    throw new IllegalArgumentException("конфликт данных товара с id " + product.getId());
                }
                item.increase(quantity);
                return;
            }
        }
        items.add(addition);
    }

    public void removeProduct(String productId) {
        ensureDraft();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("требуется идентификатор товара");
        }
        String id = productId.trim();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProduct().getId().equals(id)) {
                items.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("товар не найден в заказе: " + id);
    }

    public double total() {
        double sum = 0;
        for (OrderItem item : items) {
            sum += item.getLineTotal();
        }
        return sum;
    }

    public void confirm() {
        ensureDraft();
        if (items.isEmpty()) {
            throw new IllegalStateException("невозможно подтвердить пустой заказ");
        }
        status = OrderStatus.CONFIRMED;
    }

    public List<OrderItem> getItems() {
        List<OrderItem> copies = new ArrayList<>();
        for (OrderItem item : items) {
            copies.add(new OrderItem(item.getProduct(), item.getQuantity()));
        }
        return List.copyOf(copies);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Employee getEmployee() {
        return employee;
    }

    private void ensureDraft() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("подтверждённый заказ изменить нельзя");
        }
    }
}
