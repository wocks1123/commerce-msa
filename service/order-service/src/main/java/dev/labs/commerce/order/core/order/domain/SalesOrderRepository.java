package dev.labs.commerce.order.core.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, String>, SalesOrderCustomRepository {

    @Query("SELECT o.orderId FROM SalesOrder o WHERE o.status = :status AND o.orderCreatedAt < :threshold")
    List<String> findOrderIdsByStatusAndOrderCreatedAtBefore(@Param("status") OrderStatus status, @Param("threshold") Instant threshold);

}
