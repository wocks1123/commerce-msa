package dev.labs.commerce.inventory.core.inventory.application.event;

public record StockDeductedEvent(
        Long productId,
        String orderId,
        int quantity,
        int remainingQuantity
) {
}
