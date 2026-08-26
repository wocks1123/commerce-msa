package dev.labs.commerce.order.core.order.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.order.core.order.domain.ProductInfo;
import dev.labs.commerce.order.core.order.domain.error.OrderErrorCode;
import dev.labs.commerce.order.core.order.domain.error.ProductClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * product-service 응답 상태코드가 order-service의 예외로 어떻게 번역되는지 고정한다.
 *
 * <p>매핑 원칙: 의존 서비스의 5xx/타임아웃 = Dependency 예외(503/504),
 * 예상 밖 4xx = 호출 측 결함(500).
 */
class ProductClientAdapterTest {

    private static final String BASE_URL = "http://product-service:20101";
    private static final List<Long> PRODUCT_IDS = List.of(10L, 20L);

    private MockRestServiceServer server;
    private ProductClientAdapter productClientAdapter;

    @BeforeEach
    void setUp() {
        final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.productClientAdapter = new ProductClientAdapter(builder.build());
    }

    private ResponseActions expectProductsCall() {
        return server.expect(requestTo(BASE_URL + "/api/v1/products?ids=10&ids=20"))
                .andExpect(method(HttpMethod.GET));
    }

    @Test
    @DisplayName("정상 응답이면 ProductInfo 목록으로 매핑한다")
    void findProducts_whenSuccess_mapsToProductInfo() {
        // given
        expectProductsCall().andRespond(withSuccess("""
                [
                  {"productId":10,"productName":"상품A","sellingPrice":1000,"currency":"KRW","productStatus":"ACTIVE"},
                  {"productId":20,"productName":"상품B","sellingPrice":2000,"currency":"KRW","productStatus":"INACTIVE"}
                ]
                """, MediaType.APPLICATION_JSON));

        // when
        final List<ProductInfo> result = productClientAdapter.findProducts(PRODUCT_IDS);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE"));
        assertThat(result.get(1)).isEqualTo(new ProductInfo(20L, "상품B", 2000L, "KRW", "INACTIVE"));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 409})
    @DisplayName("예상 밖 4xx는 ProductClientException이 발생한다 (호출 측 결함 → 500)")
    void findProducts_when4xx_throwsProductClientException(int statusCode) {
        // given
        expectProductsCall().andRespond(withStatus(HttpStatus.valueOf(statusCode)));

        // when & then
        assertThatThrownBy(() -> productClientAdapter.findProducts(PRODUCT_IDS))
                .isInstanceOf(ProductClientException.class)
                .hasMessageContaining(String.valueOf(statusCode));
        server.verify();
    }

    @Test
    @DisplayName("5xx는 DependencyUnavailableException이 발생한다")
    void findProducts_when5xx_throwsDependencyUnavailableException() {
        // given
        expectProductsCall().andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // when
        final Throwable thrown = catchThrowable(() -> productClientAdapter.findProducts(PRODUCT_IDS));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(OrderErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        server.verify();
    }

    @Test
    @DisplayName("읽기 타임아웃은 DependencyTimeoutException이 발생한다")
    void findProducts_whenReadTimeout_throwsDependencyTimeoutException() {
        // given
        expectProductsCall().andRespond(request -> {
            throw new SocketTimeoutException("Read timed out");
        });

        // when
        final Throwable thrown = catchThrowable(() -> productClientAdapter.findProducts(PRODUCT_IDS));

        // then
        assertThat(thrown).isInstanceOf(DependencyTimeoutException.class);
        assertThat(((DependencyTimeoutException) thrown).getErrorCode())
                .isEqualTo(OrderErrorCode.PRODUCT_SERVICE_TIMEOUT);
        server.verify();
    }

    @Test
    @DisplayName("타임아웃이 아닌 연결 실패는 DependencyUnavailableException이 발생한다")
    void findProducts_whenConnectionFails_throwsDependencyUnavailableException() {
        // given
        expectProductsCall().andRespond(request -> {
            throw new ConnectException("Connection refused");
        });

        // when
        final Throwable thrown = catchThrowable(() -> productClientAdapter.findProducts(PRODUCT_IDS));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(OrderErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        server.verify();
    }
}
