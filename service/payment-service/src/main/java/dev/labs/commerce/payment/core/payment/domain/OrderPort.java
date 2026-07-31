package dev.labs.commerce.payment.core.payment.domain;

import java.util.List;

public interface OrderPort {

    OrderSnapshot getOrder(String orderId);

    record OrderSnapshot(
            String orderId,
            long customerId,
            OrderStatus status,
            long totalPrice,
            String currency,
            List<Item> items
    ) {
        public record Item(Long productId, int quantity) {}
    }
}
