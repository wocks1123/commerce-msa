package dev.labs.commerce.payment.api.scheduling;

import dev.labs.commerce.payment.config.PaymentExpiryProperties;
import dev.labs.commerce.payment.core.payment.application.usecase.ExpirePaymentUseCase;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ExpirePaymentCommand;
import dev.labs.commerce.payment.core.payment.domain.PaymentDao;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpiryScheduler {

    private final PaymentDao paymentDao;
    private final ExpirePaymentUseCase expirePaymentUseCase;
    private final PaymentExpiryProperties properties;

    @Scheduled(fixedDelay = 60_000)
    public void expirePayments() {
        Instant threshold = Instant.now().minus(properties.getRequestedExpiryMinutes(), ChronoUnit.MINUTES);
        List<String> paymentIds = paymentDao.findIdsByStatusAndRequestedAtBefore(PaymentStatus.REQUESTED, threshold);

        if (paymentIds.isEmpty()) {
            return;
        }

        log.info("Expiring {} payments older than {} minutes", paymentIds.size(), properties.getRequestedExpiryMinutes());
        paymentIds.forEach(paymentId -> {
            try {
                expirePaymentUseCase.execute(new ExpirePaymentCommand(paymentId));
            } catch (Exception e) {
                log.error("Failed to expire payment: paymentId={}", paymentId, e);
            }
        });
    }
}
