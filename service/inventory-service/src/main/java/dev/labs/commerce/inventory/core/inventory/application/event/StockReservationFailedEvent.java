package dev.labs.commerce.inventory.core.inventory.application.event;

import java.util.Objects;

public record StockReservationFailedEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
    public StockReservationFailedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
