package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record GetInventoryResult(
        Long productId,
        int totalQuantity,
        int availableQuantity
) {
}
