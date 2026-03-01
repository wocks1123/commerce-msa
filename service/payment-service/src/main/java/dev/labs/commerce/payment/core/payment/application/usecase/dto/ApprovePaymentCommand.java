package dev.labs.commerce.payment.core.payment.application.usecase.dto;

public record ApprovePaymentCommand(
        String orderId,
        String paymentKey,
        long paymentAmount
) {
}
