package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ConflictException;

public class PaymentAlreadyExistsException extends ConflictException {

    public PaymentAlreadyExistsException() {
        super(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
    }

    public PaymentAlreadyExistsException(String message) {
        super(PaymentErrorCode.PAYMENT_ALREADY_EXISTS, message);
    }
}
