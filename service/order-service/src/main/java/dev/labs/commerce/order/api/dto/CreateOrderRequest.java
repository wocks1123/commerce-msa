package dev.labs.commerce.order.api.dto;

import java.util.List;

public record CreateOrderRequest(
        long customerId,
        String currency,
        long totalPrice,
        long totalAmount,
        List<CreateOrderItemRequest> items
) {
}
