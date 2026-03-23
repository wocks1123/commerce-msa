package dev.labs.commerce.order.api.http.dto;

import dev.labs.commerce.order.core.order.domain.OrderStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public record GetSalesOrderResponse(
        String orderId,
        long customerId,
        OrderStatus status,
        long totalPrice,
        long totalAmount,
        String currency,
        List<OrderItemResponse> items,
        Instant pendingAt,
        @Nullable Instant paidAt,
        @Nullable Instant abortedAt,
        @Nullable Instant cancelledAt,
        @Nullable Instant failedAt,
        @Nullable Instant expiredAt
) {
    public record OrderItemResponse(
            Long orderItemId,
            long productId,
            String productName,
            long unitPrice,
            int quantity,
            long lineAmount,
            String currency
    ) {
    }
}
