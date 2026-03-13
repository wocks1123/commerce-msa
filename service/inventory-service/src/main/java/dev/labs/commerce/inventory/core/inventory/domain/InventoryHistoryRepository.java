package dev.labs.commerce.inventory.core.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    boolean existsByOrderIdAndProductIdAndOperationType(String orderId, Long productId, OperationType operationType);
}
