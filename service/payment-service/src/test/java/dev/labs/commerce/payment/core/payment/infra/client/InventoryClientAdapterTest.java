package dev.labs.commerce.payment.core.payment.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.payment.core.payment.domain.InventoryPort;
import dev.labs.commerce.payment.core.payment.domain.exception.InsufficientStockException;
import dev.labs.commerce.payment.core.payment.domain.exception.InventoryClientException;
import dev.labs.commerce.payment.core.payment.domain.exception.InventoryNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * inventory-service 응답이 payment-service의 예외로 어떻게 번역되는지 고정한다.
 */
class InventoryClientAdapterTest {

    private static final String BASE_URL = "http://inventory-service:20102";
    private static final String ORDER_ID = "0f8f2a1e-1c6c-4f0a-9a5a-1b2c3d4e5f60";
    private static final List<InventoryPort.Item> ITEMS = List.of(
            new InventoryPort.Item(10L, 1),
            new InventoryPort.Item(20L, 2)
    );

    private MockRestServiceServer server;
    private InventoryClientAdapter inventoryClientAdapter;

    @BeforeEach
    void setUp() {
        final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.inventoryClientAdapter = new InventoryClientAdapter(builder.build());
    }

    private ResponseActions expectReserveCall() {
        return server.expect(requestTo(BASE_URL + "/api/v1/inventories/reserve"))
                .andExpect(method(HttpMethod.POST));
    }

    @Test
    @DisplayName("정상 응답이면 예약 요청이 주문 품목 그대로 전송되고 예외 없이 종료된다")
    void reserve_whenSuccess_sendsOrderItemsAndCompletes() {
        // given
        expectReserveCall()
                .andExpect(content().json("""
                        {
                          "orderId": "%s",
                          "items": [
                            {"productId": 10, "quantity": 1},
                            {"productId": 20, "quantity": 2}
                          ]
                        }
                        """.formatted(ORDER_ID)))
                .andRespond(withSuccess());

        // when
        final Throwable thrown = catchThrowable(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS));

        // then
        assertThat(thrown).isNull();
        server.verify();
    }

    @Test
    @DisplayName("409는 InsufficientStockException이 발생한다 (재고 부족 → 409)")
    void reserve_when409_throwsInsufficientStockException() {
        // given
        expectReserveCall().andRespond(withStatus(HttpStatus.CONFLICT));

        // when
        final Throwable thrown = catchThrowable(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS));

        // then
        assertThat(thrown).isInstanceOf(InsufficientStockException.class);
        assertThat(((InsufficientStockException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.INSUFFICIENT_STOCK);
        server.verify();
    }

    @Test
    @DisplayName("404는 InventoryNotFoundException이 발생한다 (재고 데이터 부재 → 500)")
    void reserve_when404_throwsInventoryNotFoundException() {
        // given
        expectReserveCall().andRespond(withStatus(HttpStatus.NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining(ORDER_ID);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 422})
    @DisplayName("409/404 외 예상 밖 4xx는 InventoryClientException이 발생한다 (호출 측 결함 → 500)")
    void reserve_whenUnexpected4xx_throwsInventoryClientException(int statusCode) {
        // given
        expectReserveCall().andRespond(withStatus(HttpStatus.valueOf(statusCode)));

        // when & then
        assertThatThrownBy(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS))
                .isInstanceOf(InventoryClientException.class)
                .hasMessageContaining(String.valueOf(statusCode));
        server.verify();
    }

    @Test
    @DisplayName("5xx는 DependencyUnavailableException이 발생한다")
    void reserve_when5xx_throwsDependencyUnavailableException() {
        // given
        expectReserveCall().andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // when
        final Throwable thrown = catchThrowable(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.INVENTORY_SERVICE_UNAVAILABLE);
        server.verify();
    }

    @Test
    @DisplayName("읽기 타임아웃은 DependencyTimeoutException이 발생한다")
    void reserve_whenReadTimeout_throwsDependencyTimeoutException() {
        // given
        expectReserveCall().andRespond(request -> {
            throw new SocketTimeoutException("Read timed out");
        });

        // when
        final Throwable thrown = catchThrowable(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS));

        // then
        assertThat(thrown).isInstanceOf(DependencyTimeoutException.class);
        assertThat(((DependencyTimeoutException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.INVENTORY_SERVICE_TIMEOUT);
        server.verify();
    }

    @Test
    @DisplayName("타임아웃이 아닌 연결 실패는 DependencyUnavailableException이 발생한다")
    void reserve_whenConnectionFails_throwsDependencyUnavailableException() {
        // given
        expectReserveCall().andRespond(request -> {
            throw new ConnectException("Connection refused");
        });

        // when
        final Throwable thrown = catchThrowable(() -> inventoryClientAdapter.reserve(ORDER_ID, ITEMS));

        // then
        assertThat(thrown).isInstanceOf(DependencyUnavailableException.class);
        assertThat(((DependencyUnavailableException) thrown).getErrorCode())
                .isEqualTo(PaymentErrorCode.INVENTORY_SERVICE_UNAVAILABLE);
        server.verify();
    }
}
