package dev.labs.commerce.order.core.order.application.event;

import java.util.List;
import java.util.Objects;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        List<OrderItemPayload> items,
        long totalPrice,
        Long totalAmount,
        String currency
) {
    public OrderCreatedEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty");
    }

    public record OrderItemPayload(
            Long productId,
            Integer quantity,
            long unitPrice,
            long lineAmount,
            String currency
    ) {
    }
}
