package dev.labs.commerce.order.core.order.infra.client;

import dev.labs.commerce.order.core.order.domain.ProductInfo;
import dev.labs.commerce.order.core.order.domain.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductPort {

    private final ProductFeignClient productFeignClient;

    @Override
    public List<ProductInfo> findProducts(List<Long> productIds) {
        return productFeignClient.listByIds(productIds)
                .stream()
                .map(dto -> new ProductInfo(
                        dto.productId(),
                        dto.productName(),
                        dto.price(),
                        dto.currency(),
                        dto.productStatus()
                ))
                .toList();
    }
}
