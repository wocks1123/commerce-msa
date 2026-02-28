package dev.labs.commerce.common.error;

public class DependencyException extends CoreException {
    public DependencyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DependencyException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
