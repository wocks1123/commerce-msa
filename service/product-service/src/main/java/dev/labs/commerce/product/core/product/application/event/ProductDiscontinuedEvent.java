package dev.labs.commerce.product.core.product.application.event;

import java.util.Objects;

public record ProductDiscontinuedEvent(Long productId) {
    public ProductDiscontinuedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
    }
}
