package dev.labs.commerce.product.core.product.domain.error;

import dev.labs.commerce.common.error.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(ProductErrorCode errorCode) {
        super(errorCode);
    }

    public ProductNotFoundException(ProductErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
