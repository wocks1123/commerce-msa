package dev.labs.commerce.payment.core.payment.application.port;

import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;

public interface PgGateway {

    PgApprovalResult approve(PgApprovalCommand command);

}