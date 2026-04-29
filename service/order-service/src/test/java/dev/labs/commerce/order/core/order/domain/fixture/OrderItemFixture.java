package dev.labs.commerce.order.core.order.domain.fixture;

import dev.labs.commerce.order.core.order.domain.OrderItem;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderItemFixture {

    private long productId;
    private String productName;
    private long unitPrice;
    private int quantity;
    private long lineAmount;
    private String currency;

    private OrderItemFixture() {
    }

    public static OrderItemFixture builder() {
        return new OrderItemFixture();
    }

    public OrderItemFixture productId(long productId) {
        this.productId = productId;
        return this;
    }

    public OrderItemFixture productName(String productName) {
        this.productName = productName;
        return this;
    }

    public OrderItemFixture unitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public OrderItemFixture quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderItemFixture lineAmount(long lineAmount) {
        this.lineAmount = lineAmount;
        return this;
    }

    public OrderItemFixture currency(String currency) {
        this.currency = currency;
        return this;
    }

    public OrderItemFixture withSample() {
        this.productId = 1L;
        this.productName = "상품A";
        this.unitPrice = 5000L;
        this.quantity = 2;
        this.lineAmount = unitPrice * quantity;
        this.currency = "KRW";
        return this;
    }

    public OrderItem build() {
        OrderItem item = BeanUtils.instantiateClass(OrderItem.class);
        ReflectionTestUtils.setField(item, "productId", productId);
        ReflectionTestUtils.setField(item, "productName", productName);
        ReflectionTestUtils.setField(item, "unitPrice", unitPrice);
        ReflectionTestUtils.setField(item, "quantity", quantity);
        ReflectionTestUtils.setField(item, "lineAmount", lineAmount);
        ReflectionTestUtils.setField(item, "currency", currency);
        return item;
    }
}
