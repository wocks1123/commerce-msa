package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record ApprovePaymentResult(
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
    public static ApprovePaymentResult approved(Payment payment) {
        return new ApprovePaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPgTxId(),
                payment.getApprovedAt(),
                null,
                null
        );
    }

    public static ApprovePaymentResult failed(Payment payment) {
        return new ApprovePaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                null,
                null,
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }

    public static ApprovePaymentResult ofCurrentState(Payment payment) {
        return new ApprovePaymentResult(
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
