package dev.labs.commerce.order.core.order.domain.error;

import dev.labs.commerce.common.error.ValidationException;

public class OrderPaymentMismatchException extends ValidationException {

    public OrderPaymentMismatchException(OrderErrorCode errorCode) {
        super(errorCode);
    }
}
