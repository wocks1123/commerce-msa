package dev.labs.commerce.product.core.product.application.query;

import dev.labs.commerce.product.core.product.application.usecase.dto.GetProductResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    public GetProductResult getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        return new GetProductResult(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getCurrency(),
                product.getProductStatus(),
                product.getDescription(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public List<ListProductsResult> listProducts(ProductStatus status) {
        List<Product> products;

        if (status == null) {
            products = productRepository.findAll();
        } else {
            // QueryDSL이나 별도 쿼리 메서드가 필요한 경우 여기서 처리
            // 현재는 메모리에서 필터링
            products = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getProductStatus() == status)
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(p -> new ListProductsResult(
                        p.getProductId(),
                        p.getProductName(),
                        p.getPrice(),
                        p.getCurrency(),
                        p.getProductStatus()
                ))
                .collect(Collectors.toList());
    }
}
