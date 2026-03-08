package dev.labs.commerce.order.api.messaging.dto;

public record StockDeductedKafkaEvent(
        Long productId,
        String orderId,
        int quantity,
        int remainingQuantity
) {
}
