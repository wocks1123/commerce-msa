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

    @Column(name = "payment_pending_at")
    private Instant paymentPendingAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "aborted_at")
    private Instant abortedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "failed_at")
    private Instant failedAt;


    public static SalesOrder create(long customerId, String currency, List<OrderItem> items) {
        Assert.notEmpty(items, "items must not be empty");
        Assert.hasText(currency, "currency must not be blank");

        SalesOrder order = new SalesOrder();
        order.orderId = UUID.randomUUID().toString();
        order.customerId = customerId;
        order.currency = currency;
        order.status = OrderStatus.PENDING;
        order.items = new ArrayList<>(items);
        order.totalPrice = items.stream().mapToLong(OrderItem::getLineAmount).sum();
        order.totalAmount = items.stream().mapToLong(OrderItem::getQuantity).sum();
        return order;
    }

    public void confirmStockReserved(Instant paymentPendingAt) {
        if (this.status != OrderStatus.PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.PAYMENT_PENDING;
        this.paymentPendingAt = paymentPendingAt;
    }

    public void abortByStockFailure(Instant abortedAt) {
        if (this.status != OrderStatus.PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.ABORTED;
        this.abortedAt = abortedAt;
    }

    public void confirmPaid(Instant paidAt) {
        if (this.status != OrderStatus.PAYMENT_PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
    }

    public void abortByPaymentFailure(Instant abortedAt) {
        if (this.status != OrderStatus.PAYMENT_PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.ABORTED;
        this.abortedAt = abortedAt;
    }

    public void cancel(Instant cancelledAt) {
        if (this.status != OrderStatus.PAYMENT_PENDING) throw new InvalidOrderStateException();
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public void markAsFailed(Instant failedAt) {
        this.status = OrderStatus.FAILED;
        this.failedAt = failedAt;
    }

}
