package dev.labs.commerce.order.core.order.infra.client;

import dev.labs.commerce.common.error.DependencyTimeoutException;
import dev.labs.commerce.common.error.DependencyUnavailableException;
import dev.labs.commerce.order.core.order.domain.ProductInfo;
import dev.labs.commerce.order.core.order.domain.ProductPort;
import dev.labs.commerce.order.core.order.domain.error.OrderErrorCode;
import dev.labs.commerce.order.core.order.infra.client.dto.ProductSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClientAdapter implements ProductPort {

    @Qualifier("productRestClient")
    private final RestClient restClient;

    @Override
    public List<ProductInfo> findProducts(List<Long> productIds) {
        log.info("Calling product-service: productIds={}", productIds);
        try {
            List<ProductSummaryDto> result = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/products")
                            .queryParam("ids", productIds)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new DependencyUnavailableException(
                                OrderErrorCode.PRODUCT_SERVICE_UNAVAILABLE,
                                "Product service returned " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new DependencyUnavailableException(
                                OrderErrorCode.PRODUCT_SERVICE_UNAVAILABLE,
                                "Product service returned " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });

            return result.stream()
                    .map(dto -> new ProductInfo(
                            dto.productId(),
                            dto.productName(),
                            dto.price(),
                            dto.currency(),
                            dto.productStatus()))
                    .toList();

        } catch (ResourceAccessException e) {
            throw new DependencyTimeoutException(OrderErrorCode.PRODUCT_SERVICE_TIMEOUT, e.getMessage());
        }
    }
}
