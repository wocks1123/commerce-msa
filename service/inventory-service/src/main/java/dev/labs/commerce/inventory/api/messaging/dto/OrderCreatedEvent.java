package dev.labs.commerce.inventory.api.messaging.dto;

import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        List<OrderItemPayload> items
) {
    public record OrderItemPayload(
            Long productId,
            int quantity
    ) {
    }
}
