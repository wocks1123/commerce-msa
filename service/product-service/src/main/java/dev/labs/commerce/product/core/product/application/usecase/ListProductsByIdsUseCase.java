package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.query.ProductQueryService;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsByIdsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListProductsByIdsUseCase {

    private final ProductQueryService productQueryService;

    public List<ListProductsByIdsResult> execute(List<Long> productIds) {
        return productQueryService.listProductsByIds(productIds);
    }
}
