package dev.labs.commerce.payment.core.payment.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.payment.core.payment.domain.InventoryPort;
import dev.labs.commerce.payment.core.payment.domain.exception.InventoryClientException;
import dev.labs.commerce.payment.core.payment.domain.exception.InventoryNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.exception.InsufficientStockException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentErrorCode;
import dev.labs.commerce.payment.core.payment.infra.client.dto.ReserveInventoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryClientAdapter implements InventoryPort {

    @Qualifier("inventoryRestClient")
    private final RestClient restClient;

    @Override
    public void reserve(String orderId, List<InventoryPort.Item> items) {
        List<ReserveInventoryRequest.Item> requestItems = items.stream()
                .map(item -> new ReserveInventoryRequest.Item(item.productId(), item.quantity()))
                .toList();

        try {
            restClient.post()
                    .uri("/api/v1/inventories/reserve")
                    .body(new ReserveInventoryRequest(orderId, requestItems))
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.CONFLICT, (req, res) -> {
                        throw new InsufficientStockException(PaymentErrorCode.INSUFFICIENT_STOCK,
                                "Insufficient stock for inventory reserve");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new InventoryNotFoundException(
                                "Inventory data not found for orderId=" + orderId);
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new InventoryClientException(
                                "Unexpected 4xx from inventory-service: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new DependencyUnavailableException(
                                PaymentErrorCode.INVENTORY_SERVICE_UNAVAILABLE,
                                "Inventory service returned " + res.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new DependencyTimeoutException(PaymentErrorCode.INVENTORY_SERVICE_TIMEOUT, e.getMessage());
        }
    }
}
