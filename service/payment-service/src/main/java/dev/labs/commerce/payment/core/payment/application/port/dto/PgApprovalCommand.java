package dev.labs.commerce.payment.core.payment.application.port.dto;

public record PgApprovalCommand(
        String pgTxId,
        String orderId,
        long customerId,
        long amount
) {
}
