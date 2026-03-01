package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.port.PgGatewayRouter;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import dev.labs.commerce.payment.core.payment.domain.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAmountMismatchException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentApprovalFailedException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentInvalidStatusException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PgGatewayRouter pgGatewayRouter;
    private final PaymentEventPublisher eventPublisher;

    public ApprovePaymentResult execute(ApprovePaymentCommand command) {
        Payment payment = paymentRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new PaymentNotFoundException(command.orderId()));

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            throw new PaymentInvalidStatusException(payment.getStatus(), PaymentStatus.REQUESTED);
        }

        if (command.paymentAmount() != payment.getAmount()) {
            throw new PaymentAmountMismatchException(payment.getAmount(), command.paymentAmount());
        }

        PgApprovalResult pgResult = pgGatewayRouter.route(PgProvider.MOCK_PAY)
                .approve(new PgApprovalCommand(
                        command.paymentKey(),
                        payment.getOrderId(),
                        payment.getCustomerId(),
                        payment.getAmount()
                ));

        if (!pgResult.success()) {
            payment.fail(pgResult.failureCode(), pgResult.failureMessage(), Instant.now());
            paymentRepository.save(payment);
            throw new PaymentApprovalFailedException(pgResult.failureCode(), pgResult.failureMessage());
        }

        if (pgResult.approvedAmount() != payment.getAmount()) {
            payment.fail("AMOUNT_MISMATCH",
                    "expected=" + payment.getAmount() + ", actual=" + pgResult.approvedAmount(),
                    Instant.now());
            paymentRepository.save(payment);
            throw new PaymentAmountMismatchException(payment.getAmount(), pgResult.approvedAmount());
        }

        payment.approve(pgResult.pgTxId(), pgResult.approvedAt());
        paymentRepository.save(payment);

        eventPublisher.publishPaymentApproved(new PaymentApprovedEvent(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                pgResult.approvedAt()
        ));

        return new ApprovePaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                pgResult.pgTxId(),
                pgResult.approvedAt()
        );
    }
}
