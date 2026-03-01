package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_ALREADY_EXISTS("PAYMENT_ALREADY_EXISTS", "Payment request has already been processed."),
    PAYMENT_INVALID_STATUS("PAYMENT_INVALID_STATUS", "Operation not allowed in the current payment status.");

    private final String code;
    private final String message;
}
