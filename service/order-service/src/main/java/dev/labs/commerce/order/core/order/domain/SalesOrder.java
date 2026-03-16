package dev.labs.commerce.order.core.order.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import dev.labs.commerce.order.core.order.domain.error.InvalidOrderStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesOrder extends BaseEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false, length = 36)
    private String orderId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private long totalPrice;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "pending_at", nullable = false)
    private Instant pendingAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "aborted_at")
    private Instant abortedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "expired_at")
    private Instant expiredAt;


    public static SalesOrder create(long customerId, String currency, List<OrderItem> items, Instant pendingAt) {
        Assert.notEmpty(items, "items must not be empty");
        Assert.hasText(currency, "currency must not be blank");
        Assert.notNull(pendingAt, "pendingAt must not be null");

        SalesOrder order = new SalesOrder();
        order.orderId = UUID.randomUUID().toString();
        order.customerId = customerId;
        order.currency = currency;
        order.status = OrderStatus.PENDING;
        order.items = new ArrayList<>(items);
        order.totalPrice = items.stream().mapToLong(OrderItem::getLineAmount).sum();
        order.totalAmount = items.stream().mapToLong(OrderItem::getQuantity).sum();
        order.pendingAt = pendingAt;
        return order;
    }

    public void abort(Instant abortedAt) {
        Assert.notNull(abortedAt, "abortedAt must not be null");

        if (this.status != OrderStatus.PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.ABORTED;
        this.abortedAt = abortedAt;
    }

    public void confirmPaid(Instant paidAt) {
        Assert.notNull(paidAt, "paidAt must not be null");

        if (this.status != OrderStatus.PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
    }

    public void cancel(Instant cancelledAt) {
        Assert.notNull(cancelledAt, "cancelledAt must not be null");

        if (this.status != OrderStatus.PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public void markAsFailed(Instant failedAt) {
        Assert.notNull(failedAt, "failedAt must not be null");

        this.status = OrderStatus.FAILED;
        this.failedAt = failedAt;
    }

    public void markAsExpired(Instant expiredAt) {
        Assert.notNull(expiredAt, "expiredAt must not be null");

        this.status = OrderStatus.EXPIRED;
        this.expiredAt = expiredAt;
    }

}
