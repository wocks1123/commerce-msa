package dev.labs.commerce.order.core.order.infra.messaging.dto;

public record StockDeductionFailedKafkaEvent(
        Long productId,
        Long orderId,
        int quantity,
        String errorCode
) {
}
