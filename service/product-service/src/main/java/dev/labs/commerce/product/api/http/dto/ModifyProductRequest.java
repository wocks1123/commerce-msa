package dev.labs.commerce.product.api.http.dto;

import dev.labs.commerce.product.core.product.domain.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ModifyProductRequest(
        @NotBlank String productName,
        @NotNull @PositiveOrZero Long listPrice,
        @NotNull @PositiveOrZero Long sellingPrice,
        @NotBlank String currency,
        @NotNull ProductCategory category,
        Instant saleStartAt,
        Instant saleEndAt,
        @Size(max = 500) String thumbnailUrl,
        String description
) {
}
