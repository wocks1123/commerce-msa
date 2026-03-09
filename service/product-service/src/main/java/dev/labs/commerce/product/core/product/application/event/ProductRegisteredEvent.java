package dev.labs.commerce.product.core.product.application.event;

import java.util.Objects;

public record ProductRegisteredEvent(Long productId) {
    public ProductRegisteredEvent {
        Objects.requireNonNull(productId, "productId must not be null");
    }
}
