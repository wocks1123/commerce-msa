package dev.labs.commerce.inventory.core.inventory.application.event;

public record StockDeductionFailedEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
}
