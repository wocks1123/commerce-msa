package dev.labs.commerce.product.api.dto;

public record ModifyProductRequest(
        String productName,
        Long price,
        String currency,
        String description
) {
}
