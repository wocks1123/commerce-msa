package dev.labs.commerce.product.core.product.application.usecase.dto;

import dev.labs.commerce.product.core.product.domain.ProductCategory;

import java.time.Instant;

public record ModifyProductCommand(
        Long productId,
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        ProductCategory category,
        Instant saleStartAt,
        Instant saleEndAt,
        String thumbnailUrl,
        String description
) {
}
