package dev.labs.commerce.payment.core.payment.domain.exception;

public class OrderClientException extends RuntimeException {

    public OrderClientException(String message) {
        super(message);
    }
}
