package dev.labs.commerce.payment.core.payment.application.port.pg;

import lombok.Getter;

/**
 * PG가 결제 승인을 명확히 거절한 경우 (4xx 계열)
 * 잔액 부족, 카드 정지 등 재시도해도 결과가 동일한 실패
 */
@Getter
public class PgDeclinedException extends RuntimeException {

    private final DeclineReason reason;
    private final String pgCode;
    private final String pgMessage;

    public PgDeclinedException(DeclineReason reason, String pgCode, String pgMessage) {
        super(pgMessage);
        this.reason = reason;
        this.pgCode = pgCode;
        this.pgMessage = pgMessage;
    }

}
