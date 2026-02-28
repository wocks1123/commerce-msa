package dev.labs.commerce.inventory.core.inventory.domain.error;

import dev.labs.commerce.common.error.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(InventoryErrorCode errorCode) {
        super(errorCode);
    }

    public InsufficientStockException(InventoryErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
