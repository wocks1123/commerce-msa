package dev.labs.commerce.product.core.product.domain;

public enum ProductStatus {
    DRAFT,         // 등록 직후, 검수/준비 중
    INACTIVE,      // 판매 중단 (한 번 이상 활성화됐던 상품)
    ACTIVE,        // 판매 중
    DISCONTINUED;  // 판매 종료 (재판매 불가)

    public boolean canTransitionTo(ProductStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case DRAFT -> target == ACTIVE || target == DISCONTINUED;
            case ACTIVE -> target == INACTIVE || target == DISCONTINUED;
            case INACTIVE -> target == ACTIVE || target == DISCONTINUED;
            case DISCONTINUED -> false;
        };
    }
}
