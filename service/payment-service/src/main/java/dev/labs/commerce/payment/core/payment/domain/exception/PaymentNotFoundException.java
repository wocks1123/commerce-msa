package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(String paymentId) {
        super(PaymentErrorCode.PAYMENT_NOT_FOUND, "paymentId=" + paymentId);
    }
}
