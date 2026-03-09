package dev.labs.commerce.order.core.order.application.event;

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
