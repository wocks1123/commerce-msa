package dev.labs.commerce.inventory.core.inventory.domain.event;

public record StockDeductionFailedEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
}
