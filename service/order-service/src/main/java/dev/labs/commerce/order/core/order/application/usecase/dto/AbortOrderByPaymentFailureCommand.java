package dev.labs.commerce.order.core.order.application.usecase.dto;

public record AbortOrderByPaymentFailureCommand(
        String orderId
) {
}
