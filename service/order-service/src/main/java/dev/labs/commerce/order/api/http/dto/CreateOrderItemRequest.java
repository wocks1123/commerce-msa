package dev.labs.commerce.order.api.http.dto;

public record CreateOrderItemRequest(
        long productId,
        long unitPrice,
        int quantity,
        long lineAmount,
        String currency
) {
}
