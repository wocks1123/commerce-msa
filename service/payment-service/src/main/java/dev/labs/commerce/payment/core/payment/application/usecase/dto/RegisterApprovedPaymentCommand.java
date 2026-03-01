package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.PgProvider;

import java.time.Instant;

public record RegisterApprovedPaymentCommand(
        String orderId,
        long customerId,
        long amount,
        String currency,
        String idempotencyKey,
        PgProvider pgProvider,
        String pgTxId,
        Instant approvedAt
) {
}
