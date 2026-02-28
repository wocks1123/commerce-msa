package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record DecreaseInventoryQuantityCommand(
        Long productId,
        int quantity
) {
}
