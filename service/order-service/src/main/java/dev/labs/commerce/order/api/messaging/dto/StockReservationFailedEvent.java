package dev.labs.commerce.order.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockReservationFailedEvent(
        @NotNull Long productId,
        @NotBlank String orderId,
        int quantity,
        @NotBlank String errorCode
) {
}
