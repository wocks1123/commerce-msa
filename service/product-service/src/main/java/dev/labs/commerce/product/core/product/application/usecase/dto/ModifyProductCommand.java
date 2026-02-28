package dev.labs.commerce.product.core.product.application.usecase.dto;

public record ModifyProductCommand(
        Long productId,
        String productName,
        Long price,
        String currency,
        String description
) {
}
