package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductActivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.usecase.dto.ActivateScheduledProductCommand;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivateScheduledProductUseCase {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    public void execute(ActivateScheduledProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + command.productId()));

        ProductStatus previousStatus = product.getProductStatus();
        if (previousStatus == ProductStatus.ACTIVE) {
            return;
        }

        product.changeStatus(ProductStatus.ACTIVE);
        productRepository.save(product);

        productEventPublisher.publishProductActivated(new ProductActivatedEvent(
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

        log.info("Product activated by scheduler: productId={}", product.getProductId());
    }
}
