package dev.labs.commerce.payment.core.payment.domain.event;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record PaymentFailedEvent(
        String paymentId,
        String orderId,
        long customerId,
        String failureCode,
        @Nullable String failureMessage,
        Instant failedAt
) {
}
