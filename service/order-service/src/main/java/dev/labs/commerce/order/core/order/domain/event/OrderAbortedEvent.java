package dev.labs.commerce.order.core.order.domain.event;

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
