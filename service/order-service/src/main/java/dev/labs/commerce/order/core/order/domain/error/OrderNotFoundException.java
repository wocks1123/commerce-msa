package dev.labs.commerce.order.core.order.domain.error;

import dev.labs.commerce.common.error.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND);
    }
}
