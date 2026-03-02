package dev.labs.commerce.order.core.order.infra.messaging.dto;

public record PaymentApprovedKafkaEvent(
        String orderId,
        long customerId,
        long amount,
        String currency
) {
}
