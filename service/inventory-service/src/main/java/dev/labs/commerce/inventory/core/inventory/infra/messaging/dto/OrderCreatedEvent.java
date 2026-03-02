package dev.labs.commerce.inventory.core.inventory.infra.messaging.dto;

import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        List<OrderItemPayload> items
) {
    public record OrderItemPayload(
            Long productId,
            Integer quantity
    ) {
    }
}
