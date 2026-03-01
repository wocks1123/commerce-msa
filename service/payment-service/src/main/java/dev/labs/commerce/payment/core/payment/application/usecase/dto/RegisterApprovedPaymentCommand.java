package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import java.time.Instant;

public record RegisterApprovedPaymentCommand(
        String orderId,
        long customerId,
        long amount,
        String currency,
        String idempotencyKey,
        String pgProvider,
        String pgTxId,
        Instant approvedAt
) {
}
