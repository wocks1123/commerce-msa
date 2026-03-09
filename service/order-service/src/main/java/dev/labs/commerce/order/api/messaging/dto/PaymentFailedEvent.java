package dev.labs.commerce.order.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentFailedEvent(
        @NotBlank String orderId,
        @NotBlank String errorCode
) {
}
