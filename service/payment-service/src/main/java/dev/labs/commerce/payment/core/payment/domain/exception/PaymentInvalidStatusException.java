package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ValidationException;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PaymentInvalidStatusException extends ValidationException {

    public PaymentInvalidStatusException(PaymentStatus current, PaymentStatus required) {
        super(PaymentErrorCode.PAYMENT_INVALID_STATUS, "required=" + required + ", current=" + current);
    }

    public PaymentInvalidStatusException(PaymentStatus current, PaymentStatus... required) {
        super(PaymentErrorCode.PAYMENT_INVALID_STATUS,
                "required=" + Arrays.stream(required).map(Enum::name).collect(Collectors.joining(" or "))
                        + ", current=" + current);
    }

}
