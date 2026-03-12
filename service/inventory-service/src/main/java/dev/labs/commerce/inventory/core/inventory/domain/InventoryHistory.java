package dev.labs.commerce.inventory.core.inventory.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

@Entity
@Table(
        name = "inventory_history",
        indexes = {
                @Index(name = "idx_inventory_history_product_id_created_at", columnList = "product_id, created_at DESC")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_history_order_product_operation", columnNames = {"order_id", "product_id", "operation_type"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_history_id", nullable = false, updatable = false)
    private Long inventoryHistoryId;

    @Nullable
    @Column(name = "order_id", length = 36, updatable = false)
    private String orderId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, updatable = false, length = 20)
    private OperationType operationType;

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    @Column(name = "total_after", nullable = false, updatable = false)
    private int totalAfter;

    @Column(name = "reserved_after", nullable = false, updatable = false)
    private int reservedAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor", nullable = false, updatable = false, length = 20)
    private Actor actor;


    public static InventoryHistory create(
            @Nullable String orderId,
            Long productId,
            OperationType operationType,
            int quantity,
            int totalAfter,
            int reservedAfter,
            Actor actor
    ) {
        Assert.notNull(productId, "productId must not be null");
        Assert.notNull(operationType, "operationType must not be null");
        Assert.isTrue(quantity > 0, "quantity must be positive");
        

        InventoryHistory history = new InventoryHistory();
        history.orderId = orderId;
        history.productId = productId;
        history.operationType = operationType;
        history.quantity = quantity;
        history.totalAfter = totalAfter;
        history.reservedAfter = reservedAfter;
        history.actor = actor;
        return history;
    }

}
