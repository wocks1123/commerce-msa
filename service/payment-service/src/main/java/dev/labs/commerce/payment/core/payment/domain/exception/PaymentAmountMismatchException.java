package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ValidationException;

public class PaymentAmountMismatchException extends ValidationException {

    public PaymentAmountMismatchException(long expected, long actual) {
        super(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH,
                "expected=" + expected + ", actual=" + actual);
    }
}
