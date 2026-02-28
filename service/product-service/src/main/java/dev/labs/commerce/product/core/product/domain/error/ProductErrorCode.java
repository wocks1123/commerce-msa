package dev.labs.commerce.product.core.product.domain.error;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found."),
    INVALID_PRODUCT_STATUS("INVALID_PRODUCT_STATUS", "The product is in an invalid state for this operation.");

    private final String code;
    private final String message;
}
