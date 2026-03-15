package dev.labs.commerce.inventory.api.http.dto;

import java.util.List;

public record ReserveOrderInventoryRequest(
        String orderId,
        List<Item> items
) {

    public record Item(Long productId, int quantity) {
    }

}