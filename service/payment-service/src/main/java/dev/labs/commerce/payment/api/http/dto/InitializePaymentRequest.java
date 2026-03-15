package dev.labs.commerce.payment.api.http.dto;

import java.util.List;

public record InitializePaymentRequest(
        String orderId,
        long customerId,
        long amount,
        String currency,
        String idempotencyKey,
        List<Item> items
) {
    public record Item(Long productId, int quantity) {
    }
}
