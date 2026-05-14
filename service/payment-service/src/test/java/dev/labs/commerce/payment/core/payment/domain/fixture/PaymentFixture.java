package dev.labs.commerce.payment.core.payment.domain.fixture;

import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

public class PaymentFixture {

    private String paymentId;
    private String orderId;
    private long customerId;
    private PaymentStatus status;
    private long amount;
    private String currency;
    private String idempotencyKey;
    private PgProvider pgProvider;
    private String pgTxId;
    private String failureCode;
    private String failureMessage;
    private Instant requestedAt;
    private Instant inProgressAt;
    private Instant approvedAt;
    private Instant failedAt;
    private Instant abortedAt;
    private Instant canceledAt;
    private long version;

    private PaymentFixture() {
    }

    public static PaymentFixture builder() {
        return new PaymentFixture();
    }

    public PaymentFixture paymentId(String paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    public PaymentFixture orderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public PaymentFixture customerId(long customerId) {
        this.customerId = customerId;
        return this;
    }

    public PaymentFixture status(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentFixture amount(long amount) {
        this.amount = amount;
        return this;
    }

    public PaymentFixture currency(String currency) {
        this.currency = currency;
        return this;
    }

    public PaymentFixture idempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        return this;
    }

    public PaymentFixture pgProvider(PgProvider pgProvider) {
        this.pgProvider = pgProvider;
        return this;
    }

    public PaymentFixture pgTxId(String pgTxId) {
        this.pgTxId = pgTxId;
        return this;
    }

    public PaymentFixture failureCode(String failureCode) {
        this.failureCode = failureCode;
        return this;
    }

    public PaymentFixture failureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
        return this;
    }

    public PaymentFixture requestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
        return this;
    }

    public PaymentFixture inProgressAt(Instant inProgressAt) {
        this.inProgressAt = inProgressAt;
        return this;
    }

    public PaymentFixture approvedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
        return this;
    }

    public PaymentFixture failedAt(Instant failedAt) {
        this.failedAt = failedAt;
        return this;
    }

    public PaymentFixture abortedAt(Instant abortedAt) {
        this.abortedAt = abortedAt;
        return this;
    }

    public PaymentFixture canceledAt(Instant canceledAt) {
        this.canceledAt = canceledAt;
        return this;
    }

    public PaymentFixture version(long version) {
        this.version = version;
        return this;
    }

    public PaymentFixture withSample() {
        this.paymentId = UUID.randomUUID().toString();
        this.orderId = UUID.randomUUID().toString();
        this.customerId = 100L;
        this.status = PaymentStatus.REQUESTED;
        this.amount = 10000L;
        this.currency = "KRW";
        this.idempotencyKey = UUID.randomUUID().toString();
        this.pgProvider = PgProvider.MOCK_PAY;
        this.requestedAt = Instant.now();
        return this;
    }

    public Payment build() {
        final Payment payment = BeanUtils.instantiateClass(Payment.class);
        ReflectionTestUtils.setField(payment, "paymentId", paymentId);
        ReflectionTestUtils.setField(payment, "orderId", orderId);
        ReflectionTestUtils.setField(payment, "customerId", customerId);
        ReflectionTestUtils.setField(payment, "status", status);
        ReflectionTestUtils.setField(payment, "amount", amount);
        ReflectionTestUtils.setField(payment, "currency", currency);
        ReflectionTestUtils.setField(payment, "idempotencyKey", idempotencyKey);
        ReflectionTestUtils.setField(payment, "pgProvider", pgProvider);
        ReflectionTestUtils.setField(payment, "pgTxId", pgTxId);
        ReflectionTestUtils.setField(payment, "failureCode", failureCode);
        ReflectionTestUtils.setField(payment, "failureMessage", failureMessage);
        ReflectionTestUtils.setField(payment, "requestedAt", requestedAt);
        ReflectionTestUtils.setField(payment, "inProgressAt", inProgressAt);
        ReflectionTestUtils.setField(payment, "approvedAt", approvedAt);
        ReflectionTestUtils.setField(payment, "failedAt", failedAt);
        ReflectionTestUtils.setField(payment, "abortedAt", abortedAt);
        ReflectionTestUtils.setField(payment, "canceledAt", canceledAt);
        ReflectionTestUtils.setField(payment, "version", version);
        return payment;
    }
}
