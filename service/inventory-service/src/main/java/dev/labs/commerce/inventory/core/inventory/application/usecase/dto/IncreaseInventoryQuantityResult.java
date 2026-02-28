package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record IncreaseInventoryQuantityResult(
        Long productId,
        int totalQuantity,
        int availableQuantity
) {
}
