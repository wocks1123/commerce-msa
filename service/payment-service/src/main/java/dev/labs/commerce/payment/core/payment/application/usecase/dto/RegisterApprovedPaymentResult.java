package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;

import java.time.Instant;

public record RegisterApprovedPaymentResult(
        String paymentId,
        String orderId,
        PaymentStatus status,
        long amount,
        String currency,
        String pgTxId,
        Instant approvedAt
) {
}
