package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;
import dev.labs.commerce.payment.core.payment.application.port.PgGatewayRouter;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgDeclinedException;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgUncertainException;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAmountMismatchException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PgGatewayRouter pgGatewayRouter;
    private final PaymentEventPublisher eventPublisher;

    public ApprovePaymentResult execute(ApprovePaymentCommand command) {
        Payment payment = paymentRepository.findByOrderIdWithLock(command.orderId())
                .orElseThrow(() -> new PaymentNotFoundException(command.orderId()));

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            return ApprovePaymentResult.ofCurrentState(payment);
        }

        if (command.paymentAmount() != payment.getAmount()) {
            throw new PaymentAmountMismatchException(payment.getAmount(), command.paymentAmount());
        }

        payment.markInProgress(Instant.now());
        paymentRepository.save(payment);

        PgApprovalResult pgResult;
        try {
            pgResult = pgGatewayRouter.route(PgProvider.MOCK_PAY)
                    .approve(new PgApprovalCommand(
                            command.paymentKey(),
                            payment.getOrderId(),
                            payment.getCustomerId(),
                            payment.getAmount()
                    ));
        } catch (PgDeclinedException e) {
            log.warn("PG declined: reason={}, pgCode={}", e.getReason(), e.getPgCode());
            return failPayment(payment, e.getReason().name(), e.getPgMessage());
        } catch (PgUncertainException e) {
            log.error("PG uncertain: failureCode={}, retryable={}", e.getFailureCode(), e.isRetryable(), e);
            return abortPayment(payment, e.getFailureCode(), e.getFailureMessage());
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

        return ApprovePaymentResult.approved(payment);
    }

    private ApprovePaymentResult failPayment(Payment payment, String failureCode, String failureMessage) {
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
        return ApprovePaymentResult.failed(payment);
    }

    private ApprovePaymentResult abortPayment(Payment payment, String failureCode, String failureMessage) {
        payment.abort(failureCode, failureMessage, Instant.now());
        paymentRepository.save(payment);
        return ApprovePaymentResult.aborted(payment);
    }

}
