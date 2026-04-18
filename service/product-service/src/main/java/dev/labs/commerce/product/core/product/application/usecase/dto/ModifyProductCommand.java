package dev.labs.commerce.product.core.product.application.usecase.dto;

public record ModifyProductCommand(
        Long productId,
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        String description
) {
}
