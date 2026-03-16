package dev.labs.commerce.inventory.api.http.dto;

import jakarta.validation.constraints.Positive;

public record IncreaseInventoryQuantityRequest(
        @Positive int quantity
) {
}
