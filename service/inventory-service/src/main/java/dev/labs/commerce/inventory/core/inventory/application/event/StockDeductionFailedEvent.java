package dev.labs.commerce.inventory.core.inventory.application.event;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockDeductionFailedEvent(
        @NotNull Long productId,
        @NotBlank String orderId,
        int quantity,
        @NotBlank String errorCode
) {
    public StockDeductionFailedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
