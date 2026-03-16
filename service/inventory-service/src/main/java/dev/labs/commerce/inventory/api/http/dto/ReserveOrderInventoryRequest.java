package dev.labs.commerce.inventory.api.http.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ReserveOrderInventoryRequest(
        @NotBlank String orderId,
        @NotEmpty List<@Valid Item> items
) {

    public record Item(
            @NotNull Long productId,
            @Positive int quantity
    ) {
    }

}
