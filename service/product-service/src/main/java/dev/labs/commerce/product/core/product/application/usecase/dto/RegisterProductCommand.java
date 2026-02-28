package dev.labs.commerce.product.core.product.application.usecase.dto;

public record RegisterProductCommand(
        String productName,
        Long price,
        String currency,
        String description
) {
}
