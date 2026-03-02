package dev.labs.commerce.order.core.order.domain.error;

import dev.labs.commerce.common.error.ValidationException;

public class InvalidOrderStateException extends ValidationException {

    public InvalidOrderStateException() {
        super(OrderErrorCode.INVALID_ORDER_STATE);
    }
}
