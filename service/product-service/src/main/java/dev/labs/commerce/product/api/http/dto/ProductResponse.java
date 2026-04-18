package dev.labs.commerce.product.api.http.dto;

import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductStatus;

import java.time.Instant;

public record ProductResponse(
        Long productId,
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        ProductStatus productStatus,
        ProductCategory category,
        Instant saleStartAt,
        Instant saleEndAt,
        String thumbnailUrl,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
