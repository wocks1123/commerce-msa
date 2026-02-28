package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeProductStatusUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public ChangeProductStatusResult execute(ChangeProductStatusCommand command) {
        // Retrieve product
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.productId()));

        // Change status using domain method
        product.changeStatus(command.status());

        // Persist the changes
        Product updatedProduct = productRepository.save(product);

        // Convert to result
        return new ChangeProductStatusResult(
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
