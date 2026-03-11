package dev.labs.commerce.inventory.core.inventory.application.event;

import java.util.Objects;

public record StockReservedEvent(
        Long productId,
        String orderId,
        int quantity,
        int remainingQuantity
) {
    public StockReservedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
    }
}
