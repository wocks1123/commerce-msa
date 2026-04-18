package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModifyProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public ModifyProductResult execute(ModifyProductCommand command) {
        // Retrieve product
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + command.productId()));

        // Modify product using domain method
        product.modify(
                command.productName(),
                command.listPrice(),
                command.sellingPrice(),
                command.currency(),
                command.description()
        );

        // Persist the changes
        Product updatedProduct = productRepository.save(product);

        // Convert to result
        return new ModifyProductResult(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getListPrice(),
                updatedProduct.getSellingPrice(),
                updatedProduct.getCurrency(),
                updatedProduct.getProductStatus(),
                updatedProduct.getDescription(),
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
    }
}
