package dev.labs.commerce.common.event;

import dev.labs.commerce.common.error.CommonErrorCode;
import dev.labs.commerce.common.error.DependencyException;
import dev.labs.commerce.common.error.ErrorCode;

public class EventPublishException extends DependencyException {
    public EventPublishException(String message) {
        super(CommonErrorCode.EVENT_PUBLISH_FAILURE, message);
    }

    public EventPublishException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
