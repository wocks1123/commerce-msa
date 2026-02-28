package dev.labs.commerce.product.api.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;

public record ChangeProductStatusRequest(
        ProductStatus status
) {
}
