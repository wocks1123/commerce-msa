package dev.labs.commerce.inventory.api.dto;

public record InventoryQuantityResponse(
        Long productId,
        int totalQuantity,
        int availableQuantity
) {
}
