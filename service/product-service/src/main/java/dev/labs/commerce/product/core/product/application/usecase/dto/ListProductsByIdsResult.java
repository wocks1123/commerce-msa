package dev.labs.commerce.product.core.product.application.usecase.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;

public record ListProductsByIdsResult(
        Long productId,
        String productName,
        Long price,
        String currency,
        ProductStatus productStatus
) {
}
