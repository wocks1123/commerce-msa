package dev.labs.commerce.order.api.dto;

public record CreateOrderItemRequest(
        long productId,
        String productName,
        long unitPrice,
        int quantity,
        long lineAmount,
        String currency
) {
}
