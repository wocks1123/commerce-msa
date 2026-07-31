package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ValidationException;
import lombok.Getter;

/**
 * 결제 요청 값이 주문 원본과 불일치할 때 발생한다.
 */
@Getter
public class PaymentOrderMismatchException extends ValidationException {

    private final Field field;

    public PaymentOrderMismatchException(Field field) {
        super(PaymentErrorCode.ORDER_MISMATCH);
        this.field = field;
    }

    public enum Field {
        CUSTOMER_ID, AMOUNT, CURRENCY
    }
}
