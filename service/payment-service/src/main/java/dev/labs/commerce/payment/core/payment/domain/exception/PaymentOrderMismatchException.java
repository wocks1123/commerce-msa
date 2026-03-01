package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ValidationException;

public class PaymentOrderMismatchException extends ValidationException {

    public PaymentOrderMismatchException(String commandOrderId, String paymentOrderId) {
        super(PaymentErrorCode.PAYMENT_ORDER_MISMATCH,
                "command.orderId=" + commandOrderId + ", payment.orderId=" + paymentOrderId);
    }
}
