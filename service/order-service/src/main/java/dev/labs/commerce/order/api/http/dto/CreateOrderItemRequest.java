package dev.labs.commerce.order.api.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(
        @Positive long productId,
        @Positive long unitPrice,
        @Positive int quantity,
        @Positive long lineAmount,
        @NotBlank String currency
) {
}
