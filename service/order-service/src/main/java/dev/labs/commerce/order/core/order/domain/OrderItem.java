package dev.labs.commerce.order.core.order.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private Long orderItemId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "line_amount", nullable = false)
    private long lineAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    public static OrderItem create(long productId, String productName, long unitPrice, int quantity, String currency) {
        Assert.hasText(productName, "productName must not be blank");
        Assert.isTrue(unitPrice > 0, "unitPrice must be positive");
        Assert.isTrue(quantity > 0, "quantity must be positive");
        Assert.hasText(currency, "currency must not be blank");

        OrderItem item = new OrderItem();
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice;
        item.quantity = quantity;
        item.lineAmount = unitPrice * quantity;
        item.currency = currency;
        return item;
    }
}
