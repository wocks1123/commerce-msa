package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_ALREADY_EXISTS("PAYMENT_ALREADY_EXISTS", "이미 처리된 결제 요청입니다.");

    private final String code;
    private final String message;
}
