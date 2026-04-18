package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductRegisteredEvent;
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
    private final ProductEventPublisher productEventPublisher;

    @Transactional
    public RegisterProductResult execute(RegisterProductCommand command) {
        Product product = Product.create(
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

        Product savedProduct = productRepository.save(product);

        productEventPublisher.publishProductRegistered(
                new ProductRegisteredEvent(savedProduct.getProductId())
        );

        return new RegisterProductResult(
                savedProduct.getProductId(),
                savedProduct.getProductName(),
                savedProduct.getListPrice(),
                savedProduct.getSellingPrice(),
                savedProduct.getCurrency(),
                savedProduct.getProductStatus(),
                savedProduct.getCategory(),
                savedProduct.getSaleStartAt(),
                savedProduct.getSaleEndAt(),
                savedProduct.getThumbnailUrl(),
                savedProduct.getDescription(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }
}
