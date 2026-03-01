package dev.labs.commerce.inventory.core.inventory.domain.event;

public record StockDeductedEvent(
        Long productId,
        Long orderId,
        int quantity,
        int remainingQuantity
) {
}
