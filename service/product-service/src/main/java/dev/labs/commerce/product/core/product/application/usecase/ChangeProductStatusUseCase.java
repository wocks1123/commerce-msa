package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeProductStatusUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public ChangeProductStatusResult execute(ChangeProductStatusCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + command.productId()));

        product.changeStatus(command.status());

        Product updatedProduct = productRepository.save(product);

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
