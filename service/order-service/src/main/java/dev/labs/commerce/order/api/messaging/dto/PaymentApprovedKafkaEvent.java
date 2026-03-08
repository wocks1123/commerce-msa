package dev.labs.commerce.order.api.messaging.dto;

public record PaymentApprovedKafkaEvent(
        String orderId,
        long customerId,
        long amount,
        String currency
) {
}
