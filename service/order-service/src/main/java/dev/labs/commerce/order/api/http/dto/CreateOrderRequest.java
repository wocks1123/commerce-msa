package dev.labs.commerce.order.api.http.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @Positive long customerId,
        @NotBlank String currency,
        @Positive long totalPrice,
        @Positive long totalAmount,
        @NotEmpty List<@Valid CreateOrderItemRequest> items
) {
}
