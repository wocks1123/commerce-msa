package dev.labs.commerce.product.core.product.application.usecase.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;

import java.time.Instant;

public record ChangeProductStatusResult(
        Long productId,
        String productName,
        Long price,
        String currency,
        ProductStatus productStatus,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
