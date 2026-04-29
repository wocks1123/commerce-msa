package dev.labs.commerce.order.core.order.domain.fixture;

import dev.labs.commerce.order.core.order.domain.OrderItem;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SalesOrderFixture {

    private String orderId;
    private long customerId;
    private OrderStatus status;
    private long totalPrice;
    private long totalAmount;
    private String currency;
    private List<OrderItem> items;
    private Instant orderCreatedAt;
    private Instant pendingAt;
    private Instant paidAt;
    private Instant abortedAt;
    private Instant cancelledAt;
    private Instant failedAt;
    private Instant expiredAt;

    private SalesOrderFixture() {
    }

    public static SalesOrderFixture builder() {
        return new SalesOrderFixture();
    }

    public SalesOrderFixture orderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public SalesOrderFixture customerId(long customerId) {
        this.customerId = customerId;
        return this;
    }

    public SalesOrderFixture status(OrderStatus status) {
        this.status = status;
        return this;
    }

    public SalesOrderFixture totalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public SalesOrderFixture totalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public SalesOrderFixture currency(String currency) {
        this.currency = currency;
        return this;
    }

    public SalesOrderFixture items(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        return this;
    }

    public SalesOrderFixture orderCreatedAt(Instant orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
        return this;
    }

    public SalesOrderFixture pendingAt(Instant pendingAt) {
        this.pendingAt = pendingAt;
        return this;
    }

    public SalesOrderFixture paidAt(Instant paidAt) {
        this.paidAt = paidAt;
        return this;
    }

    public SalesOrderFixture abortedAt(Instant abortedAt) {
        this.abortedAt = abortedAt;
        return this;
    }

    public SalesOrderFixture cancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
        return this;
    }

    public SalesOrderFixture failedAt(Instant failedAt) {
        this.failedAt = failedAt;
        return this;
    }

    public SalesOrderFixture expiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
        return this;
    }

    public SalesOrderFixture withSample() {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = 100L;
        this.status = OrderStatus.CREATED;
        this.currency = "KRW";
        this.items = new ArrayList<>(List.of(OrderItemFixture.builder().withSample().build()));
        this.totalPrice = items.stream().mapToLong(OrderItem::getLineAmount).sum();
        this.totalAmount = items.stream().mapToLong(OrderItem::getQuantity).sum();
        this.orderCreatedAt = Instant.now();
        return this;
    }

    public SalesOrder build() {
        SalesOrder order = BeanUtils.instantiateClass(SalesOrder.class);
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "customerId", customerId);
        ReflectionTestUtils.setField(order, "status", status);
        ReflectionTestUtils.setField(order, "totalPrice", totalPrice);
        ReflectionTestUtils.setField(order, "totalAmount", totalAmount);
        ReflectionTestUtils.setField(order, "currency", currency);
        ReflectionTestUtils.setField(order, "items", items);
        ReflectionTestUtils.setField(order, "orderCreatedAt", orderCreatedAt);
        ReflectionTestUtils.setField(order, "pendingAt", pendingAt);
        ReflectionTestUtils.setField(order, "paidAt", paidAt);
        ReflectionTestUtils.setField(order, "abortedAt", abortedAt);
        ReflectionTestUtils.setField(order, "cancelledAt", cancelledAt);
        ReflectionTestUtils.setField(order, "failedAt", failedAt);
        ReflectionTestUtils.setField(order, "expiredAt", expiredAt);
        return order;
    }
}
