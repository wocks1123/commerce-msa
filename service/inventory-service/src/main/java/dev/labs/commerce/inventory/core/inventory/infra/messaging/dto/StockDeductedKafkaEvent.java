package dev.labs.commerce.inventory.core.inventory.infra.messaging.dto;

public record StockDeductedKafkaEvent(
        Long productId,
        Long orderId,
        int quantity,
        int remainingQuantity
) {
}
