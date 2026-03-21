package dev.labs.commerce.order.core.order.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.labs.commerce.order.core.order.domain.QSalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderCustomRepository;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SalesOrderRepositoryImpl implements SalesOrderCustomRepository {

    private final JPAQueryFactory factory;

    @Override
    public Optional<SalesOrder> findByIdWithLock(String orderId) {
        return Optional.ofNullable(
                factory.selectFrom(QSalesOrder.salesOrder)
                        .where(QSalesOrder.salesOrder.orderId.eq(orderId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

}
