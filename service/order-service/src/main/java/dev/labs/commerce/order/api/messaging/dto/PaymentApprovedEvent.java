package dev.labs.commerce.order.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentApprovedEvent(
        @NotBlank String orderId,
        long customerId,
        long amount,
        @NotBlank String currency
) {
}
