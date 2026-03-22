package dev.labs.commerce.order.core.order.domain;

import java.util.Optional;

public interface SalesOrderCustomRepository {

    Optional<SalesOrder> findByIdWithLock(String orderId);

}
