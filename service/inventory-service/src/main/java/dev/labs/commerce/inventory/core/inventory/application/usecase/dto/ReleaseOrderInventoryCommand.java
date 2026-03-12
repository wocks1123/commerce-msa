package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

public record ReleaseOrderInventoryCommand(
        Long productId,
        String orderId,
        int quantity
) {
}
