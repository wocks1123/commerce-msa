package dev.labs.commerce.order.core.order.application.usecase.dto;

import dev.labs.commerce.order.core.order.domain.OrderStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public record GetSalesOrderResult(
        String orderId,
        long customerId,
        OrderStatus status,
        long totalPrice,
        long totalAmount,
        String currency,
        List<OrderItemResult> items,
        Instant pendingAt,
        @Nullable Instant paidAt,
        @Nullable Instant abortedAt,
        @Nullable Instant cancelledAt,
        @Nullable Instant failedAt,
        @Nullable Instant expiredAt
) {
    public record OrderItemResult(
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
