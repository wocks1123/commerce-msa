package dev.labs.commerce.order.core.order.application.usecase.dto;

public record OrderItemCommand(
        long productId,
        String productName,
        long unitPrice,
        int quantity,
        long lineAmount,
        String currency
) {
}
