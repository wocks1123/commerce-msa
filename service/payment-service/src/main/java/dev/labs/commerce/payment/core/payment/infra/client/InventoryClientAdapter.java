package dev.labs.commerce.payment.core.payment.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.payment.core.payment.domain.InventoryPort;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentErrorCode;
import dev.labs.commerce.payment.core.payment.infra.client.dto.ReserveInventoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new DependencyUnavailableException(
                                PaymentErrorCode.INVENTORY_SERVICE_UNAVAILABLE,
                                "Inventory service returned " + res.getStatusCode());
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
