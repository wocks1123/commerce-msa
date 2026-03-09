package dev.labs.commerce.payment.core.payment.application.event;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

public record PaymentFailedEvent(
        String paymentId,
        String orderId,
        long customerId,
        String failureCode,
        @Nullable String failureMessage,
        Instant failedAt
) {
    public PaymentFailedEvent {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(failureCode, "failureCode must not be null");
        Objects.requireNonNull(failedAt, "failedAt must not be null");
    }
}
