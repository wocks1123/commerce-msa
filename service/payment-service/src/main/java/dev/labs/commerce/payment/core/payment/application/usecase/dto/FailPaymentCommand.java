package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import org.jspecify.annotations.Nullable;

public record FailPaymentCommand(
        String orderId,
        String failureCode,
        @Nullable String failureMessage
) {
}
