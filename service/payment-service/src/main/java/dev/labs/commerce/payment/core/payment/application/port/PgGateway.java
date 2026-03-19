package dev.labs.commerce.payment.core.payment.application.port;

import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;

public interface PgGateway {

    /**
     * PG 승인 요청
     *
     * @throws dev.labs.commerce.payment.core.payment.application.port.pg.PgDeclinedException  결제 승인을 명확히 거절한 경우 (잔액부족, 카드정지 등)
     * @throws dev.labs.commerce.payment.core.payment.application.port.pg.PgUncertainException 요청의 처리 결과를 알 수 없는 경우 (타임아웃, 5xx 등)
     */
    PgApprovalResult approve(PgApprovalCommand command);

}