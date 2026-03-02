package dev.labs.commerce.payment.core.payment.infra.pg.mock;

import dev.labs.commerce.payment.core.payment.application.port.PgGateway;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component("MOCK_PAY")
public class MockPgGateway implements PgGateway {

    @Override
    public PgApprovalResult approve(PgApprovalCommand command) {
        return PgApprovalResult.success(command.pgTxId(), command.amount(), Instant.now());
    }

}
