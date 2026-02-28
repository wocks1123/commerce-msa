package dev.labs.commerce.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR("INTERNAL_ERROR", "An unexpected server error occurred."),
    INVALID_INPUT_VALUE("INVALID_INPUT", "Invalid request parameters."),
    DEPENDENCY_FAILURE("DEPENDENCY_FAILURE", "External dependency failure."),
    EVENT_PUBLISH_FAILURE("EVENT_PUBLISH_ERROR", "Failed to publish domain event.");

    private final String code;
    private final String message;
}
