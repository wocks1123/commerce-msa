package dev.labs.commerce.order.api.messaging.dto;

public record StockDeductionFailedEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
}
