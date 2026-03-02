package dev.labs.commerce.order.core.order.infra.messaging.dto;

public record StockDeductionFailedKafkaEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
}
