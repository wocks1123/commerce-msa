package dev.labs.commerce.inventory.core.inventory.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import dev.labs.commerce.inventory.core.inventory.domain.error.InsufficientStockException;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Version
    @Column(name = "version", nullable = false)
    private long version;


    public static Inventory create(Long productId) {
        Assert.notNull(productId, "productId must not be null");

        Inventory inventory = new Inventory();
        inventory.productId = productId;
        return inventory;
    }

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }

    public void increase(int qty) {
        Assert.isTrue(qty > 0, "qty must be positive");

        this.totalQuantity += qty;
    }

    public void decrease(int qty) {
        Assert.isTrue(qty > 0, "qty must be positive");

        if (this.totalQuantity - qty < this.reservedQuantity) {
            throw new InsufficientStockException(InventoryErrorCode.INSUFFICIENT_STOCK, "Cannot decrease below reserved quantity.");
        }
        this.totalQuantity -= qty;
    }

    public void reserve(int qty) {
        Assert.isTrue(qty > 0, "qty must be positive");

        if (getAvailableQuantity() < qty) {
            throw new InsufficientStockException(InventoryErrorCode.INSUFFICIENT_STOCK);
        }
        this.reservedQuantity += qty;
    }

    public void release(int qty) {
        Assert.isTrue(qty > 0, "qty must be positive");

        if (this.reservedQuantity < qty) {
            throw new InsufficientStockException(InventoryErrorCode.RESERVED_QUANTITY_UNDERFLOW);
        }
        this.reservedQuantity -= qty;
    }

}
