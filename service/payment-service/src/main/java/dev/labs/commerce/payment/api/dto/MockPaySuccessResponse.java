package dev.labs.commerce.payment.api.dto;

public record MockPaySuccessResponse(
        String paymentId,
        String status
) {
}
