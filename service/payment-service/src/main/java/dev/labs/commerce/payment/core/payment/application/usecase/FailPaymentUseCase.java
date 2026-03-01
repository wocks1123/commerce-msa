package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.FailPaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.FailPaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.event.PaymentFailedEvent;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentInvalidStatusException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class FailPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public FailPaymentResult execute(FailPaymentCommand command) {
        Payment payment = paymentRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new PaymentNotFoundException(command.orderId()));

        if (payment.getStatus() != PaymentStatus.REQUESTED) {
            throw new PaymentInvalidStatusException(payment.getStatus(), PaymentStatus.REQUESTED);
        }

        payment.fail(command.failureCode(), command.failureMessage(), Instant.now());
        paymentRepository.save(payment);

        applicationEventPublisher.publishEvent(new PaymentFailedEvent(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getFailedAt()
        ));

        return new FailPaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getFailedAt()
        );
    }
}
