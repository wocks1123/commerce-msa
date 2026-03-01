package dev.labs.commerce.order.core.order.domain.event;

import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        List<OrderItemPayload> items,
        long totalPrice,
        Long totalAmount,
        String currency
) {
    public record OrderItemPayload(
            Long productId,
            Integer quantity,
            long unitPrice,
            long lineAmount,
            String currency
    ) {
    }
}
