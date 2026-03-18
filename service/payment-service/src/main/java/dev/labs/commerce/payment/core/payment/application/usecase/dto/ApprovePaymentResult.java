package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.application.command.FinalizePaymentResult;
import dev.labs.commerce.payment.core.payment.application.command.SkipPaymentResult;
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

    public static ApprovePaymentResult of(SkipPaymentResult result) {
        return new ApprovePaymentResult(
                result.paymentId(),
                result.orderId(),
                result.status(),
                result.amount(),
                result.currency(),
                result.pgTxId(),
                result.approvedAt(),
                result.failureCode(),
                result.failureMessage()
        );
    }

    public static ApprovePaymentResult of(FinalizePaymentResult result) {
        return new ApprovePaymentResult(
                result.paymentId(),
                result.orderId(),
                result.status(),
                result.amount(),
                result.currency(),
                result.pgTxId(),
                result.approvedAt(),
                result.failureCode(),
                result.failureMessage()
        );
    }

}
