package dev.labs.commerce.common.event;

import dev.labs.commerce.common.error.CommonErrorCode;
import dev.labs.commerce.common.error.DependencyException;

public class EventPayloadConversionException extends DependencyException {
    public EventPayloadConversionException(String message) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public EventPayloadConversionException(String message, Throwable cause) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, message);
        this.initCause(cause);
    }
}
