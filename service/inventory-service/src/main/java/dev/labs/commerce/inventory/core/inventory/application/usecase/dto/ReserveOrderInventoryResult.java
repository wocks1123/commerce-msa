package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

import java.util.List;

public record ReserveOrderInventoryResult(
        String orderId,
        List<ItemResult> items
) {
    public record ItemResult(
            Long productId,
            int totalQuantity,
            int availableQuantity
    ) {
    }
}