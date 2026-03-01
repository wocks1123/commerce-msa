package dev.labs.commerce.payment.core.payment.application.port.dto;

import java.time.Instant;

public record PgApprovalResult(
        boolean success,
        String pgTxId,          // 승인 거래번호
        long approvedAmount,    // 실제 승인된 금액
        Instant approvedAt,
        String failureCode,     // 실패 시
        String failureMessage   // 실패 시
) {
    public static PgApprovalResult success(String pgTxId, long approvedAmount, Instant approvedAt) {
        return new PgApprovalResult(true, pgTxId, approvedAmount, approvedAt, null, null);
    }

    public static PgApprovalResult failure(String failureCode, String failureMessage) {
        return new PgApprovalResult(false, null, 0, null, failureCode, failureMessage);
    }
}
