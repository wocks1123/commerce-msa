package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;

import java.time.Instant;

public record InitializePaymentResult(
        String paymentId,
        String orderId,
        PaymentStatus status,
        long amount,
        String currency,
        Instant requestedAt
) {
}
