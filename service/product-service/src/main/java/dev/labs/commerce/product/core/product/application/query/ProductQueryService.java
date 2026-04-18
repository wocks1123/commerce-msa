package dev.labs.commerce.product.core.product.application.query;

import dev.labs.commerce.product.core.product.application.usecase.dto.GetProductResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsByIdsResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
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
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId));

        return new GetProductResult(
                product.getProductId(),
                product.getProductName(),
                product.getListPrice(),
                product.getSellingPrice(),
                product.getCurrency(),
                product.getProductStatus(),
                product.getCategory(),
                product.getSaleStartAt(),
                product.getSaleEndAt(),
                product.getThumbnailUrl(),
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
                .map(this::toListProductsResult)
                .collect(Collectors.toList());
    }

    public List<ListProductsByIdsResult> listProductsByIds(List<Long> productIds) {
        return productRepository.findAllByProductIdIn(productIds)
                .stream()
                .map(this::toListProductsByIdsResult)
                .toList();
    }

    private ListProductsResult toListProductsResult(Product p) {
        return new ListProductsResult(
                p.getProductId(),
                p.getProductName(),
                p.getListPrice(),
                p.getSellingPrice(),
                p.getCurrency(),
                p.getProductStatus(),
                p.getCategory(),
                p.getThumbnailUrl()
        );
    }

    private ListProductsByIdsResult toListProductsByIdsResult(Product p) {
        return new ListProductsByIdsResult(
                p.getProductId(),
                p.getProductName(),
                p.getListPrice(),
                p.getSellingPrice(),
                p.getCurrency(),
                p.getProductStatus(),
                p.getCategory(),
                p.getThumbnailUrl()
        );
    }
}
