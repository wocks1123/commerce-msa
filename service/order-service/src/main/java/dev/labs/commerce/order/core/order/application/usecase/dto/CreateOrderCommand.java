package dev.labs.commerce.order.core.order.application.usecase.dto;

import java.util.List;

public record CreateOrderCommand(
        long customerId,
        String currency,
        long totalPrice,
        long totalAmount,
        List<OrderItemCommand> items
) {
}
