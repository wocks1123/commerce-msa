package dev.labs.commerce.order.api.messaging.dto;

public record StockDeductionFailedKafkaEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
}
