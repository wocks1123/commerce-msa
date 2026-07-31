package dev.labs.commerce.payment.core.payment.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.payment.core.payment.domain.OrderPort;
import dev.labs.commerce.payment.core.payment.domain.OrderStatus;
import dev.labs.commerce.payment.core.payment.domain.exception.OrderClientException;
import dev.labs.commerce.payment.core.payment.domain.exception.OrderNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentErrorCode;
import dev.labs.commerce.payment.core.payment.infra.client.dto.GetSalesOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.List;

@Component
@Slf4j
public class OrderClientAdapter implements OrderPort {

    private final RestClient restClient;

    public OrderClientAdapter(@Qualifier("orderRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public OrderSnapshot getOrder(String orderId) {
        log.info("Calling order-service getOrder: orderId={}", orderId);

        GetSalesOrderResponse response;
        try {
            response = restClient.get()
                    .uri("/api/v1/orders/{orderId}", orderId)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new OrderNotFoundException(orderId);
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new OrderClientException(
                                "Unexpected 4xx from order-service: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new DependencyUnavailableException(
                                PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE,
                                "Order service returned " + res.getStatusCode());
                    })
                    .body(GetSalesOrderResponse.class);
        } catch (ResourceAccessException e) {
            log.warn("Order service call failed: {}", e.getMessage());
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new DependencyTimeoutException(PaymentErrorCode.ORDER_SERVICE_TIMEOUT);
            }
            throw new DependencyUnavailableException(PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE);
        }

        return toSnapshot(orderId, response);
    }

    private static OrderSnapshot toSnapshot(String orderId, GetSalesOrderResponse response) {
        // 주문은 항상 1개 이상의 품목을 가진다(order-service의 생성 검증). 비어 있다면 응답 계약 위반이다.
        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new DependencyUnavailableException(
                    PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE,
                    "Malformed order response: orderId=" + orderId);
        }

        List<OrderSnapshot.Item> items = response.items().stream()
                .map(item -> new OrderSnapshot.Item(item.productId(), item.quantity()))
                .toList();

        return new OrderSnapshot(
                response.orderId(),
                response.customerId(),
                toOrderStatus(response.status()),
                response.totalPrice(),
                response.currency(),
                items
        );
    }

    /**
     * order-service의 상태 문자열을 payment-service가 소유한 {@link OrderStatus}로 번역한다.
     * 모르는 값은 {@link OrderStatus#UNKNOWN}으로 흡수한다 — 결제 초기화는 CREATED만 허용하므로
     * 미지의 상태는 자동으로 거절된다(fail-closed).
     */
    private static OrderStatus toOrderStatus(String raw) {
        try {
            return OrderStatus.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Unknown order status from order-service: {}", raw);
            return OrderStatus.UNKNOWN;
        }
    }
}
