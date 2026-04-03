package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentExpiredEvent;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ExpirePaymentCommand;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
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
public class ExpirePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public void execute(ExpirePaymentCommand command) {
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        Instant now = Instant.now();
        payment.fail("PAYMENT_EXPIRED", null, now);

        eventPublisher.publishPaymentExpired(new PaymentExpiredEvent(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                now
        ));

        log.info("Payment expired: paymentId={}", payment.getPaymentId());
    }
}
