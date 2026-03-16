package dev.labs.commerce.product.api.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ModifyProductRequest(
        @NotBlank String productName,
        @NotNull @Positive Long price,
        @NotBlank String currency,
        String description
) {
}
