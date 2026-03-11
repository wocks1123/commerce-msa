package dev.labs.commerce.order.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockReservedEvent(
        @NotNull Long productId,
        @NotBlank String orderId,
        int quantity,
        int remainingQuantity
) {
}
