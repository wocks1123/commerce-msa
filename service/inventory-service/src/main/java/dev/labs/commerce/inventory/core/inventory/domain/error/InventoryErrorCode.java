package dev.labs.commerce.inventory.core.inventory.domain.error;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "재고가 부족합니다."),
    RESERVED_QUANTITY_UNDERFLOW("RESERVED_QUANTITY_UNDERFLOW", "예약 수량이 부족합니다.");

    private final String code;
    private final String message;
}
