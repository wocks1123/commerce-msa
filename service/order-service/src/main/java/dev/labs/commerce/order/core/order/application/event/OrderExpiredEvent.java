package dev.labs.commerce.order.core.order.application.event;

import java.util.List;
import java.util.Objects;

public record OrderExpiredEvent(
        String orderId,
        List<OrderItemPayload> items
) {
    public OrderExpiredEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty");
    }

    public record OrderItemPayload(
            long productId,
            int quantity
    ) {
    }
}
