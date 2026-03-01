package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.RegisterApprovedPaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.RegisterApprovedPaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterApprovedPaymentUseCase {

    private final PaymentRepository paymentRepository;

    public RegisterApprovedPaymentResult execute(RegisterApprovedPaymentCommand command) {
        if (paymentRepository.existsByOrderId(command.orderId())) {
            throw new PaymentAlreadyExistsException("orderId=" + command.orderId());
        }
        if (paymentRepository.findByIdempotencyKey(command.idempotencyKey()).isPresent()) {
            throw new PaymentAlreadyExistsException("idempotencyKey=" + command.idempotencyKey());
        }

        Payment payment = Payment.createApproved(
                command.orderId(),
                command.customerId(),
                command.amount(),
                command.currency(),
                command.idempotencyKey(),
                command.pgProvider(),
                command.pgTxId(),
                command.approvedAt()
        );

        Payment saved = paymentRepository.save(payment);

        return new RegisterApprovedPaymentResult(
                saved.getPaymentId(),
                saved.getOrderId(),
                saved.getStatus(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getPgTxId(),
                saved.getApprovedAt()
        );
    }
}
