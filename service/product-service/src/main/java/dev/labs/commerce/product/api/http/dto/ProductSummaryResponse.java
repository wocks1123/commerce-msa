package dev.labs.commerce.product.api.http.dto;

import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductStatus;

public record ProductSummaryResponse(
        Long productId,
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        ProductStatus productStatus,
        ProductCategory category,
        String thumbnailUrl
) {
}
