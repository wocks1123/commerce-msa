package dev.labs.commerce.order.core.order.infra.messaging.dto;

public record StockDeductedKafkaEvent(
        Long productId,
        Long orderId,
        int quantity,
        int remainingQuantity
) {
}
