package dev.labs.commerce.payment.core.payment.application.event;

import java.time.Instant;
import java.util.Objects;

public record PaymentInitializedEvent(
        String paymentId,
        String orderId,
        Instant requestedAt
) {
    public PaymentInitializedEvent {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
    }
}
