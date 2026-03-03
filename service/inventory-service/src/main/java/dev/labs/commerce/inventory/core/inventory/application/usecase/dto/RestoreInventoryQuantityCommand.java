package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record RestoreInventoryQuantityCommand(
        Long productId,
        String orderId,
        int quantity
) {
}
