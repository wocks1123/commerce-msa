package dev.labs.commerce.payment.core.payment.application.port.dto;

import java.time.Instant;

public record PgApprovalResult(
        String pgTxId,
        long approvedAmount,
        Instant approvedAt
) {
}
