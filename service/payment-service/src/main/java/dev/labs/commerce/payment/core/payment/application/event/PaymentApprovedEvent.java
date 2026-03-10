package dev.labs.commerce.payment.core.payment.application.event;

import java.time.Instant;
import java.util.Objects;

public record PaymentApprovedEvent(
        String paymentId,
        String orderId,
        long customerId,
        long amount,
        String currency,
        Instant approvedAt
) {
    public PaymentApprovedEvent {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(approvedAt, "approvedAt must not be null");
    }
}
