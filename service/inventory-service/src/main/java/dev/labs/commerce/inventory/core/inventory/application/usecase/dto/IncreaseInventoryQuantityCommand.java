package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record IncreaseInventoryQuantityCommand(
        Long productId,
        int quantity
) {
}
