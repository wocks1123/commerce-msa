package dev.labs.commerce.order.core.order.domain.error;

import dev.labs.commerce.common.error.ValidationException;

public class OrderProductInvalidException extends ValidationException {

    public OrderProductInvalidException(OrderErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
