package dev.labs.commerce.inventory.core.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    boolean existsByOrderIdAndProductIdAndOperationType(String orderId, Long productId, OperationType operationType);

    @Query("SELECT h.operationType FROM InventoryHistory h WHERE h.orderId = :orderId AND h.productId = :productId")
    Set<OperationType> findOperationTypesByOrderIdAndProductId(@Param("orderId") String orderId, @Param("productId") Long productId);
}
