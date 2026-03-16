package dev.labs.commerce.product.api.http.dto;

import dev.labs.commerce.product.core.product.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeProductStatusRequest(
        @NotNull ProductStatus status
) {
}
