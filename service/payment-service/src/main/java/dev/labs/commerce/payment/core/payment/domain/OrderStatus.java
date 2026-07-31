package dev.labs.commerce.payment.core.payment.domain;

public enum OrderStatus {
    CREATED,
    PENDING,
    PAID,
    ABORTED,
    CANCELLED,
    FAILED,
    EXPIRED,
    UNKNOWN
}
