package dev.labs.commerce.order.api.messaging.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentExpiredEvent(
        @NotBlank String orderId
) {
}
