package dev.labs.commerce.product.core.product.domain;

import java.time.Instant;
import java.util.List;

public interface ProductDao {

    /**
     * 지정한 시각(at) 기준으로 판매 기간 내에 있는 특정 상태의 상품을 조회한다.
     * 조건: productStatus = status
     *      AND saleStartAt <= at
     *      AND (saleEndAt IS NULL OR saleEndAt > at)
     * 정렬: saleStartAt ASC, productId ASC
     */
    List<Product> findByStatusInSalePeriod(ProductStatus status, Instant at, int limit);

}
