package dev.labs.commerce.payment.core.payment.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentInvalidStatusException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @Column(name = "payment_id", nullable = false, updatable = false, length = 50)
    private String paymentId;

    @Column(name = "order_id", nullable = false, updatable = false, length = 36)
    private String orderId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 80)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false, updatable = false, length = 30)
    private PgProvider pgProvider;

    @Column(name = "pg_tx_id", length = 100)
    private String pgTxId;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public static Payment create(String orderId, long customerId, long amount, String currency,
                                 String idempotencyKey, PgProvider pgProvider, Instant requestedAt) {
        Assert.hasText(orderId, "orderId must not be blank");
        Assert.hasText(currency, "currency must not be blank");
        Assert.hasText(idempotencyKey, "idempotencyKey must not be blank");
        Assert.notNull(pgProvider, "pgProvider must not be null");
        Assert.isTrue(amount >= 0, "amount must be non-negative");
        Assert.notNull(requestedAt, "requestedAt must not be null");

        Payment payment = new Payment();
        payment.paymentId = UUID.randomUUID().toString();
        payment.orderId = orderId;
        payment.customerId = customerId;
        payment.status = PaymentStatus.REQUESTED;
        payment.amount = amount;
        payment.currency = currency;
        payment.idempotencyKey = idempotencyKey;
        payment.pgProvider = pgProvider;
        payment.requestedAt = requestedAt;
        return payment;
    }

    public static Payment createApproved(String orderId, long customerId, long amount, String currency,
                                         String idempotencyKey, PgProvider pgProvider, String pgTxId, Instant approvedAt) {
        Assert.hasText(orderId, "orderId must not be blank");
        Assert.hasText(currency, "currency must not be blank");
        Assert.hasText(idempotencyKey, "idempotencyKey must not be blank");
        Assert.notNull(pgProvider, "pgProvider must not be null");
        Assert.hasText(pgTxId, "pgTxId must not be blank");
        Assert.isTrue(amount >= 0, "amount must be non-negative");
        Assert.notNull(approvedAt, "approvedAt must not be null");

        Payment payment = new Payment();
        payment.paymentId = UUID.randomUUID().toString();
        payment.orderId = orderId;
        payment.customerId = customerId;
        payment.status = PaymentStatus.APPROVED;
        payment.amount = amount;
        payment.currency = currency;
        payment.idempotencyKey = idempotencyKey;
        payment.pgProvider = pgProvider;
        payment.pgTxId = pgTxId;
        payment.requestedAt = approvedAt;
        payment.approvedAt = approvedAt;
        return payment;
    }

    public void approve(String pgTxId, Instant approvedAt) {
        Assert.hasText(pgTxId, "pgTxId must not be blank");
        Assert.notNull(approvedAt, "approvedAt must not be null");
        if (this.status != PaymentStatus.REQUESTED) {
            throw new PaymentInvalidStatusException(this.status, PaymentStatus.REQUESTED);
        }
        this.status = PaymentStatus.APPROVED;
        this.pgTxId = pgTxId;
        this.approvedAt = approvedAt;
    }

    public void fail(String failureCode, @Nullable String failureMessage, Instant failedAt) {
        Assert.hasText(failureCode, "failureCode must not be blank");
        Assert.notNull(failedAt, "failedAt must not be null");
        if (this.status != PaymentStatus.REQUESTED) {
            throw new PaymentInvalidStatusException(this.status, PaymentStatus.REQUESTED);
        }
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.failedAt = failedAt;
    }

    public void cancel(Instant canceledAt) {
        if (this.status != PaymentStatus.APPROVED) {
            throw new PaymentInvalidStatusException(this.status, PaymentStatus.APPROVED);
        }
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

}
