package dev.labs.commerce.payment.api.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record InitializePaymentRequest(
        @NotBlank String orderId,
        @Positive long customerId,
        @Positive long amount,
        @NotBlank String currency,
        @NotBlank String idempotencyKey
) {
}
