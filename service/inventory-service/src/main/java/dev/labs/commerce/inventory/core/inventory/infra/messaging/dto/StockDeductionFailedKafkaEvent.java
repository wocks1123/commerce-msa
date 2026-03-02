package dev.labs.commerce.inventory.core.inventory.infra.messaging.dto;

public record StockDeductionFailedKafkaEvent(
        Long productId,
        String orderId,
        int quantity,
        String errorCode
) {
}
