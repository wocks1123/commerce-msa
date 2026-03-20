package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record SkipPaymentResult(
        String paymentId,
        String orderId,
        PaymentStatus status,
        long amount,
        String currency,
        @Nullable String pgTxId,
        @Nullable Instant approvedAt,
        @Nullable String failureCode,
        @Nullable String failureMessage
) implements PreparePaymentResult {
}
