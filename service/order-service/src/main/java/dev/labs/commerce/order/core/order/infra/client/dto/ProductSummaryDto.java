package dev.labs.commerce.order.core.order.infra.client.dto;

public record ProductSummaryDto(
        Long productId,
        String productName,
        Long sellingPrice,
        String currency,
        String productStatus
) {
}
