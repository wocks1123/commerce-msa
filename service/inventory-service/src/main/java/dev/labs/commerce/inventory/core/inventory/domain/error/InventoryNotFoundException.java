package dev.labs.commerce.inventory.core.inventory.domain.error;

import dev.labs.commerce.common.error.NotFoundException;

public class InventoryNotFoundException extends NotFoundException {

    public InventoryNotFoundException(InventoryErrorCode errorCode) {
        super(errorCode);
    }
}
