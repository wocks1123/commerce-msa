package dev.labs.commerce.product.core.product.application.usecase.dto;

public record RegisterProductCommand(
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        String description
) {
}
