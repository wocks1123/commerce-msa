package dev.labs.commerce.order.api.http.dto;

public record CreateOrderResponse(
        String orderId,
        String status,
        long totalPrice,
        long totalAmount,
        String currency
) {
}
