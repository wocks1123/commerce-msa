package dev.labs.commerce.product.core.product.application.support;

import dev.labs.commerce.product.core.product.application.event.ProductActivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDeactivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDiscontinuedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class ProductStatusEventDispatcher {

    private final ProductEventPublisher productEventPublisher;

    public void dispatch(ProductStatus previousStatus, Product product) {
        Assert.notNull(previousStatus, "previousStatus must not be null");
        Assert.notNull(product, "product must not be null");

        final ProductStatus newStatus = product.getProductStatus();
        if (previousStatus == newStatus) {
            return;
        }
        switch (newStatus) {
            case ACTIVE -> productEventPublisher.publishProductActivated(toActivatedEvent(product));
            case INACTIVE -> productEventPublisher.publishProductDeactivated(
                    new ProductDeactivatedEvent(product.getProductId())
            );
            case DISCONTINUED -> {
                // 한 번도 공개된 적 없는(DRAFT) 상품의 폐기는 외부에 알리지 않는다
                if (previousStatus == ProductStatus.ACTIVE || previousStatus == ProductStatus.INACTIVE) {
                    productEventPublisher.publishProductDiscontinued(
                            new ProductDiscontinuedEvent(product.getProductId())
                    );
                }
            }
            case DRAFT -> throw new InvalidProductStatusException(ProductErrorCode.INVALID_PRODUCT_STATUS, "transition to DRAFT is not allowed");
            case null -> throw new InvalidProductStatusException(ProductErrorCode.INVALID_PRODUCT_STATUS, "product status must not be null");
        }
    }

    private ProductActivatedEvent toActivatedEvent(Product product) {
        return new ProductActivatedEvent(
                product.getProductId(),
                product.getProductName(),
                product.getListPrice(),
                product.getSellingPrice(),
                product.getCurrency(),
                product.getCategory(),
                product.getSaleStartAt(),
                product.getSaleEndAt(),
                product.getThumbnailUrl(),
                product.getDescription()
        );
    }
}
