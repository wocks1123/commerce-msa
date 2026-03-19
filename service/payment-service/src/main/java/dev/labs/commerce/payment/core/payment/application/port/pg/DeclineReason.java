package dev.labs.commerce.payment.core.payment.application.port.pg;

public enum DeclineReason {
    INSUFFICIENT_BALANCE, // 잔액 또는 한도 부족
    CARD_EXPIRED,         // 카드 유효기간 만료
    DECLINED_BY_ISSUER,   // 발급사 거절 (구체적 사유 미제공)
    UNKNOWN               // PG 응답 코드를 매핑할 수 없는 경우
}
