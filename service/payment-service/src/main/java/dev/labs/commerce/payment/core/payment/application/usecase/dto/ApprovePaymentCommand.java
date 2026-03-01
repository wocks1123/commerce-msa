package dev.labs.commerce.payment.core.payment.application.usecase.dto;

public record ApprovePaymentCommand(
        String paymentId,
        String paymentKey
) {
}
