package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductActivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDeactivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDiscontinuedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeProductStatusUseCase {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    @Transactional
    public ChangeProductStatusResult execute(ChangeProductStatusCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + command.productId()));

        ProductStatus previousStatus = product.getProductStatus();

        product.changeStatus(command.status());

        Product updatedProduct = productRepository.save(product);

        publishStatusTransitionEvent(previousStatus, updatedProduct);

        return new ChangeProductStatusResult(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getListPrice(),
                updatedProduct.getSellingPrice(),
                updatedProduct.getCurrency(),
                updatedProduct.getProductStatus(),
                updatedProduct.getCategory(),
                updatedProduct.getSaleStartAt(),
                updatedProduct.getSaleEndAt(),
                updatedProduct.getThumbnailUrl(),
                updatedProduct.getDescription(),
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
    }

    private void publishStatusTransitionEvent(ProductStatus previousStatus, Product product) {
        ProductStatus newStatus = product.getProductStatus();
        if (previousStatus == newStatus) {
            return;
        }
        switch (newStatus) {
            case ACTIVE -> productEventPublisher.publishProductActivated(new ProductActivatedEvent(
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
            ));
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
            case DRAFT -> { /* 전이 규칙상 도달 불가 */ }
        }
    }
}
