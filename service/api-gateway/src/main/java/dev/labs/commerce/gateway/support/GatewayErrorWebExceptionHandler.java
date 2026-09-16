package dev.labs.commerce.gateway.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 게이트웨이 자체 에러를 다른 서비스와 동일한 ProblemDetail 포맷으로 반환한다.
 */
@Component
@Order(-1)
@RequiredArgsConstructor
@Slf4j
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private static final URI DEFAULT_TYPE = URI.create("about:blank");

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        final ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        final String path = exchange.getRequest().getPath().value();
        final HttpStatus status = resolveStatus(ex);
        final GatewayErrorCode errorCode = resolveErrorCode(status);

        if (status.is5xxServerError()) {
            log.error("Gateway error at {} {}: {}",
                    exchange.getRequest().getMethod(), path, ex.toString(), ex);
        } else {
            log.warn("Gateway rejected {} {}: {}",
                    exchange.getRequest().getMethod(), path, ex.toString());
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        final DataBuffer buffer = response.bufferFactory().wrap(serialize(status, errorCode, path));
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            final HttpStatus resolved = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            if (resolved != null) {
                return resolved;
            }
        }
        if (hasCause(ex, ConnectException.class)) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (isTimeout(ex)) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private GatewayErrorCode resolveErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> GatewayErrorCode.ROUTE_NOT_FOUND;
            case SERVICE_UNAVAILABLE -> GatewayErrorCode.SERVICE_UNAVAILABLE;
            case GATEWAY_TIMEOUT -> GatewayErrorCode.GATEWAY_TIMEOUT;
            case INTERNAL_SERVER_ERROR -> GatewayErrorCode.GATEWAY_INTERNAL_ERROR;
            default -> GatewayErrorCode.GATEWAY_ERROR;
        };
    }

    private byte[] serialize(HttpStatus status, GatewayErrorCode errorCode, String path) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, errorCode.getMessage());
        problemDetail.setType(DEFAULT_TYPE);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(path));
        problemDetail.setProperty("code", errorCode.getCode());

        try {
            return objectMapper.writeValueAsBytes(problemDetail);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ProblemDetail", e);
            return ("{\"status\":" + status.value() + ",\"code\":\"" + errorCode.getCode() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 프록시 과정에서 원인 예외는 여러 겹으로 감싸여 올라온다. 체인 전체를 훑는다.
     */
    private static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
        Throwable current = ex;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            if (current.getCause() == current) {
                return false;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Reactor Netty의 응답 타임아웃은 {@code java.util.concurrent.TimeoutException}이 아니라
     * Netty 자체 타임아웃 예외로 올라오므로 둘 다 확인한다.
     */
    private static boolean isTimeout(Throwable ex) {
        return hasCause(ex, java.util.concurrent.TimeoutException.class)
                || hasCause(ex, io.netty.handler.timeout.TimeoutException.class);
    }
}
