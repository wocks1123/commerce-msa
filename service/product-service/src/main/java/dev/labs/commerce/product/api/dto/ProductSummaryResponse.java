package dev.labs.commerce.product.api.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;

public record ProductSummaryResponse(
        Long productId,
        String productName,
        Long price,
        String currency,
        ProductStatus productStatus
) {
}
