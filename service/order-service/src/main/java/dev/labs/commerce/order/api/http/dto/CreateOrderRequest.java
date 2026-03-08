package dev.labs.commerce.order.api.http.dto;

import java.util.List;

public record CreateOrderRequest(
        long customerId,
        String currency,
        long totalPrice,
        long totalAmount,
        List<CreateOrderItemRequest> items
) {
}
