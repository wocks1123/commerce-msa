package dev.labs.commerce.product.api.dto;

public record RegisterProductRequest(
        String productName,
        Long price,
        String currency,
        String description
) {
}
