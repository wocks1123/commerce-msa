package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.productId()));

        // Modify product using domain method
        product.modify(
                command.productName(),
                command.price(),
                command.currency(),
                command.description()
        );

        // Persist the changes
        Product updatedProduct = productRepository.save(product);

        // Convert to result
        return new ModifyProductResult(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getPrice(),
                updatedProduct.getCurrency(),
                updatedProduct.getProductStatus(),
                updatedProduct.getDescription(),
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
    }
}
