package dev.labs.commerce.payment.core.payment.application.port.pg;

import lombok.Getter;

/**
 * PG 승인 요청의 처리 결과를 알 수 없는 경우 (타임아웃, 5xx, 네트워크 오류).
 * PG가 실제로 승인을 완료했을 수 있으므로 결제를 FAILED로 확정하지 않는다.
 */
@Getter
public class PgUncertainException extends RuntimeException {

    private final String failureCode;
    private final String failureMessage;
    private final boolean retryable;

    public PgUncertainException(String failureCode, String failureMessage, boolean retryable) {
        super(failureMessage);
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.retryable = retryable;
    }

    public PgUncertainException(String failureCode, String failureMessage, boolean retryable, Throwable cause) {
        super(failureMessage, cause);
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.retryable = retryable;
    }

}
