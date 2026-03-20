package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record FinalizePaymentResult(
        String paymentId,
        String orderId,
        PaymentStatus status,
        long amount,
        String currency,
        @Nullable String pgTxId,
        @Nullable Instant approvedAt,
        @Nullable String failureCode,
        @Nullable String failureMessage
) {
    public static FinalizePaymentResult of(Payment payment) {
        return new FinalizePaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPgTxId(),
                payment.getApprovedAt(),
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }
}
