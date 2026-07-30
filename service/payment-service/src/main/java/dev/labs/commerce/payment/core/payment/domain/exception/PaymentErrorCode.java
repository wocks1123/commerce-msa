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
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found."),
    ORDER_NOT_PAYABLE("ORDER_NOT_PAYABLE", "Order is not in a payable state."),
    ORDER_MISMATCH("ORDER_MISMATCH", "Requested payment information does not match the order."),
    ORDER_SERVICE_UNAVAILABLE("ORDER_SERVICE_UNAVAILABLE", "Order service is unavailable."),
    ORDER_SERVICE_TIMEOUT("ORDER_SERVICE_TIMEOUT", "Order service request timed out."),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "Insufficient stock."),
    INVENTORY_SERVICE_UNAVAILABLE("INVENTORY_SERVICE_UNAVAILABLE", "Inventory service is unavailable."),
    INVENTORY_SERVICE_TIMEOUT("INVENTORY_SERVICE_TIMEOUT", "Inventory service request timed out.");

    private final String code;
    private final String message;
}
