package dev.labs.commerce.product.core.product.application.event;

import dev.labs.commerce.product.core.product.domain.ProductCategory;

import java.time.Instant;
import java.util.Objects;

public record ProductModifiedEvent(
        Long productId,
        String productName,
        Long listPrice,
        Long sellingPrice,
        String currency,
        ProductCategory category,
        Instant saleStartAt,
        Instant saleEndAt,
        String thumbnailUrl,
        String description
) {
    public ProductModifiedEvent {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(productName, "productName must not be null");
        Objects.requireNonNull(listPrice, "listPrice must not be null");
        Objects.requireNonNull(sellingPrice, "sellingPrice must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(description, "description must not be null");
    }
}
