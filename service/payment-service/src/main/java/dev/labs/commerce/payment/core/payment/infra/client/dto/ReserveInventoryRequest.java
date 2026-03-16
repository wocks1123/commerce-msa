package dev.labs.commerce.payment.core.payment.infra.client.dto;

import java.util.List;

public record ReserveInventoryRequest(
        String orderId,
        List<Item> items
) {
    public record Item(Long productId, int quantity) {}
}
