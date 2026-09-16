package dev.labs.commerce.gateway;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * 게이트웨이가 다운스트림 응답에 손대지 않는지 검증한다.
 *
 * <p>다운스트림 역할은 Reactor Netty 스텁 서버가 맡는다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayErrorPassThroughIntegrationTest {

    private static final String NOT_FOUND_BODY = """
            {"type":"about:blank","title":"Not Found","status":404,\
            "detail":"Product not found: 99999","instance":"/api/v1/products/99999",\
            "code":"PRODUCT_NOT_FOUND"}""";

    private static final String SERVER_ERROR_BODY = """
            {"type":"about:blank","title":"Internal Server Error","status":500,\
            "detail":"An unexpected server error occurred.","instance":"/api/v1/products/boom",\
            "code":"INTERNAL_ERROR"}""";

    private static final DisposableServer DOWNSTREAM = startDownstream();
    private static final int UNBOUND_PORT = findUnboundPort();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void routeToStubs(DynamicPropertyRegistry registry) {
        registry.add("service.route.product.uri", () -> "http://localhost:" + DOWNSTREAM.port());
        registry.add("service.route.order.uri", () -> "http://localhost:" + UNBOUND_PORT);
    }

    @AfterAll
    static void stopDownstream() {
        DOWNSTREAM.disposeNow();
    }

    @Test
    @DisplayName("다운스트림이 반환한 404는 도메인 에러 코드까지 그대로 전달된다")
    void passesThroughDownstreamNotFound() {
        // given
        // 스텁 다운스트림이 PRODUCT_NOT_FOUND ProblemDetail 을 404로 반환한다

        // when
        final WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/products/99999")
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("PRODUCT_NOT_FOUND")
                .jsonPath("$.detail").isEqualTo("Product not found: 99999");
    }

    @Test
    @DisplayName("다운스트림이 반환한 500은 게이트웨이 에러로 덮이지 않는다")
    void passesThroughDownstreamServerError() {
        // given
        // 스텁 다운스트림이 자체 500 ProblemDetail 을 반환한다

        // when
        final WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/products/boom")
                .exchange();

        // then
        result.expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.instance").isEqualTo("/api/v1/products/boom")
                .jsonPath("$.code").isEqualTo("INTERNAL_ERROR");
    }

    @Test
    @DisplayName("다운스트림에 연결할 수 없을 때만 게이트웨이가 503을 만든다")
    void returnsGatewayErrorOnlyWhenDownstreamUnreachable() {
        // given
        // order 라우트는 아무도 listen 하지 않는 포트를 가리킨다

        // when
        final WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/orders/ord-7781")
                .exchange();

        // then
        result.expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("SERVICE_UNAVAILABLE")
                .jsonPath("$.instance").isEqualTo("/api/v1/orders/ord-7781");
    }

    private static DisposableServer startDownstream() {
        return HttpServer.create()
                .port(0)
                .route(routes -> routes
                        .get("/api/v1/products/99999", (request, response) -> response
                                .status(HttpResponseStatus.NOT_FOUND)
                                .header(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                                .sendString(Mono.just(NOT_FOUND_BODY)))
                        .get("/api/v1/products/boom", (request, response) -> response
                                .status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                                .header(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                                .sendString(Mono.just(SERVER_ERROR_BODY))))
                .bindNow();
    }

    /**
     * 포트를 잠시 점유했다 반납해 확실히 비어 있는 포트 번호를 얻는다.
     */
    private static int findUnboundPort() {
        final DisposableServer probe = HttpServer.create().port(0).bindNow();
        final int port = probe.port();
        probe.disposeNow();
        return port;
    }
}
