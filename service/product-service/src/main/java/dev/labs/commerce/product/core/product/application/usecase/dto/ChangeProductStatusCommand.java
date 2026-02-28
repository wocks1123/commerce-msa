package dev.labs.commerce.product.core.product.application.usecase.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;

public record ChangeProductStatusCommand(
        Long productId,
        ProductStatus status
) {
}
