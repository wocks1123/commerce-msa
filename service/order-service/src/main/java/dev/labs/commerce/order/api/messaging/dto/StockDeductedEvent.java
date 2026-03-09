package dev.labs.commerce.order.api.messaging.dto;

public record StockDeductedEvent(
        Long productId,
        String orderId,
        int quantity,
        int remainingQuantity
) {
}
