package dev.labs.commerce.order.core.order.domain;

public enum OrderStatus {
    PENDING,    // 주문 생성
    PAID,       // 결제 완료
    ABORTED,    // 시스템에 의한 중단 (재고 부족, 결제 실패)
    CANCELLED,  // 사용자 직접 취소
    FAILED,     // 시스템 오류 등 비정상 종료
    EXPIRED     // 일정 시간 내 결제가 완료되지 않음
}
