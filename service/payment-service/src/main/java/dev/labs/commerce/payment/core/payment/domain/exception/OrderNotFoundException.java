package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(String orderId) {
        super(PaymentErrorCode.ORDER_NOT_FOUND, "orderId=" + orderId);
    }
}
