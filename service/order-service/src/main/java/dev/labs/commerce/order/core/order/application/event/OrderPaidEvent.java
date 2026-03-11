package dev.labs.commerce.order.core.order.application.event;

import java.util.List;
import java.util.Objects;

public record OrderPaidEvent(
        String orderId,
        List<OrderItemPayload> items
) {
    public OrderPaidEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }

    public record OrderItemPayload(
            Long productId,
            Integer quantity
    ) {
    }
}
