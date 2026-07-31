package dev.labs.commerce.payment.core.payment.infra.client.dto;

import java.util.List;

public record GetSalesOrderResponse(
        String orderId,
        long customerId,
        String status,
        long totalPrice,
        String currency,
        List<Item> items
) {
    public record Item(Long productId, int quantity) {
    }
}
