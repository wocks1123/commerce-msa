package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.query.ProductQueryService;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsResult;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListProductsUseCase {

    private final ProductQueryService productQueryService;

    @Transactional(readOnly = true)
    public List<ListProductsResult> execute(ProductStatus status) {
        return productQueryService.listProducts(status);
    }
}
