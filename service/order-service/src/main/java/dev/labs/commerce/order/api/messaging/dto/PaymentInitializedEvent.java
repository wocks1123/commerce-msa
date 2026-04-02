package dev.labs.commerce.order.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PaymentInitializedEvent(
        @NotBlank String paymentId,
        @NotBlank String orderId,
        @NotNull Instant requestedAt
) {
}
