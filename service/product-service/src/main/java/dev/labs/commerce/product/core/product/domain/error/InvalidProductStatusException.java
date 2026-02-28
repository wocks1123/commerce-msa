package dev.labs.commerce.product.core.product.domain.error;

import dev.labs.commerce.common.error.ValidationException;

public class InvalidProductStatusException extends ValidationException {
    public InvalidProductStatusException(ProductErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidProductStatusException(ProductErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
