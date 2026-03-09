package dev.labs.commerce.inventory.core.inventory.application.event;

import java.util.Objects;

public record StockDeductedEvent(
        Long productId,
        String orderId,
        int quantity,
        int remainingQuantity
) {
    public StockDeductedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
    }
}
