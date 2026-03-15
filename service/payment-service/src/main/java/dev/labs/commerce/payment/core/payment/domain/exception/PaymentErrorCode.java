package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_ALREADY_EXISTS("PAYMENT_ALREADY_EXISTS", "Payment request has already been processed."),
    PAYMENT_INVALID_STATUS("PAYMENT_INVALID_STATUS", "Operation not allowed in the current payment status."),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Payment not found."),
    PAYMENT_APPROVAL_FAILED("PAYMENT_APPROVAL_FAILED", "Payment approval failed by PG."),
    PAYMENT_AMOUNT_MISMATCH("PAYMENT_AMOUNT_MISMATCH", "Approved amount does not match the requested amount."),
    PAYMENT_ORDER_MISMATCH("PAYMENT_ORDER_MISMATCH", "Order ID does not match the payment record."),
    INVENTORY_SERVICE_UNAVAILABLE("INVENTORY_SERVICE_UNAVAILABLE", "Inventory service is unavailable."),
    INVENTORY_SERVICE_TIMEOUT("INVENTORY_SERVICE_TIMEOUT", "Inventory service request timed out.");

    private final String code;
    private final String message;
}
