package dev.labs.commerce.order.core.order.application.usecase.dto;

public record ConfirmPaidCommand(
        String orderId,
        long customerId,
        long amount,
        String currency
) {
}
