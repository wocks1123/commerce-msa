package dev.labs.commerce.order.core.order.domain;

public record ProductInfo(
        Long productId,
        String productName,
        Long price,
        String currency,
        String productStatus
) {
}
