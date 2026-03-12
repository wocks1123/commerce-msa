package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

import java.util.List;

public record ConfirmOrderInventoryCommand(
        String orderId,
        List<Item> items
) {
    public record Item(
            Long productId,
            int quantity
    ) {
    }
}
