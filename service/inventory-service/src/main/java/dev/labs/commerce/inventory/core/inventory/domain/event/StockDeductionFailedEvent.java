package dev.labs.commerce.inventory.core.inventory.domain.event;

public record StockDeductionFailedEvent(
        Long productId,
        Long orderId,
        int quantity,
        String errorCode
) {
}
