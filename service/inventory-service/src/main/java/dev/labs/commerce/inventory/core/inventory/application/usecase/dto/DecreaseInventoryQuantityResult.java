package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record DecreaseInventoryQuantityResult(
        Long productId,
        int totalQuantity,
        int availableQuantity
) {
}
