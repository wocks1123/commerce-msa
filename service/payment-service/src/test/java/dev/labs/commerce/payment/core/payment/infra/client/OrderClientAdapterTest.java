package dev.labs.commerce.payment.core.payment.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.payment.core.payment.domain.OrderPort;
import dev.labs.commerce.payment.core.payment.domain.OrderStatus;
import dev.labs.commerce.payment.core.payment.domain.exception.OrderClientException;
import dev.labs.commerce.payment.core.payment.domain.exception.OrderNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentErrorCode;
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * order-service 응답이 payment-service의 예외/도메인 모델로 어떻게 번역되는지 고정한다.
 */
class OrderClientAdapterTest {

    private static final String BASE_URL = "http://order-service:20103";
    private static final String ORDER_ID = "0f8f2a1e-1c6c-4f0a-9a5a-1b2c3d4e5f60";

    private MockRestServiceServer server;
    private OrderClientAdapter orderClientAdapter;

    @BeforeEach
    void setUp() {
        final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.orderClientAdapter = new OrderClientAdapter(builder.build());
    }

    private ResponseActions expectOrderCall() {
        return server.expect(requestTo(BASE_URL + "/api/v1/orders/" + ORDER_ID))
                .andExpect(method(HttpMethod.GET));
    }

    private static String orderResponseBody(String status) {
        return """
                {
                  "orderId": "%s",
                  "customerId": 7,
                  "status": "%s",
                  "totalPrice": 3000,
                  "currency": "KRW",
                  "items": [
                    {"productId": 10, "quantity": 1},
                    {"productId": 20, "quantity": 2}
                  ]
                }
                """.formatted(ORDER_ID, status);
    }

    @Test
    @DisplayName("정상 응답이면 OrderSnapshot으로 매핑한다")
    void getOrder_whenSuccess_mapsToOrderSnapshot() {
        // given
        expectOrderCall().andRespond(withSuccess(orderResponseBody("CREATED"), MediaType.APPLICATION_JSON));

        // when
        final OrderPort.OrderSnapshot result = orderClientAdapter.getOrder(ORDER_ID);

        // then
        assertThat(result).isEqualTo(new OrderPort.OrderSnapshot(
                ORDER_ID,
                7L,
                OrderStatus.CREATED,
                3000L,
                "KRW",
                List.of(
                        new OrderPort.OrderSnapshot.Item(10L, 1),
                        new OrderPort.OrderSnapshot.Item(20L, 2)
                )
        ));
        server.verify();
    }

    @Test
    @DisplayName("모르는 주문 상태는 UNKNOWN으로 흡수한다 (fail-closed)")
    void getOrder_whenUnknownStatus_mapsToUnknown() {
        // given
        expectOrderCall().andRespond(withSuccess(orderResponseBody("SOMETHING_NEW"), MediaType.APPLICATION_JSON));

        // when
        final OrderPort.OrderSnapshot result = orderClientAdapter.getOrder(ORDER_ID);

        // then
        assertThat(result.status()).isEqualTo(OrderStatus.UNKNOWN);
        server.verify();
    }

    @Test
    @DisplayName("품목이 비어 있는 응답은 계약 위반으로 DependencyUnavailableException이 발생한다")
    void getOrder_whenItemsEmpty_throwsDependencyUnavailableException() {
        // given
        expectOrderCall().andRespond(withSuccess("""
                {
                  "orderId": "%s",
                  "customerId": 7,
                  "status": "CREATED",
                  "totalPrice": 3000,
                  "currency": "KRW",
                  "items": []
                }
                """.formatted(ORDER_ID), MediaType.APPLICATION_JSON));

        // when
        final Throwable thrown = catchThrowable(() -> orderClientAdapter.getOrder(ORDER_ID));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE);
        server.verify();
    }

    @Test
    @DisplayName("404는 OrderNotFoundException이 발생한다")
    void getOrder_when404_throwsOrderNotFoundException() {
        // given
        expectOrderCall().andRespond(withStatus(HttpStatus.NOT_FOUND));

        // when
        final Throwable thrown = catchThrowable(() -> orderClientAdapter.getOrder(ORDER_ID));

        // then
        assertThat(thrown).isInstanceOf(OrderNotFoundException.class);
        assertThat(((OrderNotFoundException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.ORDER_NOT_FOUND);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 409})
    @DisplayName("404 외 예상 밖 4xx는 OrderClientException이 발생한다 (호출 측 결함 → 500)")
    void getOrder_whenUnexpected4xx_throwsOrderClientException(int statusCode) {
        // given
        expectOrderCall().andRespond(withStatus(HttpStatus.valueOf(statusCode)));

        // when & then
        assertThatThrownBy(() -> orderClientAdapter.getOrder(ORDER_ID))
                .isInstanceOf(OrderClientException.class)
                .hasMessageContaining(String.valueOf(statusCode));
        server.verify();
    }

    @Test
    @DisplayName("5xx는 DependencyUnavailableException이 발생한다")
    void getOrder_when5xx_throwsDependencyUnavailableException() {
        // given
        expectOrderCall().andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // when
        final Throwable thrown = catchThrowable(() -> orderClientAdapter.getOrder(ORDER_ID));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE);
        server.verify();
    }

    @Test
    @DisplayName("읽기 타임아웃은 DependencyTimeoutException이 발생한다")
    void getOrder_whenReadTimeout_throwsDependencyTimeoutException() {
        // given
        expectOrderCall().andRespond(request -> {
            throw new SocketTimeoutException("Read timed out");
        });

        // when
        final Throwable thrown = catchThrowable(() -> orderClientAdapter.getOrder(ORDER_ID));

        // then
        assertThat(thrown).isInstanceOf(DependencyTimeoutException.class);
        assertThat(((DependencyTimeoutException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.ORDER_SERVICE_TIMEOUT);
        server.verify();
    }

    @Test
    @DisplayName("타임아웃이 아닌 연결 실패는 DependencyUnavailableException이 발생한다")
    void getOrder_whenConnectionFails_throwsDependencyUnavailableException() {
        // given
        expectOrderCall().andRespond(request -> {
            throw new ConnectException("Connection refused");
        });

        // when
        final Throwable thrown = catchThrowable(() -> orderClientAdapter.getOrder(ORDER_ID));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE);
        server.verify();
    }
}
