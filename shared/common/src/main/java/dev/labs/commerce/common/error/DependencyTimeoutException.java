package dev.labs.commerce.common.error;

public class DependencyTimeoutException extends DependencyException {
    public DependencyTimeoutException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DependencyTimeoutException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
