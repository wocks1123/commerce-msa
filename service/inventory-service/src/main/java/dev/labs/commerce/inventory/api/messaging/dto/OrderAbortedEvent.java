package dev.labs.commerce.inventory.api.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderAbortedEvent(
        @NotBlank String orderId,
        @NotEmpty List<@Valid OrderItemPayload> items
) {
    public record OrderItemPayload(
            @NotNull Long productId,
            int quantity
    ) {
    }
}
