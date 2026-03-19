package dev.labs.commerce.payment.core.payment.infra.pg.mock;

import dev.labs.commerce.payment.core.payment.application.port.PgGateway;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.application.port.pg.DeclineReason;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgDeclinedException;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgUncertainException;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 테스트용 Mock PG Gateway.
 * pgTxId prefix로 응답을 제어한다:
 * - "decline_" → PgDeclinedException (명확한 거절)
 * - "uncertain_" → PgUncertainException (불확실한 실패)
 * - 그 외 → 승인 성공
 */
@Component("MOCK_PAY")
public class MockPgGateway implements PgGateway {

    @Override
    public PgApprovalResult approve(PgApprovalCommand command) {
        String pgTxId = command.pgTxId();

        if (pgTxId.startsWith("decline_")) {
            throw new PgDeclinedException(
                    DeclineReason.DECLINED_BY_ISSUER,
                    "CARD_DECLINED",
                    "Mock PG declined the payment"
            );
        }

        if (pgTxId.startsWith("uncertain_")) {
            throw new PgUncertainException(
                    "PG_SERVER_ERROR",
                    "Mock PG result is uncertain",
                    false
            );
        }

        return new PgApprovalResult(pgTxId, command.amount(), Instant.now());
    }
}
