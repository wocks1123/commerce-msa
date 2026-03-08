package dev.labs.commerce.payment.api.http.dto;

public record MockPaySuccessResponse(
        String paymentId,
        String status
) {
}
