package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record DecreaseInventoryQuantityCommand(
        Long productId,
        String orderId,
        int quantity
) {
}
