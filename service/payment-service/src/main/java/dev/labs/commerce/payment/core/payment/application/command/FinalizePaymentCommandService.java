package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class FinalizePaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public FinalizePaymentResult finalize(String orderId, PgApprovalResult pgResult) {
        Payment payment = paymentRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));

        if (pgResult.aborted()) {
            payment.abort(pgResult.failureCode(), pgResult.failureMessage(), Instant.now());
            paymentRepository.save(payment);
            return FinalizePaymentResult.of(payment);
        }

        if (!pgResult.success()) {
            return failPayment(payment, pgResult.failureCode(), pgResult.failureMessage());
        }

        if (pgResult.approvedAmount() != payment.getAmount()) {
            return failPayment(payment, "AMOUNT_MISMATCH",
                    "expected=" + payment.getAmount() + ", actual=" + pgResult.approvedAmount());
        }

        payment.approve(pgResult.pgTxId(), pgResult.approvedAt());
        paymentRepository.save(payment);
        eventPublisher.publishPaymentApproved(new PaymentApprovedEvent(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getApprovedAt()
        ));

        return FinalizePaymentResult.of(payment);
    }

    private FinalizePaymentResult failPayment(Payment payment, String failureCode, @Nullable String failureMessage) {
        payment.fail(failureCode, failureMessage, Instant.now());
        paymentRepository.save(payment);
        eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getFailedAt()
        ));
        return FinalizePaymentResult.of(payment);
    }
}
