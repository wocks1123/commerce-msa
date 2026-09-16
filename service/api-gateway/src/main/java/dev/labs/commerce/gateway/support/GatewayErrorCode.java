package dev.labs.commerce.gateway.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게이트웨이에서 만들어지는 자체 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum GatewayErrorCode {

    ROUTE_NOT_FOUND("ROUTE_NOT_FOUND", "No route matched the request path."),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Downstream service is unavailable."),
    GATEWAY_TIMEOUT("GATEWAY_TIMEOUT", "Downstream service did not respond in time."),
    GATEWAY_ERROR("GATEWAY_ERROR", "The gateway could not process the request."),
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected server error occurred.");

    private final String code;
    private final String message;
}
