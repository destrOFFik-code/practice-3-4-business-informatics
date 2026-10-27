package ru.edu.pr04;

public final class OrderItem {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 40;

    private final Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("требуется товар");
        }
        this.product = product;
        setQuantity(quantity);
    }

    private void setQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("количество должно быть от "
                    + MIN_QUANTITY + " до " + MAX_QUANTITY);
        }
        if (!Double.isFinite(product.getPrice() * quantity)) {
            throw new IllegalArgumentException("стоимость позиции слишком велика");
        }
        this.quantity = quantity;
    }

    public void increase(int delta) {
        // Проверяем до сложения, чтобы исключить переполнение int и частичное изменение.
        if (delta <= 0 || delta > MAX_QUANTITY - quantity) {
            throw new IllegalArgumentException("приращение должно быть положительным, "
                    + "итоговое количество — не больше " + MAX_QUANTITY);
        }
        setQuantity(quantity + delta);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }
}
