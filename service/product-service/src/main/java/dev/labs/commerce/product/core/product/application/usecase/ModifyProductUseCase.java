package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductModifiedEvent;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ModifyProductUseCase {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    @Transactional
    public ModifyProductResult execute(ModifyProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + command.productId()));

        Set<String> changedFields = product.modify(
                command.productName(),
                command.listPrice(),
                command.sellingPrice(),
                command.currency(),
                command.category(),
                command.saleStartAt(),
                command.saleEndAt(),
                command.thumbnailUrl(),
                command.description()
        );

        Product updatedProduct = productRepository.save(product);

        if (!changedFields.isEmpty() && isPubliclyVisible(updatedProduct.getProductStatus())) {
            productEventPublisher.publishProductModified(new ProductModifiedEvent(
                    updatedProduct.getProductId(),
                    updatedProduct.getProductName(),
                    updatedProduct.getListPrice(),
                    updatedProduct.getSellingPrice(),
                    updatedProduct.getCurrency(),
                    updatedProduct.getCategory(),
                    updatedProduct.getSaleStartAt(),
                    updatedProduct.getSaleEndAt(),
                    updatedProduct.getThumbnailUrl(),
                    updatedProduct.getDescription()
            ));
        }

        return new ModifyProductResult(
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

    private static boolean isPubliclyVisible(ProductStatus status) {
        return status == ProductStatus.ACTIVE || status == ProductStatus.INACTIVE;
    }
}
