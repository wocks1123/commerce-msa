package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(PaymentErrorCode errorCode) {
        super(errorCode);
    }

    public InsufficientStockException(PaymentErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
