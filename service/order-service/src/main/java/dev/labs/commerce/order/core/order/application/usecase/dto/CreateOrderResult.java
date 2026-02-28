package dev.labs.commerce.order.core.order.application.usecase.dto;

import dev.labs.commerce.order.core.order.domain.OrderStatus;

public record CreateOrderResult(
        String orderId,
        OrderStatus status,
        long totalPrice,
        long totalAmount,
        String currency
) {
}
