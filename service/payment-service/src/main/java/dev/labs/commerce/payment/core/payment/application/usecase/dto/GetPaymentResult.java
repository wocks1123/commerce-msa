package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record GetPaymentResult(
        String paymentId,
        String orderId,
        long customerId,
        PaymentStatus status,
        long amount,
        String currency,
        PgProvider pgProvider,
        @Nullable String pgTxId,
        @Nullable String failureCode,
        @Nullable String failureMessage,
        Instant requestedAt,
        @Nullable Instant inProgressAt,
        @Nullable Instant approvedAt,
        @Nullable Instant failedAt,
        @Nullable Instant abortedAt,
        @Nullable Instant canceledAt
) {
}
