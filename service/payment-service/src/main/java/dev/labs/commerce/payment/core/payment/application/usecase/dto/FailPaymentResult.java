package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record FailPaymentResult(
        String paymentId,
        String orderId,
        PaymentStatus status,
        String failureCode,
        @Nullable String failureMessage,
        Instant failedAt
) {
}
