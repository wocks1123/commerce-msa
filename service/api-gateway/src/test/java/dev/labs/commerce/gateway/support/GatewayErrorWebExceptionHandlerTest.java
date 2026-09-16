package dev.labs.commerce.gateway.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayErrorWebExceptionHandlerTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private final GatewayErrorWebExceptionHandler handler =
            new GatewayErrorWebExceptionHandler(objectMapper);

    @Test
    @DisplayName("매칭되는 라우트가 없으면 404와 ROUTE_NOT_FOUND를 반환한다")
    void returnsRouteNotFoundWhenNoRouteMatches() {
        // given
        final MockServerWebExchange exchange = exchangeFor("/nope");
        final Throwable exception = new ResponseStatusException(HttpStatus.NOT_FOUND);

        // when
        handler.handle(exchange, exception).block();

        // then
        final JsonNode result = bodyOf(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.get("status").asInt()).isEqualTo(404);
        assertThat(result.get("title").asText()).isEqualTo("Not Found");
        assertThat(result.get("code").asText()).isEqualTo("ROUTE_NOT_FOUND");
    }

    @Test
    @DisplayName("다운스트림 연결이 거부되면 503과 SERVICE_UNAVAILABLE을 반환한다")
    void returnsServiceUnavailableWhenConnectionRefused() {
        // given
        final MockServerWebExchange exchange = exchangeFor("/api/v1/orders/ord-1");
        final Throwable exception = new RuntimeException("proxy failed",
                new ConnectException("Connection refused: no further information"));

        // when
        handler.handle(exchange, exception).block();

        // then
        final JsonNode result = bodyOf(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result.get("status").asInt()).isEqualTo(503);
        assertThat(result.get("code").asText()).isEqualTo("SERVICE_UNAVAILABLE");
    }

    @Test
    @DisplayName("java.util.concurrent 타임아웃이면 504와 GATEWAY_TIMEOUT을 반환한다")
    void returnsGatewayTimeoutOnJdkTimeout() {
        // given
        final MockServerWebExchange exchange = exchangeFor("/api/v1/products");
        final Throwable exception = new TimeoutException("did not observe any item within 10000ms");

        // when
        handler.handle(exchange, exception).block();

        // then
        final JsonNode result = bodyOf(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(result.get("code").asText()).isEqualTo("GATEWAY_TIMEOUT");
    }

    @Test
    @DisplayName("Netty 응답 타임아웃이면 504와 GATEWAY_TIMEOUT을 반환한다")
    void returnsGatewayTimeoutOnNettyReadTimeout() {
        // given
        final MockServerWebExchange exchange = exchangeFor("/api/v1/products");
        final Throwable exception = new RuntimeException("proxy failed",
                io.netty.handler.timeout.ReadTimeoutException.INSTANCE);

        // when
        handler.handle(exchange, exception).block();

        // then
        final JsonNode result = bodyOf(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(result.get("code").asText()).isEqualTo("GATEWAY_TIMEOUT");
    }

    @Test
    @DisplayName("분류되지 않은 예외는 500과 GATEWAY_INTERNAL_ERROR로 처리한다")
    void returnsInternalErrorForUnclassifiedException() {
        // given
        final MockServerWebExchange exchange = exchangeFor("/api/v1/payments");
        final Throwable exception = new IllegalStateException("boom");

        // when
        handler.handle(exchange, exception).block();

        // then
        final JsonNode result = bodyOf(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.get("code").asText()).isEqualTo("GATEWAY_INTERNAL_ERROR");
    }

    @Test
    @DisplayName("응답은 problem+json 이고 instance에 요청 경로가 담긴다")
    void writesProblemDetailWithRequestPath() {
        // given
        final MockServerWebExchange exchange = exchangeFor("/api/v1/orders/ord-7781");
        final Throwable exception = new ResponseStatusException(HttpStatus.NOT_FOUND);

        // when
        handler.handle(exchange, exception).block();

        // then
        final JsonNode result = bodyOf(exchange);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result.get("type").asText()).isEqualTo("about:blank");
        assertThat(result.get("instance").asText()).isEqualTo("/api/v1/orders/ord-7781");
        assertThat(result.get("detail").asText()).isNotBlank();
    }

    private MockServerWebExchange exchangeFor(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    private JsonNode bodyOf(MockServerWebExchange exchange) {
        final String body = exchange.getResponse().getBodyAsString().block();
        try {
            return objectMapper.readTree(body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
