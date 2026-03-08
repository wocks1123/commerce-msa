package dev.labs.commerce.payment.api.http.dto;

public record MockPayFailResponse(
        String paymentId,
        String status
) {
}
