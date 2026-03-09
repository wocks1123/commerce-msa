package dev.labs.commerce.payment.core.payment.application.event;

import java.time.Instant;

public record PaymentApprovedEvent(
        String paymentId,
        String orderId,
        long customerId,
        long amount,
        String currency,
        Instant approvedAt
) {
}
