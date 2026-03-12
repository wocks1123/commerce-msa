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


    public static InventoryHistory reserve(String orderId, Inventory inventory, int quantity, Actor actor) {
        return of(orderId, inventory, OperationType.RESERVE, quantity, actor);
    }

    public static InventoryHistory confirm(String orderId, Inventory inventory, int quantity, Actor actor) {
        return of(orderId, inventory, OperationType.CONFIRM, quantity, actor);
    }

    public static InventoryHistory release(String orderId, Inventory inventory, int quantity, Actor actor) {
        return of(orderId, inventory, OperationType.RELEASE, quantity, actor);
    }

    public static InventoryHistory restock(Inventory inventory, int quantity, Actor actor) {
        return of(null, inventory, OperationType.RESTOCK, quantity, actor);
    }

    private static InventoryHistory of(
            @Nullable String orderId,
            Inventory inventory,
            OperationType operationType,
            int quantity,
            Actor actor
    ) {
        Assert.isTrue(quantity > 0, "quantity must be positive");

        InventoryHistory history = new InventoryHistory();
        history.orderId = orderId;
        history.productId = inventory.getProductId();
        history.operationType = operationType;
        history.quantity = quantity;
        history.totalAfter = inventory.getTotalQuantity();
        history.reservedAfter = inventory.getReservedQuantity();
        history.actor = actor;
        return history;
    }

}
