package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.domain.Payment;

public sealed interface PreparePaymentResult permits ProceedPaymentResult, SkipPaymentResult {

    String orderId();

    static PreparePaymentResult proceed(Payment payment) {
        return new ProceedPaymentResult(
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getPgProvider()
        );
    }

    static PreparePaymentResult skip(Payment payment) {
        return new SkipPaymentResult(
                payment.getPaymentId(),  // paymentId
                payment.getOrderId(),    // orderId
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPgTxId(),
                payment.getApprovedAt(),
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }

}
