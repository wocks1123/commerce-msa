package dev.labs.commerce.order.api.messaging.dto;

public record PaymentFailedKafkaEvent(
        String orderId,
        String errorCode
) {
}
