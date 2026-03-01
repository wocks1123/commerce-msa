package dev.labs.commerce.order.core.order.domain;

public enum OrderStatus {
    PENDING,            // 주문 생성
    PAYMENT_PENDING,    // 재고 감소 완료, 결제 대기
    PAID,               // 결제 완료, 주문 확정
    CANCELLED,          // 재고 부족 or 결제 실패로 취소
    FAILED              // 시스템 오류 등 비정상 종료
}
