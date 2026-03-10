package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.application.event.ProductRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterProductUseCase {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public RegisterProductResult execute(RegisterProductCommand command) {
        Product product = Product.create(
                command.productName(),
                command.price(),
                command.currency(),
                command.description()
        );

        Product savedProduct = productRepository.save(product);

        applicationEventPublisher.publishEvent(
                new ProductRegisteredEvent(savedProduct.getProductId())
        );

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
