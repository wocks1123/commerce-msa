package dev.labs.commerce.product.api.http.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;

import java.time.Instant;

public record ProductResponse(
        Long productId,
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        ProductStatus productStatus,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
