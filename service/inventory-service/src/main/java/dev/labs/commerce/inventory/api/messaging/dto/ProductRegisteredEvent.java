package dev.labs.commerce.inventory.api.messaging.dto;

import jakarta.validation.constraints.NotNull;

public record ProductRegisteredEvent(@NotNull Long productId) {
}
