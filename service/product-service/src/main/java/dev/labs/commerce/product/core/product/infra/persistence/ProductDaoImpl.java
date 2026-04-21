package dev.labs.commerce.product.core.product.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductDao;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductDaoImpl implements ProductDao {

    private final JPAQueryFactory factory;

    @Override
    public List<Product> findByStatusInSalePeriod(ProductStatus status, Instant at, int limit) {
        QProduct product = QProduct.product;
        return factory.selectFrom(product)
                .where(
                        product.productStatus.eq(status),
                        product.saleStartAt.loe(at),
                        product.saleEndAt.isNull().or(product.saleEndAt.gt(at))
                )
                .orderBy(product.saleStartAt.asc(), product.productId.asc())
                .limit(limit)
                .fetch();
    }

}
