package dev.labs.commerce.product.api.http.dto;

public record RegisterProductRequest(
        String productName,
        Long price,
        String currency,
        String description
) {
}
