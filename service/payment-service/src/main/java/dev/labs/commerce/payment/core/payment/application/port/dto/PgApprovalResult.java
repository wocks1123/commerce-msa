package dev.labs.commerce.payment.core.payment.application.port.dto;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record PgApprovalResult(
        boolean success,
        boolean aborted,
        @Nullable String pgTxId,
        long approvedAmount,
        @Nullable Instant approvedAt,
        @Nullable String failureCode,
        @Nullable String failureMessage
) {
    public static PgApprovalResult success(String pgTxId, long approvedAmount, Instant approvedAt) {
        return new PgApprovalResult(true, false, pgTxId, approvedAmount, approvedAt, null, null);
    }

    public static PgApprovalResult failure(String failureCode, @Nullable String failureMessage) {
        return new PgApprovalResult(false, false, null, 0, null, failureCode, failureMessage);
    }

    public static PgApprovalResult ofAborted(String failureCode, @Nullable String failureMessage) {
        return new PgApprovalResult(false, true, null, 0, null, failureCode, failureMessage);
    }

}
