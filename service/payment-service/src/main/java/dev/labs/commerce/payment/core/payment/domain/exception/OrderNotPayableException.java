package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ConflictException;
import dev.labs.commerce.payment.core.payment.domain.OrderStatus;

public class OrderNotPayableException extends ConflictException {

    public OrderNotPayableException(String orderId, OrderStatus current) {
        super(PaymentErrorCode.ORDER_NOT_PAYABLE, "orderId=" + orderId + ", required=" + OrderStatus.CREATED + ", current=" + current);
    }
}
