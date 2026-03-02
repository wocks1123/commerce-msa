package dev.labs.commerce.order.core.order.infra.messaging.dto;

public record PaymentFailedKafkaEvent(
        String orderId,
        String errorCode
) {
}
