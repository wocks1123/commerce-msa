package dev.labs.commerce.inventory.api.messaging.dto;

import java.util.List;

public record OrderAbortedEvent(
        String orderId,
        List<OrderItemPayload> items
) {
    public record OrderItemPayload(
            long productId,
            int quantity
    ) {
    }
}
