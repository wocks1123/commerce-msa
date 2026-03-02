package dev.labs.commerce.order.core.order.domain.error;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found."),
    INVALID_ORDER_STATE("INVALID_ORDER_STATE", "Operation not allowed in the current order state."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found."),
    PRODUCT_NOT_ACTIVE("PRODUCT_NOT_ACTIVE", "Product is not available for ordering."),
    LINE_AMOUNT_MISMATCH("LINE_AMOUNT_MISMATCH", "Line amount does not match."),
    TOTAL_PRICE_MISMATCH("TOTAL_PRICE_MISMATCH", "Total price does not match."),
    TOTAL_AMOUNT_MISMATCH("TOTAL_AMOUNT_MISMATCH", "Total amount does not match."),
    CUSTOMER_ID_MISMATCH("CUSTOMER_ID_MISMATCH", "Customer does not match the order."),
    CURRENCY_MISMATCH("CURRENCY_MISMATCH", "Currency does not match the order."),
    PRODUCT_SERVICE_UNAVAILABLE("PRODUCT_SERVICE_UNAVAILABLE", "Product service is unavailable."),
    PRODUCT_SERVICE_TIMEOUT("PRODUCT_SERVICE_TIMEOUT", "Product service request timed out.");

    private final String code;
    private final String message;
}
