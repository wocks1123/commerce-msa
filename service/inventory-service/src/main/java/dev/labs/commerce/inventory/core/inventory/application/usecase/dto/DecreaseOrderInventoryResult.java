package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

import java.util.List;

public record DecreaseOrderInventoryResult(
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