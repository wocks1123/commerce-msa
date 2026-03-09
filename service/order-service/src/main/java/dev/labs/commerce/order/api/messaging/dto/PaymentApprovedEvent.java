package dev.labs.commerce.order.api.messaging.dto;

public record PaymentApprovedEvent(
        String orderId,
        long customerId,
        long amount,
        String currency
) {
}
