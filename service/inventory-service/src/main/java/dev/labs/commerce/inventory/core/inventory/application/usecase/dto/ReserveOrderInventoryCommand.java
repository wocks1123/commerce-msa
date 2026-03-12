package dev.labs.commerce.inventory.core.inventory.application.usecase.dto;

import dev.labs.commerce.common.error.ValidationException;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;

import java.util.List;

public record ReserveOrderInventoryCommand(
        String orderId,
        List<Item> items
) {
    public record Item(
            Long productId,
            int quantity
    ) {
        public Item {
            if (quantity <= 0) {
                throw new ValidationException(InventoryErrorCode.INVALID_QUANTITY);
            }
        }
    }
}