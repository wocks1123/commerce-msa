package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public RegisterProductResult execute(RegisterProductCommand command) {
        // Create product using domain factory method
        Product product = Product.create(
                command.productName(),
                command.price(),
                command.currency(),
                command.description()
        );

        // Persist the product
        Product savedProduct = productRepository.save(product);

        // Convert to result
        return new RegisterProductResult(
                savedProduct.getProductId(),
                savedProduct.getProductName(),
                savedProduct.getPrice(),
                savedProduct.getCurrency(),
                savedProduct.getProductStatus(),
                savedProduct.getDescription(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }
}
