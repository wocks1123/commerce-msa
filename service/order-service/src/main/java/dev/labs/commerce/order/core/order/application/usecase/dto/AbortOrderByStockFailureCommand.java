package dev.labs.commerce.order.core.order.application.usecase.dto;

public record AbortOrderByStockFailureCommand(
        String orderId
) {
}
