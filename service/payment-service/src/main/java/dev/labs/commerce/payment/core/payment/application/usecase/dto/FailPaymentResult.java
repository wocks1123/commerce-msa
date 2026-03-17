package dev.labs.commerce.payment.core.payment.application.usecase.dto;

import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record FailPaymentResult(
        String paymentId,
        String orderId,
        PaymentStatus status,
        @Nullable String failureCode,
        @Nullable String failureMessage,
        @Nullable Instant failedAt
) {
    public static FailPaymentResult failed(Payment payment) {
        return new FailPaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getFailedAt()
        );
    }

    public static FailPaymentResult ofCurrentState(Payment payment) {
        return new FailPaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getFailedAt()
        );
    }
}
