package dev.labs.commerce.inventory.api.http.dto;

public record InventoryQuantityResponse(
        Long productId,
        int totalQuantity,
        int availableQuantity
) {
}
