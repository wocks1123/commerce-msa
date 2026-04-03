package dev.labs.commerce.payment.core.payment.application.event;

import java.time.Instant;
import java.util.Objects;

public record PaymentExpiredEvent(
        String paymentId,
        String orderId,
        long customerId,
        Instant expiredAt
) {
    public PaymentExpiredEvent {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(expiredAt, "expiredAt must not be null");
    }
}
