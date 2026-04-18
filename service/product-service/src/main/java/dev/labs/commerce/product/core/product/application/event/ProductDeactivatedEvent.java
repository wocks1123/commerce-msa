package dev.labs.commerce.product.core.product.application.event;

import java.util.Objects;

public record ProductDeactivatedEvent(Long productId) {
    public ProductDeactivatedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
    }
}
