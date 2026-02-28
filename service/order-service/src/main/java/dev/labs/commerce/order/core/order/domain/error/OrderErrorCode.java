package dev.labs.commerce.order.core.order.domain.error;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    PRODUCT_NOT_ACTIVE("PRODUCT_NOT_ACTIVE", "주문 가능한 상태의 상품이 아닙니다."),
    LINE_AMOUNT_MISMATCH("LINE_AMOUNT_MISMATCH", "상품 금액 정보가 일치하지 않습니다."),
    TOTAL_PRICE_MISMATCH("TOTAL_PRICE_MISMATCH", "주문 총 금액 정보가 일치하지 않습니다."),
    TOTAL_AMOUNT_MISMATCH("TOTAL_AMOUNT_MISMATCH", "주문 총 수량 정보가 일치하지 않습니다."),
    PRODUCT_SERVICE_UNAVAILABLE("PRODUCT_SERVICE_UNAVAILABLE", "Product service is unavailable."),
    PRODUCT_SERVICE_TIMEOUT("PRODUCT_SERVICE_TIMEOUT", "Product service request timed out.");

    private final String code;
    private final String message;
}
