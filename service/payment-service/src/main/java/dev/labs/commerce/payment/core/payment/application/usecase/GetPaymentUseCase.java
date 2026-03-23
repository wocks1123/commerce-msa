package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.GetPaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPaymentUseCase {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public GetPaymentResult execute(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return new GetPaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPgProvider(),
                payment.getPgTxId(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getRequestedAt(),
                payment.getInProgressAt(),
                payment.getApprovedAt(),
                payment.getFailedAt(),
                payment.getAbortedAt(),
                payment.getCanceledAt()
        );
    }
}
