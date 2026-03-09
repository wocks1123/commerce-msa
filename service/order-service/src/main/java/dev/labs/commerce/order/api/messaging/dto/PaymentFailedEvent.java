package dev.labs.commerce.order.api.messaging.dto;

public record PaymentFailedEvent(
        String orderId,
        String errorCode
) {
}
