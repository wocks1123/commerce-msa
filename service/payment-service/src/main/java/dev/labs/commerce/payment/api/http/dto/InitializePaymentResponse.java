package dev.labs.commerce.payment.api.http.dto;

import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;

import java.time.Instant;

public record InitializePaymentResponse(
        String paymentId,
        String orderId,
        PaymentStatus status,
        long amount,
        String currency,
        Instant requestedAt
) {
}
