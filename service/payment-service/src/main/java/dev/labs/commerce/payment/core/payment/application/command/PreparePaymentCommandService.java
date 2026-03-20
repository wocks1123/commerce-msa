package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAmountMismatchException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class PreparePaymentCommandService {

    private final PaymentRepository paymentRepository;

    public PreparePaymentResult prepare(ApprovePaymentCommand command) {
        Payment payment = paymentRepository.findByOrderIdWithLock(command.orderId())
                .orElseThrow(() -> new PaymentNotFoundException(command.orderId()));

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            return PreparePaymentResult.skip(payment);
        }

        if (command.paymentAmount() != payment.getAmount()) {
            throw new PaymentAmountMismatchException(payment.getAmount(), command.paymentAmount());
        }

        payment.markInProgress(Instant.now());
        paymentRepository.save(payment);

        return PreparePaymentResult.proceed(payment);
    }

}
