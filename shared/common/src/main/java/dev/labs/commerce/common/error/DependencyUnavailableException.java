package dev.labs.commerce.common.error;

public class DependencyUnavailableException extends DependencyException {
    public DependencyUnavailableException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DependencyUnavailableException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
