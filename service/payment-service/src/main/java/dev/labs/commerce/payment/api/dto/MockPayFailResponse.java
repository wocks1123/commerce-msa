package dev.labs.commerce.payment.api.dto;

public record MockPayFailResponse(
        String paymentId,
        String status
) {
}
