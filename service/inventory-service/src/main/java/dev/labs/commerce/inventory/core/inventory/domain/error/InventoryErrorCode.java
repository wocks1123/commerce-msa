package dev.labs.commerce.inventory.core.inventory.domain.error;

import dev.labs.commerce.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_NOT_FOUND("INVENTORY_NOT_FOUND", "재고 정보를 찾을 수 없습니다."),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "재고가 부족합니다."),
    RESERVED_QUANTITY_UNDERFLOW("RESERVED_QUANTITY_UNDERFLOW", "예약 수량이 부족합니다."),
    INVALID_QUANTITY("INVALID_QUANTITY", "수량은 1 이상이어야 합니다.");

    private final String code;
    private final String message;
}
