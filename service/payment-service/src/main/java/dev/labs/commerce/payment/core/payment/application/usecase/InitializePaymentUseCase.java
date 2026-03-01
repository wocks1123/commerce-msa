package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InitializePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public InitializePaymentResult execute(InitializePaymentCommand command) {
        if (paymentRepository.existsByOrderId(command.orderId())) {
            throw new PaymentAlreadyExistsException("orderId=" + command.orderId());
        }
        if (paymentRepository.findByIdempotencyKey(command.idempotencyKey()).isPresent()) {
            throw new PaymentAlreadyExistsException("idempotencyKey=" + command.idempotencyKey());
        }

        Payment payment = Payment.create(
                command.orderId(),
                command.customerId(),
                command.amount(),
                command.currency(),
                command.idempotencyKey(),
                command.pgProvider(),
                command.requestedAt()
        );

        Payment saved = paymentRepository.save(payment);

        return new InitializePaymentResult(
                saved.getPaymentId(),
                saved.getOrderId(),
                saved.getStatus(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getRequestedAt()
        );
    }
}
