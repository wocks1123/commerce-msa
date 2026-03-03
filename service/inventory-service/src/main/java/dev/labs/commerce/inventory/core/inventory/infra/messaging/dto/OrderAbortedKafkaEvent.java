package dev.labs.commerce.inventory.core.inventory.infra.messaging.dto;

import java.util.List;

public record OrderAbortedKafkaEvent(
        String orderId,
        List<OrderItemPayload> items
) {
    public record OrderItemPayload(
            long productId,
            int quantity
    ) {
    }
}
