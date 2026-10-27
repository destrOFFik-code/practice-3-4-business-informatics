package ru.edu.pr04;

public final class Product {
    private final String id;
    private final String name;
    private final double price;

    public Product(String id, String name, double price) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("требуется идентификатор товара");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("требуется наименование товара");
        }
        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("цена должна быть конечным положительным числом");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
