package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.support.ProductStatusEventDispatcher;
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
    private final ProductStatusEventDispatcher productStatusEventDispatcher;

    @Transactional
    public ChangeProductStatusResult execute(ChangeProductStatusCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + command.productId()));

        ProductStatus previousStatus = product.getProductStatus();

        product.changeStatus(command.status());

        Product updatedProduct = productRepository.save(product);

        productStatusEventDispatcher.dispatch(previousStatus, updatedProduct);

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
}
