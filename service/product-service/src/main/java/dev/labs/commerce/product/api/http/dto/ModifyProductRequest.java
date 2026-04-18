package dev.labs.commerce.product.api.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ModifyProductRequest(
        @NotBlank String productName,
        @NotNull @PositiveOrZero Long listPrice,
        @NotNull @PositiveOrZero Long sellingPrice,
        @NotBlank String currency,
        String description
) {
}
