package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public sealed interface ApprovePaymentResult
        permits ApprovePaymentResult.Approved, ApprovePaymentResult.Failed,
        ApprovePaymentResult.Aborted, ApprovePaymentResult.AlreadyProcessed {

    String paymentId();

    String orderId();

    PaymentStatus status();

    static ApprovePaymentResult approved(Payment payment) {
        return new Approved(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPgTxId(),
                payment.getApprovedAt()
        );
    }

    static ApprovePaymentResult failed(Payment payment) {
        return new Failed(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }

    static ApprovePaymentResult aborted(Payment payment) {
        return new Aborted(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }

    static ApprovePaymentResult ofCurrentState(Payment payment) {
        return new AlreadyProcessed(
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

    record Approved(
            String paymentId,
            String orderId,
            PaymentStatus status,
            long amount,
            String currency,
            String pgTxId,
            Instant approvedAt
    ) implements ApprovePaymentResult {
    }

    record Failed(
            String paymentId,
            String orderId,
            PaymentStatus status,
            long amount,
            String currency,
            @Nullable String failureCode,
            @Nullable String failureMessage
    ) implements ApprovePaymentResult {
    }

    record Aborted(
            String paymentId,
            String orderId,
            PaymentStatus status,
            long amount,
            String currency,
            @Nullable String failureCode,
            @Nullable String failureMessage
    ) implements ApprovePaymentResult {
    }

    record AlreadyProcessed(
            String paymentId,
            String orderId,
            PaymentStatus status,
            long amount,
            String currency,
            @Nullable String pgTxId,
            @Nullable Instant approvedAt,
            @Nullable String failureCode,
            @Nullable String failureMessage
    ) implements ApprovePaymentResult {
    }

}
